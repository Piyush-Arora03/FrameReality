package com.example.framereality.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.JsonToken
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.framereality.MyUtils
import com.example.framereality.R
import com.example.framereality.databinding.ActivityLoginOptionsBinding
import com.example.framereality.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginOptionsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginOptionsBinding

    private val TAG = "LOGIN_OPTIONS_TAG"

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var mGoogleSignInClient: GoogleSignInClient



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)

        binding.skipBtn.setOnClickListener {
            finish()
        }

        binding.loginGoogleBtn.setOnClickListener {
            beginLoginBtn()
        }
    }

    private fun beginLoginBtn(){
        Log.d(TAG, "beginLoginBtn: ")
        val googleSignInIntent = mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInIntent)
    }

    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "googleSignInARL: AccountID: ${account.id}")
                firebaseAuthWithGoogleAccount(account.idToken)
            } catch (e : Exception){
                Log.e(TAG, "googleSignInARL: ", e)
            }
        }
        else{
            Log.d(TAG, "googleSignInARL: Cancelled...!")

            MyUtils.toast(this,"Cancelled...!")
        }
    }
    private fun firebaseAuthWithGoogleAccount(idToken: String?){
        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken : $idToken")
        val credential = GoogleAuthProvider.getCredential(idToken,null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                if(authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Account Created...!")
                    updateUserInfoDb()
                }
                else{
                    startActivity(Intent(this,MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener { e->
                Log.e(TAG, "firebaseAuthWithGoogleAccount: ",e)
                MyUtils.toast(this,"Failed signin due to  ${e.message}")
            }
    }

    private fun updateUserInfoDb(){
        Log.d(TAG, "updateUserInfoDb: ")
        progressDialog.setMessage("Saving user info...!")
        progressDialog.show()

        val timestamp =MyUtils.timestamp()
        val registeredUserUid = "${firebaseAuth.uid}"
        val registeredUserEmail = "${firebaseAuth.currentUser!!.email}"
        val name = "${firebaseAuth.currentUser!!.displayName}"

        val hashMap = HashMap<String,Any>()
        hashMap["uid"] = registeredUserUid
        hashMap["email"] = registeredUserEmail
        hashMap["name"] = name
        hashMap["timestamp"] = timestamp
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = MyUtils.USER_TYPE_GOOGLE
        hashMap["token"] = ""

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(registeredUserUid)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserInfoDb: User Info saved...!")
                progressDialog.dismiss()
                startActivity(Intent(this,MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e->
                Log.e(TAG, "updateUserInfoDb: ", e)
                progressDialog.dismiss()
                MyUtils.toast(this,"Failed to save due to ${e.message}")
            }
    }
}