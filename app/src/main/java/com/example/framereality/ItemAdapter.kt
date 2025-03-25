package com.example.framereality.adapter

import ModelItem
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.framereality.R
import com.example.framereality.databinding.RowItemBinding

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
        holder.binding.apply {
            // Bind text fields
            titleTV.text = item.title
            cityTV.text = item.city
            descriptionTV.text = item.description
            priceTV.text = "$${item.price}"
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
        }
    }
}
