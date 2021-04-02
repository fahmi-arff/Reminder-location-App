package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config
import java.lang.Error


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var datasource: FakeDataSource
    private lateinit var context: Context

    @Before
    fun setup() {
        datasource = FakeDataSource()
        saveReminderViewModel =
                SaveReminderViewModel(ApplicationProvider.getApplicationContext(), datasource)
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun cleanDatasource() = runBlocking {
        datasource.deleteAllReminders()
        stopKoin()
    }

    @Test
    fun validateEnteredData_validData_returnsTrue() {
        val title = "title"
        val location = "location"
        val reminderDataItem = ReminderDataItem(
                title, null, location,
                null, null
        )
        val isValid = saveReminderViewModel.validateEnteredData(reminderDataItem)

        assertThat(isValid, `is`(true))
    }

    @Test
    fun validateEnteredData_noLocation_returnsFalse() {
        val title = "title"
        val reminderDataItem = ReminderDataItem(
                title, null, null,
                null, null
        )
        val isValid = saveReminderViewModel.validateEnteredData(reminderDataItem)

        assertThat(isValid, `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))

    }

    @Test
    fun validateEnteredData_noTitle_returnsFalse() {
        val location = "location"
        val reminderDataItem = ReminderDataItem(
                null, null, location,
                null, null
        )
        val isValid = saveReminderViewModel.validateEnteredData(reminderDataItem)

        assertThat(isValid, `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun check_loading() = mainCoroutineRule.runBlockingTest {
        val reminderDataItem = ReminderDataItem(
                "title", "desc", "location",
                -6.824,108.825
        )

        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder( reminderDataItem )

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(
                saveReminderViewModel.showToast.getOrAwaitValue(),
                `is`(context.getString(R.string.reminder_saved))
        )
    }

    @Test
    fun shouldReturnError() = mainCoroutineRule.runBlockingTest {
        datasource.setReturnError(true)
        val reminderDataItem = ReminderDataItem(
                null, "desc", "location",
                -6.824,108.825
        )


        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)

        val result = datasource.getReminder(reminderDataItem.id)

        result as Result.Error
        assertThat(result.message, `is`("Test Exception"))
    }


}