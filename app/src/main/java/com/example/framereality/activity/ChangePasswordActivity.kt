package com.example.framereality.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.framereality.MyUtils
import com.example.framereality.R
import com.example.framereality.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding:ActivityChangePasswordBinding

    private val TAG = "CHANGE_PASSWORD_TAG"

    private lateinit var firebaseAuth: FirebaseAuth

    private var firebaseUser:FirebaseUser? = null

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackBtn.setOnClickListener {
            finish()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var currentPassword = ""
    private var newPassword = ""
    private var confirmNewPassword = ""

    private fun validateData(){

        Log.d(TAG, "validateData: Validating Data...")

        currentPassword = binding.currentPasswordEt.text.toString()
        newPassword = binding.newPasswordEt.text.toString()
        confirmNewPassword = binding.confirmNewPasswordEt.text.toString()

        Log.d(TAG, "validateData: currentPassword: $currentPassword")
        Log.d(TAG, "validateData: newPassword: $newPassword")
        Log.d(TAG, "validateData: confirmNewPassword: $confirmNewPassword")

        if(currentPassword.isEmpty()){
            binding.currentPasswordEt.error = "Enter current password"
            binding.currentPasswordEt.requestFocus()
        }
        else if(newPassword.isEmpty()){
            binding.newPasswordEt.error = "Enter new password"
            binding.currentPasswordEt.requestFocus()
        }
        else if(confirmNewPassword.isEmpty()){
            binding.confirmNewPasswordEt.error = "Enter confirm new password"
            binding.confirmNewPasswordEt.requestFocus()
        }
        else if(newPassword != confirmNewPassword){
            binding.confirmNewPasswordEt.error = "Password doesn't match"
            binding.confirmNewPasswordEt.requestFocus()
        }
        else{
            authenticateUserForUpdatePassword()
        }
    }

    private fun authenticateUserForUpdatePassword(){
        Log.d(TAG, "authenticateUserForUpdatePassword: Re-authenticating User...")

        progressDialog.setMessage("Authenticating User...")
        progressDialog.show()


        val authCredential = EmailAuthProvider.getCredential(firebaseUser!!.email!!,currentPassword)
        firebaseUser!!.reauthenticate(authCredential)
            .addOnSuccessListener {
                Log.d(TAG, "authenticateUserForUpdatePassword: Authentication success")
                updatePassword()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "authenticateUserForUpdatePassword: ", e)
                progressDialog.dismiss()
                MyUtils.toast(this,"Failed to authenticate due to ${e.message}")
            }
    }

    private fun updatePassword(){
        Log.d(TAG, "updatePassword: Updating Password...")

        progressDialog.setMessage("Updating Password...")
        progressDialog.show()

        firebaseUser!!.updatePassword(newPassword)
            .addOnSuccessListener {
                Log.d(TAG, "updatePassword: Password updated...")
                progressDialog.dismiss()
                MyUtils.toast(this,"Password updated...")
            }
            .addOnFailureListener { e->
                Log.e(TAG, "updatePassword: ", e)
                progressDialog.dismiss()
                MyUtils.toast(this,"Failed to update password due to ${e.message}")
            }
    }
}