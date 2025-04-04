package com.example.framereality.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.framereality.PropertyModel
import com.example.framereality.R

class PropertyHomeAdapter(
    private val context: Context,
    private val onFavoriteClick: (PropertyModel) -> Unit
) : RecyclerView.Adapter<PropertyHomeAdapter.PropertyViewHolder>() {

    private val displayedList = ArrayList<PropertyModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_property, parent, false)
        return PropertyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bind(displayedList[position])
    }

    override fun getItemCount(): Int = displayedList.size

    inner class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTV: TextView = itemView.findViewById(R.id.propertyTitleTV)
        private val subcategoryTV: TextView = itemView.findViewById(R.id.propertySubcategoryTV)
        private val priceTV: TextView = itemView.findViewById(R.id.propertyPriceTV)
        private val locationTV: TextView = itemView.findViewById(R.id.propertyLocationTV)
        private val specsTV: TextView = itemView.findViewById(R.id.propertySpecsTV)
        private val descriptionTV: TextView = itemView.findViewById(R.id.propertyDescriptionTV)
        private val propertyImageIV: ImageView = itemView.findViewById(R.id.propertyImageIV)
        private val favoriteBtn: ImageButton = itemView.findViewById(R.id.favoriteBtn)

        fun bind(property: PropertyModel) {
            titleTV.text = property.title
            subcategoryTV.text = property.subcategory
            priceTV.text = "₹${formatPrice(property.price.toLong())}"
            locationTV.text = property.address
            specsTV.text =
                "Floors: ${property.floors} | Beds: ${property.bedrooms} | Baths: ${property.bathrooms} | Area: ${property.areaSizeUnit}"
            descriptionTV.text = property.description

            if (property.imageUrls.isNotEmpty()) {
                Glide.with(context)
                    .load(property.imageUrls[0])
                    .placeholder(R.drawable.image_gray)
                    .into(propertyImageIV)
            } else {
                propertyImageIV.setImageResource(R.drawable.image_gray)
            }

            favoriteBtn.setOnClickListener {
                onFavoriteClick(property)
            }
        }
    }

    private fun formatPrice(price: Long): String {
        return if (price >= 100000) {
            val lakhPrice = price / 100000.0
            if (lakhPrice % 1.0 == 0.0) {
                "${lakhPrice.toInt()}L"
            } else {
                String.format("%.1fL", lakhPrice)
            }
        } else {
            price.toString()
        }
    }

    fun updateData(newList: List<PropertyModel>) {
        displayedList.clear()
        displayedList.addAll(newList)
        notifyDataSetChanged()
    }
}
