package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FireBaseUserLiveData : LiveData<FirebaseUser?>() {
    private val firebaseUser = FirebaseAuth.getInstance()

    private val clickState = FirebaseAuth.AuthStateListener {
        value = it.currentUser
    }




    override fun onActive() {
        firebaseUser.addAuthStateListener(clickState)
    }

    override fun onInactive() {

        firebaseUser.removeAuthStateListener(clickState)

    }




}