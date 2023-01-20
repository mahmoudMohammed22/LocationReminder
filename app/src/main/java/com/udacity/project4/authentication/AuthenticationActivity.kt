package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainFragment"
        const val SIGN_IN_RESULT_CODE = 111
    }
    private lateinit var binding : ActivityAuthenticationBinding

    private val viewModel : SignViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_authentication)

//         complete TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          complete TODO: If the user was authenticated, send him to RemindersActivity

//          complete TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        // state of user login or register
        observeLoginState()

        binding.lifecycleOwner = this

        // make botton login
        binding.authLogin.setOnClickListener {
            // to go screen sign in or register
            signInFlow()

        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // check user is sign in
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")

            } else {
                // Sign in failed. If response is null the user canceled the
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }


    }

    private fun signInFlow() {

        //Give the user two option to sign in
        // with emil ot account Google
        //if user dont have account they will need to add emile and password

        val itemProvider = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        // this layout to desgin screen user sign in
        val layout = AuthMethodPickerLayout.Builder(R.layout.cutome_layout)
            .setEmailButtonId(R.id.Login_in_emil)
            .setGoogleButtonId(R.id.Login_in_google)

        // creat and launch sign in
        // We listen to the response of this activity with the
        // SIGN_IN_REQUEST_CODE
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(layout.build())
                .setAvailableProviders(itemProvider)
                .build(), AuthenticationActivity.SIGN_IN_RESULT_CODE
        )
    }

    fun navgateToReminderActivity(){
        val intent = Intent(this,RemindersActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun observeLoginState(){

        viewModel.state.observe(this, Observer {
            when(it){
                // if user is sign in yo should go to reminderList screen
                SignViewModel.LoginState.AUTHENTICATED->{
                    //chenge login name and function to go to reminder screen when user is sign in
                    binding.authLogin.text = "Continue"
                    binding.authLogin.setOnClickListener { navgateToReminderActivity() }
                }else ->{
                // else make go to sign screen
                // make auth_login back old name
                binding.authLogin.text ="Login"
                binding.authLogin.setOnClickListener { signInFlow() }


                }
            }
        })
    }


}
