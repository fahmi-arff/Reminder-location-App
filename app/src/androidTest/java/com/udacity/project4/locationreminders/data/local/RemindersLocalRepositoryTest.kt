package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainAndroidTestCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var mainCoroutineRule = MainAndroidTestCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var remindersDAO: RemindersDao
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder( ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        remindersDAO = database.reminderDao()
        repository =
            RemindersLocalRepository( remindersDAO, Dispatchers.Main )
    }

    @After
    fun closeDB() {
        database.close()
    }

    @Test
    fun saveReminderAndGetById() = mainCoroutineRule.runBlockingTest {

        val reminder = ReminderDTO(
            "title", "desc", "location",
            -6.824,108.825
        )
        repository.saveReminder(reminder)

        val loaded = repository.getReminder(reminder.id) as Result.Success

        assertThat(loaded, Matchers.notNullValue())
        assertThat(loaded.data.id, `is`(reminder.id))
        assertThat(loaded.data.description, `is`(reminder.description))
        assertThat(loaded.data.location, `is`(reminder.location))
        assertThat(loaded.data.latitude, `is`(reminder.latitude))
        assertThat(loaded.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveReminderAndGetReminders() = runBlockingTest {

        val reminder = ReminderDTO(
                "title", "desc", "location",
                -6.824,108.825
        )
        repository.saveReminder(reminder)

        val loaded = repository.getReminders() as Result.Success

        assertThat(loaded, Matchers.notNullValue())
        assertThat(loaded.data.size, `is`(1))
        assertThat(loaded.data, hasItem(reminder))
    }

    @Test
    fun getRemindersAndDeleteAllReminders() = runBlockingTest {

        val reminder1 = ReminderDTO(
                "title", "desc", "location",
                -6.824,108.825
        )

        val reminder2 = ReminderDTO(
                "title2", "desc2", "location2",
                -6.824,109.825
        )
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        val savedReminders = repository.getReminders() as Result.Success

        assertThat(savedReminders.data, notNullValue())
        assertThat(savedReminders.data.size, `is`(2))

        repository.deleteAllReminders()

        val reminders = repository.getReminders() as Result.Success
        assertThat(reminders.data.size, `is`(0))
    }

    @Test
    fun getReminderById_wrongId_ReturnNull() = runBlockingTest {

        val reminder = repository.getReminder("9") as Result.Error

        assertThat(reminder.message, notNullValue())
        assertThat(reminder.message, `is`("Reminder not found!"))
    }

}