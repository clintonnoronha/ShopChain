package com.example.shopchain.ui.auth

import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.os.ConfigurationCompat
import com.example.shopchain.R
import com.example.shopchain.model.CountryCodes
import com.example.shopchain.ui.main.HomeActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.*
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var btnGenerateOtp: Button
    lateinit var txtLogin: TextView
    var spinner: Spinner? = null
    var countryCode: String? = null
    lateinit var locale: Locale
    private var defaultCountry: String? = null
    lateinit var name: EditText
    lateinit var mobile: EditText
    private var userName: String = ""
    private var number: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btnGenerateOtp = findViewById(R.id.btn_generate_otp)
        txtLogin = findViewById(R.id.txtLogin)
        spinner = findViewById(R.id.spnCountrySignUp)
        name = findViewById(R.id.et_name)
        mobile = findViewById(R.id.et_number)

        auth = FirebaseAuth.getInstance()

        if (spinner != null) {
            val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, CountryCodes.countryNames)
            spinner?.adapter = arrayAdapter
            locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0)
            defaultCountry = locale.displayCountry.trim()
            spinner?.setSelection(arrayAdapter.getPosition(defaultCountry))

            spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    countryCode = "+" + CountryCodes.countryAreaCodes.get(position)
                    Log.d("LogCheck", "default county = ${defaultCountry}, country code = {$countryCode}")
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Code to perform some action when nothing is selected
                }
            }
        }


        btnGenerateOtp.setOnClickListener {
            generateOtp()
        }

        // Callback function for Phone Auth
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                startActivity(Intent(this@SignUpActivity, HomeActivity::class.java))
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                finish()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@SignUpActivity, "${e.message}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("TAG","onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
                val intent = Intent(this@SignUpActivity, OtpVerifyActivity::class.java)
                intent.putExtra("storedVerificationId",storedVerificationId)
                intent.putExtra("username", userName)
                intent.putExtra("mobile", number)
                startActivity(intent)
            }
        }

        txtLogin.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }

    }

    private fun generateOtp() {
        userName = name.text.toString().trim()
        number = mobile.text.toString().trim()
        if(!number.isEmpty() && !userName.isEmpty()){
            // Check
            number =  countryCode + number
            sendVerificationCode (number)
        } else {
            Toast.makeText(this,"Enter the credentials!",Toast.LENGTH_LONG).show()
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

    override fun onBackPressed() {
        val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}