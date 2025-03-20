package com.example.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.framereality.R
import com.example.framereality.assets.Gift

class GiftAdapter(private val context: Context, private val giftList: MutableList<Gift>) :
    RecyclerView.Adapter<GiftAdapter.GiftViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.gift_card_item, parent, false)
        return GiftViewHolder(view)
    }

    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        val gift = giftList[position]
        holder.giftName.text = gift.name
        holder.giftPrice.text = "₹ ${gift.price}"
        Glide.with(context).load(gift.image).into(holder.giftImage)
    }

    override fun getItemCount(): Int = giftList.size

    class GiftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val giftImage: ImageView = itemView.findViewById(R.id.giftImage)
        val giftName: TextView = itemView.findViewById(R.id.giftName)
        val giftPrice: TextView = itemView.findViewById(R.id.giftPrice)
    }
}
