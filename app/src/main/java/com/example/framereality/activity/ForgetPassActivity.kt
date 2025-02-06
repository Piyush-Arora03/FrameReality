package com.example.framereality.activity

import android.os.Bundle
import android.util.Log
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
import com.example.framereality.databinding.ActivityForgetPassBinding
import com.google.firebase.auth.FirebaseAuth

class ForgetPassActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgetPassBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val TAG = "ForgetPassActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflate the layout and set the view
        binding = ActivityForgetPassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets for immersive UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        // Back button action
        binding.ToolBarBackButton.setOnClickListener {
            finish()
        }

        // Handle submit button click to validate and send password reset email
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var email = ""

    /**
     * Validates the email input before proceeding with password reset.
     */
    private fun validateData() {
        email = binding.EmailEt.text.toString().trim()
        Log.d(TAG, "validateData: Email entered: $email")

        if (email.isEmpty()) {
            binding.EmailEt.error = "Email cannot be empty"
            binding.EmailEt.requestFocus()
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.EmailEt.error = "Invalid Email Format"
            binding.EmailEt.requestFocus()
        } else {
            sendPasswordRecoveryInstructions()
        }
    }

    /**
     * Sends a password recovery email using Firebase Authentication.
     */
    private fun sendPasswordRecoveryInstructions() {
        showProgressDialog()
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Log.d(TAG, "sendPasswordRecoveryInstructions: Reset email sent successfully")
                hideProgressDialog()
                Toast.makeText(
                    this, "A password reset email has been sent to $email", Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                Log.e(TAG, "sendPasswordRecoveryInstructions: Failed to send email", e)

                val errorMessage = when (e.message?.contains("badly formatted", ignoreCase = true)) {
                    true -> "Invalid email format. Please enter a valid email address."
                    else -> "Failed to send reset email. Please check the email or try again later."
                }

                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
    }

    // ------------------------------ UI Helper Methods ------------------------------

    private var progressDialog: AlertDialog? = null

    /**
     * Shows a loading dialog while processing authentication.
     */
    private fun showProgressDialog() {
        val builder = AlertDialog.Builder(this).setCancelable(false)

        val layout = LinearLayout(this).apply {
            setPadding(50, 50, 50, 50)
        }
        layout.addView(ProgressBar(this))

        builder.setView(layout)
        progressDialog = builder.create()
        progressDialog?.show()
    }

    /**
     * Hides the loading dialog.
     */
    private fun hideProgressDialog() {
        progressDialog?.takeIf { it.isShowing }?.dismiss()
    }
}
