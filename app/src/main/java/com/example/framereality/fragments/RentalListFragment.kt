package com.example.framereality.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.framereality.PropertyModel
import com.example.framereality.adapter.PropertyHomeAdapter
import com.example.framereality.databinding.FragmentRentalListBinding
import com.google.firebase.database.*

class RentalListFragment : Fragment() {

    private var _binding: FragmentRentalListBinding? = null
    private val binding get() = _binding!!

    private lateinit var propertyList: ArrayList<PropertyModel>
    private lateinit var propertyAdapter: PropertyHomeAdapter
    private lateinit var propertiesRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRentalListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        propertiesRef = FirebaseDatabase.getInstance().getReference("Properties")
        propertyList = ArrayList()

        binding.propertyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        propertyAdapter = PropertyHomeAdapter(requireContext(), propertyList) { property ->
            Toast.makeText(requireContext(), "${property.title} added to favorites", Toast.LENGTH_SHORT).show()
            addToFavorites(property)
        }
        binding.propertyRecyclerView.adapter = propertyAdapter

        fetchPropertiesForRent()
    }

    private fun fetchPropertiesForRent() {
        propertiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                propertyList.clear()
                for (propertySnapshot in snapshot.children) {
                    val property = propertySnapshot.getValue(PropertyModel::class.java)
                    if (property != null && property.purpose == "Rent") {  // Filter "Rent" properties
                        val imageUrls = ArrayList<String>()
                        val imagesSnapshot = propertySnapshot.child("Images")
                        for (imageChild in imagesSnapshot.children) {
                            val imageUrl = imageChild.child("imageUrl").getValue(String::class.java)
                            if (!imageUrl.isNullOrEmpty()) {
                                imageUrls.add(imageUrl)
                            }
                        }
                        val propertyWithImages = property.copy(imageUrls = imageUrls)
                        propertyList.add(propertyWithImages)
                    }
                }
                propertyAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load properties: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun addToFavorites(property: PropertyModel) {
        val userId = "sampleUserId" // Replace with actual user id from FirebaseAuth if available.
        val favRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId)
        favRef.child(property.id).setValue(property)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "${property.title} added to favorites", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Failed to add favorite: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
