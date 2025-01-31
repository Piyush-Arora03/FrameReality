package com.example.framereality.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.framereality.R
import com.example.framereality.databinding.ActivityLoginPhoneBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class LoginPhoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginPhoneBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var mVerificationId: String? = null
    private val TAG = "LOGIN_PHONE_ACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Set Status Bar Color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Handle Back Button
        binding.ToolBarBackButton.setOnClickListener { finish() }
        binding.resend.visibility = View.GONE
        // Initialize Phone Login Callbacks
        setupPhoneLoginCallbacks()

        // Handle OTP Send Button Click
        binding.SendOTPbtn.setOnClickListener { validatePhoneNumber() }

        // Handle OTP Resend Button Click
        binding.resend.setOnClickListener {
            if (forceResendingToken != null) resendVerificationCode()
        }

        // Handle OTP Verification Button Click
        binding.OTPbtn.setOnClickListener {
            val otp = binding.PhoneEt.text.toString().trim()
            if (otp.length == 6) verifyPhoneNumberWithCode(otp)
            else binding.OTPet.error = "Enter a valid 6-digit OTP"
        }
    }

    // Validates and starts phone number verification
    private fun validatePhoneNumber() {
        val phoneCode = binding.PhoneCodeEt.selectedCountryCodeWithPlus
        val phoneNumber = binding.PhoneEt.text.toString().trim()
        val phoneNumberWithCode = phoneCode + phoneNumber

        if (phoneNumber.isEmpty()) {
            binding.PhoneEt.error = "Enter Phone Number"
            binding.PhoneEt.requestFocus()
        }
        else{
            startPhoneNumberVerification(phoneNumberWithCode)
            binding.resend.visibility = View.VISIBLE
        }
    }

    // Starts phone number verification process
    private fun startPhoneNumberVerification(phoneNumberWithCode: String) {
        showProgressDialog()
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumberWithCode)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Resends OTP
    private fun resendVerificationCode() {
        showProgressDialog()
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(binding.PhoneCodeEt.selectedCountryCodeWithPlus + binding.PhoneEt.text.toString().trim())
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks)
            .setForceResendingToken(forceResendingToken!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Sets up phone authentication callbacks
    private fun setupPhoneLoginCallbacks() {
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                mVerificationId = verificationId
                forceResendingToken = token
                Toast.makeText(this@LoginPhoneActivity, "OTP Sent", Toast.LENGTH_SHORT).show()
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                hideProgressDialog()
                Toast.makeText(this@LoginPhoneActivity, "Verification failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Verifies the phone number using OTP
    private fun verifyPhoneNumberWithCode(otp: String) {
        showProgressDialog()
        val credential = PhoneAuthProvider.getCredential(mVerificationId!!, otp)
        signInWithPhoneAuthCredential(credential)
    }

    // Signs in user with phone authentication
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        showProgressDialog()
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener {
            if (it.additionalUserInfo?.isNewUser == true) {
                Log.d(TAG, "New user registered")
                updateUserInfo()
            } else {
                navigateToMainScreen()
            }
        }.addOnFailureListener {
            hideProgressDialog()
            Toast.makeText(this, "Login failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Updates user info in Firebase Database
    private fun updateUserInfo() {
        val userId = firebaseAuth.uid ?: return
        val userMap = mapOf(
            "uid" to userId,
            "phone" to (binding.PhoneCodeEt.selectedCountryCodeWithPlus + binding.PhoneEt.text.toString().trim()),
            "userType" to "user"
        )
        FirebaseDatabase.getInstance().getReference("Users").child(userId).setValue(userMap)
            .addOnSuccessListener { navigateToMainScreen() }
            .addOnFailureListener {
                hideProgressDialog()
                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
            }
    }

    // Navigates to MainActivity
    private fun navigateToMainScreen() {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    // Shows a simple progress dialog
    private var progressDialog: AlertDialog? = null
    private fun showProgressDialog() {
        val builder = AlertDialog.Builder(this).setCancelable(false)
        val layout = LinearLayout(this).apply { setPadding(50, 50, 50, 50) }
        layout.addView(ProgressBar(this))
        builder.setView(layout)
        progressDialog = builder.create()
        progressDialog?.show()
    }

    // Hides the progress dialog
    private fun hideProgressDialog() {
        progressDialog?.takeIf { it.isShowing }?.dismiss()
    }
}
