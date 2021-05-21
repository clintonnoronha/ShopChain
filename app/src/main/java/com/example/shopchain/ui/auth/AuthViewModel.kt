package com.example.shopchain.ui.auth

import android.view.View
import androidx.lifecycle.ViewModel
import com.example.shopchain.model.CountryCodes
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel: ViewModel() {

    var mobileNumber: String? = null
    var countryCodes = CountryCodes()
    var otp: String? = null

    var authListener: AuthListener? = null

    fun onLoginButtonClicked(view: View) {
        authListener?.onStarted()
        if (mobileNumber.isNullOrEmpty()) {
            // Invalid Details
            authListener?.onFailure("Invalid mobile number.")
            return
        }
        // Valid details
//        val auth = FirebaseAuth.getInstance()
//        auth.signInWithCredential()

        authListener?.onSuccess()
    }

}