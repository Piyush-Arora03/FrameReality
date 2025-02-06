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
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.example.framereality.R
import com.example.framereality.databinding.ActivityForgetPassBinding
import com.google.firebase.auth.FirebaseAuth

class ForgetPassActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgetPassBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val TAG ="ForgetPassActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityForgetPassBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor=ContextCompat.getColor(this,R.color.colorPrimary)
        firebaseAuth=FirebaseAuth.getInstance()
        binding.ToolBarBackButton.setOnClickListener{
            finish()
        }
        binding.submitBtn.setOnClickListener{
            validateData()
        }
    }
    private var email=""
    private fun validateData() {
       email=binding.EmailEt.text.toString().trim()
        Log.d(TAG,"validateData: Email: $email")
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.EmailEt.error="Invalid Email"
            binding.EmailEt.requestFocus()
        }else{
            sendPasswordRecoveryInstructions()
        }
    }

    private fun sendPasswordRecoveryInstructions() {
        showProgressDialog()
        firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener {
            Log.d(TAG,"sendPasswordRecoveryInstructions: Email sent")
            hideProgressDialog()
            Toast.makeText(this,"Email sent to $email",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            hideProgressDialog()
            Log.d(TAG,"sendPasswordRecoveryInstructions: ${it.message}")
            Toast.makeText(this,"failed due to ${it.message}",Toast.LENGTH_SHORT).show()
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