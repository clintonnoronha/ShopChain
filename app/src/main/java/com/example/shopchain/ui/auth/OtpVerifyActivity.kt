package com.example.shopchain.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.shopchain.R
import com.example.shopchain.model.User
import com.example.shopchain.ui.main.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase

class OtpVerifyActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    private var name: String? = ""
    private var mobile: String? = ""
    private var login: Boolean? = false
    lateinit var credential: PhoneAuthCredential

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verify)

        auth = FirebaseAuth.getInstance()

        val storedVerificationId: String? = intent.getStringExtra("storedVerificationId")
        val storedVerificationIdLogin: String? = intent.getStringExtra("storedVerificationIdLogin")
        name = intent.getStringExtra("username")
        mobile = intent.getStringExtra("mobile")
        login = intent.getBooleanExtra("login", false)

        val otpGiven = findViewById<EditText>(R.id.id_otp)
        val btnVerify = findViewById<Button>(R.id.verifyBtn)

        btnVerify.setOnClickListener{
            val otp = otpGiven.text.toString().trim()
            if (!otp.isEmpty()) {
                if (login as Boolean) {
                    credential = PhoneAuthProvider.getCredential(storedVerificationIdLogin.toString(), otp)
                    signInWithPhoneAuthCredential(credential)
                } else {
                    credential = PhoneAuthProvider.getCredential(storedVerificationId.toString(), otp)
                    signInWithPhoneAuthCredentialRegistration(credential)
                }
            }else{
                Toast.makeText(this,"Enter OTP",Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("LogCheck", "User sign in successful!")
                        startActivity(Intent(this@OtpVerifyActivity, HomeActivity::class.java))
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        finish()
                    } else {
                        // Sign in failed, display a message and update the UI
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            Toast.makeText(this,"Invalid OTP",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
    }

    private fun signInWithPhoneAuthCredentialRegistration(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserToDatabase()
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this,"Invalid OTP",Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun saveUserToDatabase() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(
                uid,
                name!!,
                mobile!!
        )
        ref.setValue(user).addOnSuccessListener {
            Log.d("LogCheck", "User registered to Firebase Database")
            startActivity(Intent(this@OtpVerifyActivity, HomeActivity::class.java))
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            finish()
        }.addOnFailureListener {
                    Log.d("LogCheck", "${it.message}")
        }
    }
}