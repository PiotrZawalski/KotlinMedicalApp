package com.example.app.overview

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.app.databinding.GridViewItemBinding
import com.example.app.network.MedicalImage
import com.example.app.EncodeActivity

class ImageGridAdapter :
    ListAdapter<MedicalImage, ImageGridAdapter.RtgPhotosViewHolder>(DiffCallback) {

    class RtgPhotosViewHolder(
        private var binding: GridViewItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(rtgPhoto: MedicalImage) {
            binding.photo = rtgPhoto
            binding.image.setOnClickListener {
                val context = itemView.context

                val intent = Intent(context, EncodeActivity::class.java)
                intent.putExtra("selected_photo", rtgPhoto)
                context.startActivity(intent)
            }

            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MedicalImage>() {
        override fun areItemsTheSame(oldItem: MedicalImage, newItem: MedicalImage): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: MedicalImage, newItem: MedicalImage): Boolean {
            return oldItem.imgSrcUrl == newItem.imgSrcUrl
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RtgPhotosViewHolder {
        return RtgPhotosViewHolder(
            GridViewItemBinding.inflate(LayoutInflater.from(parent.context))
        )
    }


    override fun onBindViewHolder(holder: RtgPhotosViewHolder, position: Int) {
        val rtgPhoto = getItem(position)
        holder.bind(rtgPhoto)
    }
}