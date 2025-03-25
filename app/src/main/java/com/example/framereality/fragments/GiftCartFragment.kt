package com.example.framereality.fragments

import ModelItem
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.framereality.ModelImage
import com.example.framereality.adapter.ItemsAdapter
import com.example.framereality.databinding.FragmentGiftCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GiftCartFragment : Fragment() {

    private var _binding: FragmentGiftCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: ItemsAdapter
    private val cartList = ArrayList<ModelItem>()

    private lateinit var databaseReference: DatabaseReference
    private val TAG = "CartFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGiftCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cartAdapter = ItemsAdapter(requireContext(), cartList)
        binding.cartRecyclerView.adapter = cartAdapter

        // Get Firebase Reference to Cart
        val userId = FirebaseAuth.getInstance().uid ?: return
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Cart")

        loadCartItems()
    }

    private fun loadCartItems() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartList.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(ModelItem::class.java)

                    // ✅ Manually extract 'images' field
                    val imagesSnapshot = itemSnapshot.child("images")
                    val imagesMap: MutableMap<String, ModelImage> = mutableMapOf()

                    for (imageEntry in imagesSnapshot.children) {
                        val modelImage = imageEntry.getValue(ModelImage::class.java)
                        if (modelImage != null) {
                            imagesMap[imageEntry.key!!] = modelImage
                        }
                    }
                    if (item != null) {
                        item.Images = imagesMap  // ✅ Manually set images
                        Log.d(TAG, "Loaded cart item: $item")
                        cartList.add(item)
                    } else {
                        Log.e(TAG, "Failed to map itemSnapshot: ${itemSnapshot.key}")
                    }
                }
                cartAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load cart items: ${error.message}")
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}