package com.example.framereality.activity

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.Menu
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.framereality.MyUtils
import com.example.framereality.R
import com.example.framereality.databinding.ActivityProfileEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: AlertDialog
    private val TAG = "ProfileEditActivity"
    private var mUserType = ""
    private var imageUri:Uri?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets for proper padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Authentication instance
        firebaseAuth = FirebaseAuth.getInstance()

        // Handle back button click
        binding.ToolBarBackButton.setOnClickListener { finish() }

        // Load user profile data from Firebase
        loadMyInfo()
        binding.imagePickerBtn.setOnClickListener{
            imagePickDialog()
        }

        binding.updateBtn.setOnClickListener{
            validateData()
        }
    }
    private var name = ""
    private var dob = ""
    private var email = ""
    private var phoneCode = ""
    private var phoneNumber = ""

    // Validate user input and initiate profile update
    private fun validateData() {
        name = binding.nameEt.text.toString().trim()
        dob = binding.dobEt.text.toString().trim()
        email = binding.EmailEt.text.toString().trim()
        phoneCode = binding.PhoneCodeEt.selectedCountryCodeWithPlus
        phoneNumber = binding.PhoneEt.text.toString().trim()

        if (imageUri == null) {
            updateProfileDB(null)
        } else {
            uploadProfileImageStorage()
        }
    }

    // Upload image to Firebase Storage
    private fun uploadProfileImageStorage() {
        Log.d(TAG, "Uploading Profile Image")
//        showProgressDialog("Uploading Profile Image")

        val filePathAndName = "UserImages/${firebaseAuth.uid}"
        val ref = FirebaseStorage.getInstance().reference.child(filePathAndName)
        ref.putFile(imageUri!!)
            .addOnProgressListener { snapshot ->
                val progress = 100 * snapshot.bytesTransferred / snapshot.totalByteCount
                Log.d(TAG, "Uploading Profile Image: Progress ${progress.toInt()}%")
                showProgressDialog("Progress: ${progress.toInt()}%")
            }
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    updateProfileDB(uri.toString())
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Error uploading profile image: ${it.message}")
                hideProgressDialog()
                Toast.makeText(this, "Error uploading profile image: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Update user profile in Firebase Realtime Database
    private fun updateProfileDB(imageUrl: String?) {
        showProgressDialog("Updating user info...")
        val hashMap = HashMap<String, Any>().apply {
            put("name", name)
            put("dob", dob)
            imageUrl?.let { put("profileImageUrl", it) }
            if (mUserType == MyUtils.USER_TYPE_EMAIL || mUserType == MyUtils.USER_TYPE_GOOGLE) {
                put("phoneCode", phoneCode)
                put("phoneNumber", phoneNumber)
            } else {
                put("email", email)
            }
        }
        FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "Profile updated successfully")
                hideProgressDialog()
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.e(TAG, "Error updating profile: ${it.message}")
                hideProgressDialog()
                Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
            }
    }

    // Show image selection dialog
    private fun imagePickDialog() {
        val popupMenu = PopupMenu(this, binding.imagePickerBtn).apply {
            menu.add(Menu.NONE, 1, 1, "Camera")
            menu.add(Menu.NONE, 2, 2, "Gallery")
            show()
        }
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> requestCameraPermissions()
                2 -> pickImageGallery()
            }
            true
        }
    }

    // Launch gallery intent
    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        galleryActivityResultLauncher.launch(intent)
    }

    // Handle gallery result
    private val galleryActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.baseline_person_black)
                .into(binding.profileIV)
        } else {
            Toast.makeText(this, "Image selection canceled", Toast.LENGTH_SHORT).show()
        }
    }

    // Request camera permissions
    private fun requestCameraPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestCameraPermissionsLauncher.launch(permissions)
    }

    // Handle camera permissions result
    private val requestCameraPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.all { it.value }) {
            pickImageCamera()
        } else {
            Toast.makeText(this, "Camera permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Launch camera intent
    private fun pickImageCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "Temp_Title")
            put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")
        }
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, imageUri) }
        cameraActivityResultLauncher.launch(intent)
    }

    // Handle camera result
    private val cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.baseline_person_black)
                .into(binding.profileIV)
        } else {
            Toast.makeText(this, "Camera capture canceled", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Loads user information from Firebase Realtime Database.
     */
    private fun loadMyInfo() {
        showProgressDialog("Loading Your info..")
        Log.d(TAG, "Loading user info...")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Retrieve user details from Firebase
                val dob = snapshot.child("dob").getValue(String::class.java) ?: "N/A"
                val name = snapshot.child("name").getValue(String::class.java) ?: "N/A"
                val email = snapshot.child("email").getValue(String::class.java) ?: "N/A"
                val phoneCode = snapshot.child("phoneCode").getValue(String::class.java) ?: ""
                val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java) ?: "N/A"
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java) ?: ""
                mUserType = snapshot.child("userType").getValue(String::class.java) ?: ""

                // Disable input fields based on user type
                if (mUserType == MyUtils.USER_TYPE_PHONE) {
                    binding.PhoneEt.isEnabled = false
                    binding.PhoneTil.isEnabled = false
                    binding.PhoneCodeEt.isEnabled = false
                } else {
                    binding.EmailEt.isEnabled = false
                    binding.EmailTil.isEnabled = false
                }

                // Populate UI with retrieved data
                binding.nameEt.setText(name)
                binding.EmailEt.setText(email)
                binding.PhoneEt.setText(phoneNumber)
                binding.dobEt.setText(dob)

                // Set phone code if valid
                try {
                    val phoneCodeInt = phoneCode.replace("+", "").toInt()
                    binding.PhoneCodeEt.setCountryForPhoneCode(phoneCodeInt)
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting phone code: ${e.message}")
                }

                // Load profile image using Glide
                try {
                    Glide.with(this@ProfileEditActivity)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.baseline_person_black)
                        .into(binding.profileIV)
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading profile image: ${e.message}")
                }
                hideProgressDialog()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
                hideProgressDialog()
            }
        })
    }

    /**
     * Displays a progress dialog while processing user actions.
     */
    private fun showProgressDialog(msg : String) {
        val builder = AlertDialog.Builder(this).setCancelable(false)
        val layout = LinearLayout(this).apply { setPadding(50, 50, 50, 50) }
        layout.addView(ProgressBar(this))
        builder.setView(layout)
            .setMessage(msg)
        progressDialog = builder.create()
        progressDialog.show()
    }

    /**
     * Hides the progress dialog if it is currently showing.
     */
    private fun hideProgressDialog() {
        progressDialog.takeIf { it.isShowing }?.dismiss()
    }
}
