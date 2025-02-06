package com.example.framereality.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.framereality.MyUtils
import com.example.framereality.databinding.ActivityRegisterEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterEmailActivity : AppCompatActivity() {
    // View Binding instance
    private lateinit var binding: ActivityRegisterEmailBinding

    // Log tag for debugging
    private val TAG = "REGISTER_EMAIL_TAG"

    // Firebase Authentication instance
    private lateinit var firebaseAuth: FirebaseAuth

    // Progress Dialog for showing loading state
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize Progress Dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        // Handle toolbar back button click
        binding.toolbarBackBtn.setOnClickListener {
            finish()
        }

        // Handle "Already have an account?" text click
        binding.haveAccountTv.setOnClickListener {
            finish()
//            startActivity(Intent(this,LoginOptionsActivity::class.java))
        }

        // Handle register button click
        binding.registerBtn.setOnClickListener {
            validateData()
        }
    }

    // Variables to store user input
    private var email = ""
    private var password = ""
    private var cPassword = ""

    // Validate user input before registration
    private fun validateData() {
        // Retrieve user inputs
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString()
        cPassword = binding.cPasswordEt.text.toString()

        Log.d(TAG, "validateData: Email: $email")
        Log.d(TAG, "validateData: Password: $password")
        Log.d(TAG, "validateData: Confirm Password: $cPassword")

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.error = "Invalid Email Pattern"
            binding.emailEt.requestFocus()
        }
        // Check if password is empty
        else if (password.isEmpty()) {
            binding.passwordEt.error = "Enter Password"
            binding.passwordEt.requestFocus()
        }
        // Check if passwords match
        else if (password != cPassword) {
            binding.cPasswordEt.error = "Password doesn't match"
            binding.cPasswordEt.requestFocus()
        }
        // If all validations pass, proceed with user registration
        else {
            registerUser()
        }
    }

    // Register the user with Firebase Authentication
    private fun registerUser() {
        progressDialog.setMessage("Creating Account")
        progressDialog.show()

        // Create a new user account with email and password
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "registerUser: Register Success")
                updateUserInfo()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "registerUser: ", e)
                MyUtils.toast(this, "Failed due to ${e.message}")
                progressDialog.dismiss()
            }
    }

    // Store user information in Firebase Realtime Database
    private fun updateUserInfo() {
        progressDialog.setMessage("Saving User Info...!")

        // Get current timestamp
        val timestamp = MyUtils.timestamp()
        val registeredUserEmail = firebaseAuth.currentUser!!.email
        val registeredUserUid = firebaseAuth.uid

        // Create a hashmap to store user details
        val hashMap = HashMap<String, Any>()
        hashMap["uid"] = "$registeredUserUid"
        hashMap["email"] = "$registeredUserEmail"
        hashMap["name"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = MyUtils.USER_TYPE_EMAIL
        hashMap["token"] = ""

        // Reference to Firebase Realtime Database
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("$registeredUserUid")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserInfo: Info saved")
                progressDialog.dismiss()

                // Navigate to the main activity and clear the activity stack
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateUserInfo: ", e)
                MyUtils.toast(this, "Failed to save due to ${e.message}")
                progressDialog.dismiss()
            }
    }
}
