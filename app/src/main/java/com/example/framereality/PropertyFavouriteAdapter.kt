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

class PropertyFavouriteAdapter(
    private val context: Context,
    private val propertyList: ArrayList<PropertyModel>,
    private val onFavoriteClick: (PropertyModel) -> Unit
) : RecyclerView.Adapter<PropertyFavouriteAdapter.PropertyFavouriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyFavouriteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.favourite_card_property, parent, false)
        return PropertyFavouriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: PropertyFavouriteViewHolder, position: Int) {
        holder.bind(propertyList[position])
    }

    override fun getItemCount(): Int = propertyList.size

    inner class PropertyFavouriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTV: TextView = itemView.findViewById(R.id.favPropertyTitleTV)
        private val subcategoryTV: TextView = itemView.findViewById(R.id.favPropertySubcategoryTV)
        private val priceTV: TextView = itemView.findViewById(R.id.favPropertyPriceTV)
        private val categoryTV: TextView = itemView.findViewById(R.id.favPropertyCategoryTV)
        private val purposeTV: TextView = itemView.findViewById(R.id.favPropertyPurposeTV)
        private val locationTV: TextView = itemView.findViewById(R.id.favPropertyLocationTV)
        private val specsTV: TextView = itemView.findViewById(R.id.favPropertySpecsTV)
        private val descriptionTV: TextView = itemView.findViewById(R.id.favPropertyDescriptionTV)
        private val contactTV: TextView = itemView.findViewById(R.id.favPropertyContactTV)
        private val propertyImageIV: ImageView = itemView.findViewById(R.id.favPropertyImageIV)
        private val removeFavoriteBtn: ImageButton = itemView.findViewById(R.id.removeFavoriteBtn)

        fun bind(property: PropertyModel) {
            titleTV.text = property.title
            subcategoryTV.text = property.subcategory
            priceTV.text = "₹${formatPrice(property.price)}"
            categoryTV.text = "Category: ${property.category}"
            purposeTV.text = "Purpose: ${property.purpose}"
            locationTV.text = property.address
            // Specs without area and owner info:
            specsTV.text = "Floors: ${property.floors} | Beds: ${property.bedrooms} | Baths: ${property.bathrooms}"
            descriptionTV.text = property.description
            contactTV.text = "Phone: ${property.phoneNumber}"

            if (property.imageUrls.isNotEmpty()) {
                Glide.with(context)
                    .load(property.imageUrls[0])
                    .placeholder(R.drawable.image_gray)
                    .into(propertyImageIV)
            } else {
                propertyImageIV.setImageResource(R.drawable.image_gray)
            }

            removeFavoriteBtn.setOnClickListener {
                onFavoriteClick(property)
            }
        }
    }

    private fun formatPrice(price: Double): String {
        return if (price >= 100000) {
            val lakhPrice = price / 100000.0
            if (lakhPrice % 1.0 == 0.0) "${lakhPrice.toInt()}L" else String.format("%.1fL", lakhPrice)
        } else {
            price.toString()
        }
    }
}
