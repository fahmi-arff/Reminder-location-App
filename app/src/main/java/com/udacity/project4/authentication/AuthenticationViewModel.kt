package com.udacity.project4.authentication

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.base.BaseViewModel

enum class AuthenticationState {
    AUTHENTICATED, UNAUTHENTICATED
}

class AuthenticationViewModel(val app : Application) : BaseViewModel(app) {

  val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else
            AuthenticationState.UNAUTHENTICATED
    }
}