package com.example.framereality.fragment

import ModelItem
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.framereality.adapter.ItemsAdapter
import com.example.framereality.databinding.FragmentGiftBinding
import com.google.firebase.database.*

class ItemsFragment : Fragment() {

    private var _binding: FragmentGiftBinding? = null
    private val binding get() = _binding!!

    private lateinit var itemsAdapter: ItemsAdapter
    private val itemsList = ArrayList<ModelItem>()

    private lateinit var databaseReference: DatabaseReference
    private val TAG = "ItemsFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGiftBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup RecyclerView
        binding.itemsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        itemsAdapter = ItemsAdapter(requireContext(), itemsList)
        binding.itemsRecyclerView.adapter = itemsAdapter

        // Initialize Firebase reference; adjust the node name if needed
        databaseReference = FirebaseDatabase.getInstance().getReference("Items")
        loadItemsFromFirebase()
    }

    private fun loadItemsFromFirebase() {
        // Listen for changes in the "Items" node
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemsList.clear()
                for (itemSnapshot in snapshot.children) {
                    // Assuming each child can be mapped to ModelItem
                    val item = itemSnapshot.getValue(ModelItem::class.java)
                    if (item != null) {
                        Log.d(TAG, "Loaded item: $item")
                        itemsList.add(item)
                    } else {
                        Log.e(TAG, "Failed to map itemSnapshot: ${itemSnapshot.key}")
                    }
                }
                itemsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load items: ${error.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
