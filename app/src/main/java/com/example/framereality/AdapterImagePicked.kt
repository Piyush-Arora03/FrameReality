package com.example.framereality

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.framereality.databinding.ActivityChangePasswordBinding
import com.example.framereality.databinding.RowImagePickedBinding

class AdapterImagePicked(
    private val context: Context,
    private val imagePickedArrayList: ArrayList<ModelImagePicked>
) :Adapter<AdapterImagePicked.HolderImagePicked>() {

    //ViewBinding
    private lateinit var binding: RowImagePickedBinding
    inner class HolderImagePicked(itemView: View) : ViewHolder(itemView){

        var imageTv = binding.imageIV
        var closeBtn = binding.closeBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagePicked {
        binding = RowImagePickedBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderImagePicked(binding.root)
    }

    override fun getItemCount(): Int {
        return imagePickedArrayList.size
    }

    override fun onBindViewHolder(holder: HolderImagePicked, position: Int) {
        val modelImagePicked = imagePickedArrayList[position]
        val imageUri = modelImagePicked.localImageUri

        Glide.with(context)
            .load(imageUri)
            .placeholder(R.drawable.image_gray)
            .into(holder.imageTv)

        holder.closeBtn.setOnClickListener {
            imagePickedArrayList.remove(modelImagePicked)
            notifyItemRemoved(position)
        }
    }
}