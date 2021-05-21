package com.example.shopchain.ui.auth

import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.os.ConfigurationCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.shopchain.R
import com.example.shopchain.databinding.ActivityLoginBinding
import com.example.shopchain.model.CountryCodes
import com.example.shopchain.ui.main.HomeActivity
import com.example.shopchain.util.toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity(), AuthListener {

    lateinit var auth: FirebaseAuth
    lateinit var locale: Locale
    lateinit var credential: PhoneAuthCredential
    private var defaultCountry: String? = null
    private var countryCode: String? = null
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var number: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        val viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        binding.viewModel = viewModel
        viewModel.authListener = this

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, CountryCodes.countryNames)
        locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0)
        defaultCountry = locale.displayCountry.trim()
        binding.spnCountry.adapter = arrayAdapter
        binding.spnCountry.setSelection(arrayAdapter.getPosition(defaultCountry))

        binding.spnCountry.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                countryCode = "+" + CountryCodes.countryAreaCodes.get(position)
                Log.d("LogCheck", "default country = ${defaultCountry}, country code = {$countryCode}")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Code to perform some action when nothing is selected
            }

        }

        binding.txtRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                finish()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@LoginActivity, "${e.message}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("TAG","onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
                val intent = Intent(this@LoginActivity, OtpVerifyActivity::class.java)
                intent.putExtra("storedVerificationIdLogin", storedVerificationId)
                intent.putExtra("mobileLogin", number)
                intent.putExtra("login", true)
                startActivity(intent)
            }
        }

        binding.btnGetOtp.setOnClickListener {
            number = binding.etMobileNumber.text.toString().trim()
            if (!number.isNullOrEmpty()) {
                number = countryCode + number
                sendVerificationCode(number)
            } else {
                Toast.makeText(this,"Enter your mobile number!",Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun sendVerificationCode(number: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(number) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun onStarted() {

    }

    override fun onSuccess() {

    }

    override fun onFailure(message: String) {
        toast(message)
    }

    override fun onBackPressed() {
        this.finishAffinity()
    }
}