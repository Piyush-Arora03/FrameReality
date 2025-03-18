package com.example.framereality.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.framereality.PropertyModel
import com.example.framereality.adapter.PropertyFavouriteAdapter
import com.example.framereality.databinding.FragmentFavouriteListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavouriteListFragment : Fragment() {

    private var _binding: FragmentFavouriteListBinding? = null
    private val binding get() = _binding!!

    private lateinit var favList: ArrayList<PropertyModel>
    private lateinit var favAdapter: PropertyFavouriteAdapter
    private lateinit var favRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouriteListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        favList = ArrayList()
        val userId = "sampleUserId" // Replace with actual user id from FirebaseAuth if available.
        favRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId)

        binding.favRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        favAdapter = PropertyFavouriteAdapter(requireContext(), favList) { property ->
            // Optionally, remove the favorite when the remove button is clicked.
            removeFromFavorites(property)
        }
        binding.favRecyclerView.adapter = favAdapter

        fetchFavorites()
    }

    private fun fetchFavorites() {
        favRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favList.clear()
                for (favSnapshot in snapshot.children) {
                    val property = favSnapshot.getValue(PropertyModel::class.java)
                    if (property != null) {
                        Log.d("Fav", "Property ${property.id} imageUrls: ${property.imageUrls}")
                        favList.add(property)
                    }
                }
                favAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load favorites: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun removeFromFavorites(property: PropertyModel) {
        val userId = "sampleUserId" // Replace with actual user id if available.
        val favRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId)
        favRef.child(property.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "${property.title} removed from favorites", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Failed to remove favorite: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
