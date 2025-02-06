package com.example.framereality.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.framereality.R
import com.example.framereality.databinding.ActivityLoginViewBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*

class LoginEmailActivity : AppCompatActivity() {

    // View Binding
    private lateinit var binding: ActivityLoginViewBinding

    // Firebase Authentication
    private lateinit var firebaseAuth: FirebaseAuth

    // Debugging Tag
    private val TAG = "LOGIN_EMAIL_ACTIVITY"

    // Variables for storing user credentials
    private var email = ""
    private var pass = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize View Binding
        binding = ActivityLoginViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adjust system bars for immersive UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set Status Bar Color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Back Button Click
        binding.toolBarBackButton.setOnClickListener { finish() }

        // Login Button Click
        binding.loginBtn.setOnClickListener { validateData() }
    }

    // Validates Email and Password Input
    private fun validateData() {
        email = binding.EmailEt.text.toString().trim()
        pass = binding.PasswordEt.text.toString().trim()

        Log.d(TAG, "validateData: Email: $email")
        Log.d(TAG, "validateData: Password: [PROTECTED]")

        when {
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.EmailEt.error = "Invalid Email"
                binding.EmailEt.requestFocus()
            }
            pass.isEmpty() -> {
                binding.PasswordEt.error = "Enter Password"
                binding.PasswordEt.requestFocus()
            }
            else -> {
                loginUser()
            }
        }
    }

    // Logs in user with Firebase Authentication
    private fun loginUser() {
        showProgressDialog()

        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                Log.d(TAG, "loginUser: Login Successful")

                hideProgressDialog()
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                // Navigate to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { exception ->
                hideProgressDialog()

                val errorMessage = when (exception) {
                    is FirebaseAuthInvalidUserException -> "No account found with this email."
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
                    is FirebaseAuthUserCollisionException -> "This email is already in use."
                    is FirebaseAuthWeakPasswordException -> "Your password is too weak."
                    is FirebaseAuthRecentLoginRequiredException -> "Please log in again for security reasons."
                    is FirebaseNetworkException -> "No internet connection. Please try again."
                    else -> "Authentication failed. Please try again."
                }

                Log.e(TAG, "loginUser: ${exception.message}")
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
    }

    // Shows a loading dialog while processing authentication
    private var progressDialog: AlertDialog? = null
    private fun showProgressDialog() {
        val builder = AlertDialog.Builder(this)
            .setCancelable(false) // Prevents user from closing it

        // Create a ProgressBar inside a LinearLayout
        val layout = LinearLayout(this).apply { setPadding(50, 50, 50, 50) }
        layout.addView(ProgressBar(this))

        builder.setView(layout)
        progressDialog = builder.create()
        progressDialog?.show()
    }

    // Hides the loading dialog
    private fun hideProgressDialog() {
        progressDialog?.takeIf { it.isShowing }?.dismiss()
    }
}
