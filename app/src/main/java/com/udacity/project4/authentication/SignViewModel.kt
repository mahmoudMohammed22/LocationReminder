package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class SignViewModel : ViewModel() {

    enum class LoginState{
        AUTHENTICATED , UNAUTHENTICATED
    }

    val state = FireBaseUserLiveData().map {
        if(it != null){
            LoginState.AUTHENTICATED
        }else{
            LoginState.UNAUTHENTICATED
        }
    }
}