package com.example.framereality.adapter

import ModelItem
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.framereality.R
import com.example.framereality.databinding.RowItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ItemsAdapter(
    private val context: Context,
    private val itemsList: ArrayList<ModelItem>
) : RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: RowItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = RowItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun getItemCount(): Int = itemsList.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemsList[position]
        val itemRef = FirebaseDatabase.getInstance().getReference("Items").child(item.id)
        val cartRef = FirebaseDatabase.getInstance().getReference("Users")
            .child(FirebaseAuth.getInstance().uid ?: "")
            .child("Cart")

        holder.binding.apply {
            // Bind text fields
            titleTV.text = item.title
            cityTV.text = item.city
            descriptionTV.text = item.description
            priceTV.text = "₹ ${item.price}"

            // Load first image if available
            val modelImage = item.Images?.values?.firstOrNull()
            if (modelImage != null && modelImage.imageUrl.isNotEmpty()) {
                Glide.with(context)
                    .load(modelImage.imageUrl)
                    .placeholder(R.drawable.image_gray)
                    .into(itemIV)
            } else {
                itemIV.setImageResource(R.drawable.image_gray)
            }

            // Set favorite button state
            favButton.setImageResource(R.drawable.baseline_favorite_black)

            // Handle click on favorite button
            favButton.setOnClickListener {
                val newFavState = !item.isFavourite // Toggle state
                // Update Realtime Database
                itemRef.child("isFavourite").setValue(newFavState).addOnSuccessListener {
                    item.isFavourite = newFavState
                    notifyItemChanged(position)
                    if (newFavState) {
                        // Add to cart
                        Toast.makeText(context,"Added to cart",Toast.LENGTH_SHORT).show()
                        cartRef.child(item.id).setValue(item)
                    } else {
                        // Remove from cart
                        Toast.makeText(context,"Removed from cart",Toast.LENGTH_SHORT).show()
                        cartRef.child(item.id).removeValue()
                    }
                }.addOnFailureListener { e ->
                    Log.e("ItemsAdapter", "Failed to update favorite status", e)
                }
            }
        }
    }
}
