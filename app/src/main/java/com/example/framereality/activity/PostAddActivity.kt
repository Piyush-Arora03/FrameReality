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
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.framereality.AdapterImagePicked
import com.example.framereality.ModelImagePicked
import com.example.framereality.MyUtils
import com.example.framereality.databinding.ActivityPostAddBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class PostAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostAddBinding
    private val TAG = "PostAddActivity"

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri:Uri? = null

    private var adapterPropertySubcategory: ArrayAdapter<String>? = null

    private lateinit var imagePickedArrayList: ArrayList<ModelImagePicked>

    private lateinit var adapterImagePicked : AdapterImagePicked

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPostAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...!")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set adapter for property size dropdown
        val areaSizeAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MyUtils.propertyAreaSizeUnit)
        binding.areaSizeUnitACTV.setAdapter(areaSizeAdapter)

        imagePickedArrayList = ArrayList()
        loadImages()

        setupTabs()
        propertyCategoryHome() // Set default category



        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)
            purpose = radioButton.text.toString()
            Log.d(TAG, "Purpose: $purpose")
        }

        binding.pickImagesTV.setOnClickListener {
            showImagePickOptions()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private fun setupTabs() {
        val tabLayout = binding.propertyCategoryTabLayout


        // Handle tab selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        category = MyUtils.propertyTypes[0]
                        propertyCategoryHome()
                    }
                    1 -> {
                        category = MyUtils.propertyTypes[1]
                        propertyCategoryPlot()
                    }
                    2 -> {
                        category = MyUtils.propertyTypes[2]
                        propertyCategoryCommercial()
                    }
                }
                Log.d(TAG, "Category: $category")
                binding.propertySubcategoryACTV.setAdapter(adapterPropertySubcategory)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun propertyCategoryHome() {
        // For the Home tab, show fields and use the Homes array.
        showFields(true)
        adapterPropertySubcategory = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            MyUtils.propertyTypesHomes
        )
        binding.propertySubcategoryACTV.setText("")
    }

    private fun propertyCategoryPlot() {
        // For the Plot tab, hide fields and use the Plots array.
        showFields(false)
        adapterPropertySubcategory = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            MyUtils.propertyTypesPlots
        )
        binding.propertySubcategoryACTV.setText("")
    }

    private fun propertyCategoryCommercial() {
        // For the Commercial tab, hide fields and use the Commercial array.
        showFields(false)
        adapterPropertySubcategory = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            MyUtils.propertyTypesCommercial
        )
        binding.propertySubcategoryACTV.setText("")
    }

    private fun showFields(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        binding.floorsTIL.visibility = visibility
        binding.bedRoomsTIL.visibility = visibility
        binding.bathRoomsTIL.visibility = visibility
    }

    private fun showImagePickOptions(){
        Log.d(TAG, "showImagePickOptions: ")

        val popupMenu = PopupMenu(this,binding.pickImagesTV)

        popupMenu.menu.add(Menu.NONE,1,1,"Camera")
        popupMenu.menu.add(Menu.NONE,2,2,"Gallery")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->

            val itemId = item.itemId
            when(itemId){
                1 -> {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        val permissions = arrayOf(Manifest.permission.CAMERA)
                        requestCameraPermissions.launch(permissions)
                    }
                    else{
                        val permissions = arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        requestCameraPermissions.launch(permissions)
                    }
                }
                2 -> {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        pickImageGallery()
                    }
                    else{
                        val permissions = Manifest.permission.WRITE_EXTERNAL_STORAGE
                        requestStoragePermission.launch(permissions)
                    }
                }
            }

            true
        }
    }

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ isGranted ->
        Log.d(TAG, "requestStoragePermission: isGranted: $isGranted")

        if(isGranted){
            pickImageGallery()
        }
        else{

            MyUtils.toast(this,"Storage Permission denied!")
        }
    }

    private val requestCameraPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Log.d(TAG, "requestCameraPermissions: result: $result")

        var areAllGranted = true
        for(isGranted in result.values){
            areAllGranted = areAllGranted && isGranted
        }

        if(areAllGranted){
            pickImageCamera()
        }
        else{
            MyUtils.toast(this,"Camera Permission denied!")
        }
    }

    private fun pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ")

        val intent = Intent(Intent.ACTION_PICK)

        intent.setType("image/*")
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result->
        Log.d(TAG, "galleryActivityResultLauncher: result: $result")

        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data
            imageUri = data?.data

            Log.d(TAG, "galleryActivityResultLauncher: imageUri: $imageUri")

            val timestamp = "${MyUtils.timestamp()}"

            val modelImagePicked = ModelImagePicked(timestamp,imageUri,null,false)

            imagePickedArrayList.add(modelImagePicked)

            loadImages()
        }
        else{
            MyUtils.toast(this,"Cancelled!")
        }
    }

    private fun pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ")

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE,"TEMP_TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"TEMP_DESCRIPTION")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)

        cameraActivityResultLauncher.launch(intent)

    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "cameraActivityResultLauncher: result: $result")

        if(result.resultCode == Activity.RESULT_OK){
            Log.d(TAG, "cameraActivityResultLauncher: imageUri: $imageUri")

            val timestamp = "${MyUtils.timestamp()}"

            val modelImagePicked = ModelImagePicked(timestamp,imageUri,null,false)

            imagePickedArrayList.add(modelImagePicked)

            loadImages()
        }
        else{
            MyUtils.toast(this,"Cancelled!")
        }
    }

    private fun loadImages() {
        Log.d(TAG, "loadImages: ")
        adapterImagePicked = AdapterImagePicked(this,imagePickedArrayList)

        binding.recyclerPhotos.adapter = adapterImagePicked
    }

    private var purpose = MyUtils.PROPERTY_TYPE_SELL
    private var category = MyUtils.propertyTypes[0]
    private var subcategory = ""
    private var floors = ""
    private var bedRooms = ""
    private var bathRooms = ""
    private var areaSize = ""
    private var areaSizeUnit = ""
    private var price = ""
    private var title = ""
    private var description = ""
    private var email = ""
    private var phoneCode = ""
    private var phoneNumber = ""
    private var country = ""
    private var city = ""
    private var address = ""
    private var latitude = ""
    private var longitude = ""

    private fun validateData(){
        Log.d(TAG, "validateData: ")

        subcategory = binding.propertySubcategoryACTV.text.toString().trim()
        floors = binding.floorsEt.text.toString().trim()
        bedRooms = binding.bedRoomsET.text.toString().trim()
        bathRooms = binding.bathRoomsET.text.toString().trim()
        areaSize = binding.areaSizeET.text.toString().trim()
        areaSizeUnit = binding.areaSizeUnitACTV.text.toString().trim()
        address = binding.locationET.text.toString().trim()
        price = binding.priceET.text.toString().trim()
        title = binding.titleET.text.toString().trim()
        description = binding.descriptionET.text.toString().trim()
        email = binding.emailET.text.toString().trim()
        phoneCode = binding.phoneCodeET.selectedCountryCodeWithPlus
        phoneNumber = binding.phoneNumberEt.text.toString().trim()

        if(subcategory.isEmpty()){
            binding.propertySubcategoryACTV.error = "Choose Subcategory...!"
            binding.propertySubcategoryACTV.requestFocus()
        }
        else if(category == MyUtils.propertyTypes[0] && floors.isEmpty()){
            binding.floorsEt.error = "Enter floors count...!"
            binding.floorsEt.requestFocus()
        }
        else if(category == MyUtils.propertyTypes[0] && bedRooms.isEmpty()){
            binding.bedRoomsET.error = "Enter Bed Rooms count...!"
            binding.bedRoomsET.requestFocus()
        }
        else if(category == MyUtils.propertyTypes[0] && bathRooms.isEmpty()){
            binding.bathRoomsET.error = "Enter Bath Rooms count...!"
            binding.bathRoomsET.requestFocus()
        }
        else if(areaSize.isEmpty()){
            binding.areaSizeET.error = "Enter Area Size...!"
            binding.areaSizeET.requestFocus()
        }
        else if(areaSizeUnit.isEmpty()){
            binding.areaSizeUnitACTV.error = "Enter Area Size Unit...!"
            binding.areaSizeUnitACTV.requestFocus()
        }
        else if(address.isEmpty()){
            binding.locationET.error = "Pick Location...!"
            binding.locationET.requestFocus()
        }
//        else if(areaSizeUnit.isEmpty()){
//            binding.areaSizeUnitACTV.error = "Enter Area Size Unit...!"
//            binding.areaSizeUnitACTV.requestFocus()
//        }
        else if(price.isEmpty()){
            binding.priceET.error = "Enter Price...!"
            binding.priceET.requestFocus()
        }
        else if(title.isEmpty()){
            binding.titleET.error = "Enter Title...!"
            binding.titleET.requestFocus()
        }
        else if(description.isEmpty()){
            binding.descriptionET.error = "Enter Description...!"
            binding.descriptionET.requestFocus()
        }
        else if(phoneNumber.isEmpty()){
            binding.phoneNumberEt.error = "Enter Phone Number...!"
            binding.phoneNumberEt.requestFocus()
        }
        else if(imagePickedArrayList.isEmpty()){
            MyUtils.toast(this,"Pick at-least one image...!")
        }
        else{
            postAd()
        }
    }
    private fun postAd(){
        Log.d(TAG, "postAd: ")
        progressDialog.setMessage("Publishing Ad")
        progressDialog.show()

        if(floors.isEmpty()){
            floors = "0"
        }

        if(bedRooms.isEmpty()){
            bedRooms = "0"
        }

        if(bathRooms.isEmpty()){
            bathRooms = "0"
        }

        val timestamp = MyUtils.timestamp()

        val refProperties = FirebaseDatabase.getInstance().getReference("Properties")
        val keyId = refProperties.push().key

        val hashMap = HashMap<String,Any>()
        hashMap["id"] =  "$keyId"
        hashMap["uid"] = ""+firebaseAuth.uid
        hashMap["purpose"] = ""+purpose
        hashMap["category"] = ""+category
        hashMap["subcategory"] = ""+subcategory
        hashMap["title"] = ""+title
        hashMap["description"] = ""+description
        hashMap["email"] = ""+email
        hashMap["phoneCode"] = ""+phoneCode
        hashMap["phoneNumber"] = ""+phoneNumber
        hashMap["country"] = ""+country
        hashMap["city"] = ""+city
        hashMap["address"] = ""+address
        hashMap["status"] = ""+MyUtils.AD_STATUS_AVAILABLE
        hashMap["areaSizeUnit"] = ""+areaSizeUnit
        hashMap["floors"] = floors.toLong()
        hashMap["bedrooms"] = bedRooms.toLong()
        hashMap["bathrooms"] = bathRooms.toLong()
        hashMap["price"] = price.toDouble()
        hashMap["timestamp"] = timestamp
        hashMap["latitude"] = latitude
        hashMap["longitude"] = longitude

        refProperties.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                uploadImagesStorage(keyId)
                Log.d(TAG, "postAd: Ad Published")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "postAd: ", e)
                progressDialog.dismiss()
                MyUtils.toast(this,"Failed to publish ad due to ${e.message}")
            }
    }

    private fun uploadImagesStorage(propertyId : String){
        Log.d(TAG, "uploadImagesStorage: propertyId : $propertyId")

        for(i in imagePickedArrayList.indices){
            val modelImagePicked = imagePickedArrayList[i]
            if(!modelImagePicked.isFromInternet){
                val imageName = modelImagePicked.id
                val filePathAndName = "Properties/$imageName"
                val imageIndexForProgress = i+1
                val pickedImageUri = modelImagePicked.localImageUri
                val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
                storageReference.putFile(pickedImageUri!!)
                    .addOnProgressListener { snapshot ->

                        val progress = (100.0 * snapshot.bytesTransferred)/snapshot.totalByteCount
                        val message = "Uploading $imageIndexForProgress of ${imagePickedArrayList.size} images \nProgress: $progress"
                        Log.d(TAG, "uploadImagesStorage: message: $message")
                        progressDialog.setMessage(message)
                        progressDialog.show()
                    }
                    .addOnSuccessListener { taskSnapshot ->
                        Log.d(TAG, "uploadImagesStorage: ")
                        val uriTask = taskSnapshot.storage.downloadUrl
                        while (!uriTask.isSuccessful);
                        val uploadImageUri = uriTask.result

                        if(uriTask.isSuccessful){
                            val hashMap = HashMap<String,Any>()
                            hashMap["id"] = ""+modelImagePicked.id
                            hashMap["imageUrl"] = ""+uploadImageUri

                            val refProperties = FirebaseDatabase.getInstance().getReference("Properties")
                            refProperties.child(propertyId).child("Images").child(imageName)
                                .updateChildren(hashMap)
                        }
                        progressDialog.dismiss()
                    }
                    .addOnFailureListener { e->
                        Log.e(TAG, "uploadImagesStorage: ", e)
                        progressDialog.dismiss()
                        MyUtils.toast(this,"Failed to upload image due to ${e.message}")
                    }
            }
        }
    }
}
