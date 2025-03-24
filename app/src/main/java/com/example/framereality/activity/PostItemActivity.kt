package com.example.framereality.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.framereality.AdapterImagePicked
import com.example.framereality.ModelImagePicked
import com.example.framereality.MyUtils
import com.example.framereality.databinding.ActivityPostItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class PostItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostItemBinding
    private val TAG = "PostItemActivity"

    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri? = null
    private lateinit var imagePickedArrayList: ArrayList<ModelImagePicked>
    private lateinit var adapterImagePicked: AdapterImagePicked

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this).apply {
            setTitle("Please wait...")
            setCanceledOnTouchOutside(false)
        }

        firebaseAuth = FirebaseAuth.getInstance()

        // Adjust for system insets if needed
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize image list and load any previously picked images
        imagePickedArrayList = ArrayList()
        loadImages()

        // Set click listener to pick images
        binding.pickImagesTV.setOnClickListener {
            showImagePickOptions()
        }

        // Set click listener on the submit button to validate data and post to Firebase
        binding.submitBtn.setOnClickListener {
            validateDataAndPost()
        }
    }

    // Validate input data before posting the item
    private fun validateDataAndPost() {
        val title = binding.titleET.text.toString().trim()
        val city = binding.cityET.text.toString().trim()
        val description = binding.descriptionET.text.toString().trim()
        val priceText = binding.priceET.text.toString().trim()

        if (title.isEmpty()) {
            binding.titleET.error = "Enter Title"
            binding.titleET.requestFocus()
            return
        }
        if (city.isEmpty()) {
            binding.cityET.error = "Enter City"
            binding.cityET.requestFocus()
            return
        }
        if (description.isEmpty()) {
            binding.descriptionET.error = "Enter Description"
            binding.descriptionET.requestFocus()
            return
        }
        if (priceText.isEmpty()) {
            binding.priceET.error = "Enter Price"
            binding.priceET.requestFocus()
            return
        }
        if (imagePickedArrayList.isEmpty()) {
            MyUtils.toast(this, "Pick at least one image!")
            return
        }

        val isFavourite = false
        postItem(title, city, description, priceText.toDouble(), isFavourite)
    }

    // Post the item data to Firebase Realtime Database
    private fun postItem(
        title: String,
        city: String,
        description: String,
        price: Double,
        isFavourite: Boolean
    ) {
        progressDialog.setMessage("Posting item...")
        progressDialog.show()

        val timestamp = MyUtils.timestamp()
        val refItems = FirebaseDatabase.getInstance().getReference("Items")
        val keyId = refItems.push().key

        val hashMap = hashMapOf<String, Any>(
            "id" to (keyId ?: ""),
            "uid" to (firebaseAuth.uid ?: ""),
            "title" to title,
            "city" to city,
            "description" to description,
            "price" to price,
            "timestamp" to timestamp,
            "isFavourite" to isFavourite
        )

        refItems.child(keyId!!).setValue(hashMap)
            .addOnSuccessListener {
                uploadImagesToFirebase(keyId)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.e(TAG, "postItem: ", e)
                MyUtils.toast(this, "Failed to post item: ${e.message}")
            }
    }

    // Upload picked images to Firebase Storage
    private fun uploadImagesToFirebase(itemId: String) {
        for (i in imagePickedArrayList.indices) {
            val modelImagePicked = imagePickedArrayList[i]
            if (!modelImagePicked.isFromInternet) {
                val imageName = modelImagePicked.id
                val filePath = "Items/$imageName"
                val storageReference = FirebaseStorage.getInstance().getReference(filePath)
                storageReference.putFile(modelImagePicked.localImageUri!!)
                    .addOnProgressListener { snapshot ->
                        val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
                        progressDialog.setMessage("Uploading image ${i + 1} of ${imagePickedArrayList.size}: ${progress.toInt()}%")
                    }
                    .addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                            val hashMap = hashMapOf<String, Any>(
                                "id" to imageName,
                                "imageUrl" to downloadUri.toString()
                            )
                            FirebaseDatabase.getInstance().getReference("Items")
                                .child(itemId)
                                .child("Images")
                                .child(imageName)
                                .setValue(hashMap)
                                .addOnSuccessListener {
                                    if (i == imagePickedArrayList.size - 1) {
                                        progressDialog.dismiss()
                                        MyUtils.toast(this, "Item posted successfully!")
                                    }
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Log.e(TAG, "uploadImagesToFirebase: ", e)
                        MyUtils.toast(this, "Failed to upload image: ${e.message}")
                    }
            }
        }
    }

    // Display a popup menu for picking image options
    private fun showImagePickOptions() {
        val popupMenu = PopupMenu(this, binding.pickImagesTV)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    // Check and request CAMERA permissions before picking image from Camera
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // On Tiramisu and above, only CAMERA permission is required.
                        requestCameraPermissions.launch(arrayOf(Manifest.permission.CAMERA))
                    } else {
                        // For older versions, request CAMERA and WRITE_EXTERNAL_STORAGE permissions.
                        requestCameraPermissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    }
                }
                2 -> {
                    // Pick image from Gallery
                    pickImageFromGallery()
                }
            }
            true
        }
    }

    // Camera permission request using ActivityResultContracts
    private val requestCameraPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Log.d(TAG, "requestCameraPermissions: result: $result")
        var areAllGranted = true
        for (isGranted in result.values) {
            areAllGranted = areAllGranted && isGranted
        }
        if (areAllGranted) {
            pickImageFromCamera()
        } else {
            MyUtils.toast(this, "Camera Permission denied!")
        }
    }

    // Launch gallery intent
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        galleryActivityResultLauncher.launch(intent)
    }

    // Handle gallery result
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            imageUri = data?.data
            Log.d(TAG, "Gallery imageUri: $imageUri")
            val timestamp = MyUtils.timestamp()
            val modelImagePicked = ModelImagePicked(timestamp.toString(), imageUri, null, false)
            imagePickedArrayList.add(modelImagePicked)
            loadImages()
        } else {
            MyUtils.toast(this, "Image selection cancelled")
        }
    }

    // Launch camera intent
    private fun pickImageFromCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "TEMP_TITLE")
            put(MediaStore.Images.Media.DESCRIPTION, "TEMP_DESCRIPTION")
        }
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        cameraActivityResultLauncher.launch(intent)
    }

    // Handle camera result
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Camera imageUri: $imageUri")
            val timestamp = MyUtils.timestamp()
            val modelImagePicked = ModelImagePicked(timestamp.toString(), imageUri, null, false)
            imagePickedArrayList.add(modelImagePicked)
            loadImages()
        } else {
            MyUtils.toast(this, "Camera action cancelled")
        }
    }

    // Load images into RecyclerView using AdapterImagePicked
    private fun loadImages() {
        adapterImagePicked = AdapterImagePicked(this, imagePickedArrayList)
        binding.recyclerPhotos.layoutManager = StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerPhotos.adapter = adapterImagePicked
    }
}
