package com.example.framereality.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.framereality.MyUtils
import com.example.framereality.R
import com.example.framereality.activity.MainActivity
import com.example.framereality.activity.PostAddActivity
import com.example.framereality.activity.ProfileEditActivity
import com.example.framereality.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    // View binding for the fragment's layout.
    private lateinit var binding: FragmentProfileBinding

    // Tag for logging.
    private val TAG = "PROFILE_TAG"

    // Context instance to be initialized when fragment attaches to an activity.
    private lateinit var mContext: Context

    // Called when the fragment is attached to a context (activity).
    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    // ProgressDialog used to indicate loading state.
    private lateinit var progressDialog: ProgressDialog

    // Firebase Authentication instance.
    private lateinit var firebaseAuth: FirebaseAuth

    // Inflate the layout for this fragment using view binding.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Called after the view is created. Initialize UI components and listeners.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize and configure the progress dialog.
        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        // Initialize Firebase Authentication.
        firebaseAuth = FirebaseAuth.getInstance()

        // Load current user's information from Firebase.
        loadMyInfo()

        //handle postAdBtn click, start PostAddActivity
        binding.postAdBtn.setOnClickListener {
            val intent = Intent(mContext,PostAddActivity::class.java)
            startActivity(intent)
        }

        // Set up logout click listener:
        // On click, sign out, navigate to MainActivity, and clear the activity stack.
        binding.logoutCv.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(mContext, MainActivity::class.java))
            activity?.finishAffinity()
        }

        // Set up edit profile click listener:
        // On click, start the ProfileEditActivity.
        binding.editProfileCv.setOnClickListener {
            startActivity(Intent(mContext, ProfileEditActivity::class.java))
        }
        binding.postAdBtn.setOnClickListener{
            startActivity(Intent(mContext, PostAddActivity::class.java))
        }
    }

    // Load user information from Firebase Realtime Database.
    private fun loadMyInfo() {
        // Get a reference to the "Users" node.
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        // Access the current user's node using their UID.
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange: ")
                    // Retrieve various user information from the snapshot.
                    val dob = "${snapshot.child("dob").value}"
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    var timestamp = "${snapshot.child("timestamp").value}"
                    val userType = "${snapshot.child("userType").value}"

                    // Concatenate phone code and number.
                    val phone = phoneCode + phoneNumber

                    // If timestamp is null, set it to "0" as a default value.
                    if (timestamp == null) {
                        timestamp = "0"
                    }

                    // Format the timestamp into a human-readable date using a utility function.
                    val formattedDate = MyUtils.formatTimestampDate(timestamp.toLong())

                    // Update the UI with the fetched data.
                    binding.emailTv.text = email
                    binding.fullNameTv.text = name
                    binding.dobTv.text = dob
                    binding.phoneTv.text = phone
                    binding.memberSinceTv.text = formattedDate

                    // Check email verification status for email-based users.
                    if (userType == MyUtils.USER_TYPE_EMAIL) {
                        val isVerified = firebaseAuth.currentUser!!.isEmailVerified
                        Log.d(TAG, "onDataChange: isVerified: $isVerified")
                        if (isVerified) {
                            // If verified, hide the verify account section.
                            binding.verifyAccountCv.visibility = View.GONE
                            binding.verificationTv.text = "Verified"
                        } else {
                            // If not verified, show the verify account section.
                            binding.verifyAccountCv.visibility = View.VISIBLE
                            binding.verificationTv.text = "Not Verified"
                        }
                    } else {
                        // For other user types, hide the verify account section.
                        binding.verifyAccountCv.visibility = View.GONE
                        binding.verificationTv.text = "Verified"
                    }

                    // Load the user's profile image using Glide, with a placeholder if needed.
                    try {
                        Glide.with(mContext)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.baseline_person_black)
                            .circleCrop()
                            .into(binding.profileIv)
                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // TODO: Handle potential errors (e.g., log or show a message to the user).
                    // Not yet implemented.
                }
            })
    }
}
