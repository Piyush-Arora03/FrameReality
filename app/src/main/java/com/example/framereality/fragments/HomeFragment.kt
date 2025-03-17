package com.example.framereality.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.framereality.PropertyModel
import com.example.framereality.adapter.PropertyHomeAdapter
import com.example.framereality.databinding.FragmentHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // List for home display using the full model.
    private lateinit var propertyList: ArrayList<PropertyModel>
    private lateinit var propertyHomeAdapter: PropertyHomeAdapter
    private lateinit var propertiesRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Initialize the Firebase reference for "Properties".
        propertiesRef = FirebaseDatabase.getInstance().getReference("Properties")
        propertyList = ArrayList()

        binding.propertyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        propertyHomeAdapter = PropertyHomeAdapter(requireContext(), propertyList) { property ->
            // When favorite button is clicked, add the property to Favorites.
            addToFavorites(property)
        }
        binding.propertyRecyclerView.adapter = propertyHomeAdapter

        fetchProperties()
    }

    private fun fetchProperties() {
        propertiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                propertyList.clear()
                for (propertySnapshot in snapshot.children) {
                    val property = propertySnapshot.getValue(PropertyModel::class.java)
                    if (property != null) {
                        // Extract image URLs from the "Images" child node.
                        val imageUrls = ArrayList<String>()
                        val imagesSnapshot = propertySnapshot.child("Images")
                        for (imageChild in imagesSnapshot.children) {
                            val imageUrl = imageChild.child("imageUrl").getValue(String::class.java)
                            if (!imageUrl.isNullOrEmpty()) {
                                imageUrls.add(imageUrl)
                            }
                        }
                        // Create a new property with the extracted image URLs.
                        val propertyWithImages = property.copy(imageUrls = imageUrls)
                        propertyList.add(propertyWithImages)
                    }
                }
                propertyHomeAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load properties: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
