package com.example.app

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ImagePairsAdapter(private val imagePairs: List<Pair<Uri, Uri>>) : RecyclerView.Adapter<ImagePairsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val originalImageView: ImageView = itemView.findViewById(R.id.originalImageView)
        val encodedImageView: ImageView = itemView.findViewById(R.id.encodedImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_pair, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imagePair = imagePairs[position]
        holder.originalImageView.setImageURI(imagePair.first)
        holder.encodedImageView.setImageURI(imagePair.second)
    }

    override fun getItemCount(): Int {
        return imagePairs.size
    }

}

