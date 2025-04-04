package com.example.framereality.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.framereality.PropertyModel
import com.example.framereality.R
import com.example.framereality.adapter.PropertyHomeAdapter
import com.example.framereality.databinding.FragmentHomeBinding
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val TAG = "HomeFragment"

    private lateinit var originalList: ArrayList<PropertyModel>
    private lateinit var propertyAdapter: PropertyHomeAdapter
    private lateinit var propertiesRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        propertiesRef = FirebaseDatabase.getInstance().getReference("Properties")
        originalList = ArrayList()

        binding.propertyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        propertyAdapter = PropertyHomeAdapter(requireContext()) { property ->
            addToFavorites(property)
            Toast.makeText(requireContext(), "${property.title} added to favorites", Toast.LENGTH_SHORT).show()
        }
        binding.propertyRecyclerView.adapter = propertyAdapter

        fetchPropertiesForSale()

        val searchVw = requireActivity().findViewById<SearchView>(R.id.searchView)
        searchVw.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterPropertiesByCity(newText.orEmpty())
                return true
            }
        })
    }

    private fun fetchPropertiesForSale() {
        propertiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                originalList.clear()
                for (propertySnapshot in snapshot.children) {
                    val property = propertySnapshot.getValue(PropertyModel::class.java)
                    if (property != null && property.purpose == "Sell") {
                        val imageUrls = ArrayList<String>()
                        val imagesSnapshot = propertySnapshot.child("Images")
                        for (imageChild in imagesSnapshot.children) {
                            val imageUrl = imageChild.child("imageUrl").getValue(String::class.java)
                            if (!imageUrl.isNullOrEmpty()) {
                                imageUrls.add(imageUrl)
                            }
                        }
                        val propertyWithImages = property.copy(imageUrls = imageUrls)
                        originalList.add(propertyWithImages)
                    }
                }
                propertyAdapter.updateData(originalList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load properties: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addToFavorites(property: PropertyModel) {
        val userId = "sampleUserId" // Replace with FirebaseAuth UID
        val favRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId)
        favRef.child(property.id).setValue(property)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "${property.title} added to favorites", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Failed to add favorite: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterPropertiesByCity(query: String) {
        Log.d(TAG, "Filtering with query: $query")

        val filteredList = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter { property ->
                val city = property.address ?: ""
                val matches = city.contains(query, ignoreCase = true)
                Log.d(TAG, "Matched City: $city")
                matches
            }
        }
        propertyAdapter.updateData(ArrayList(filteredList))
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
