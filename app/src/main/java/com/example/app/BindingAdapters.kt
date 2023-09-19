package com.example.app

import android.view.View
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.app.network.MedicalImage
import com.example.app.overview.ImageApiStatus
import com.example.app.overview.ImageGridAdapter


@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<MedicalImage>?) {
    val adapter = recyclerView.adapter as ImageGridAdapter
    adapter.submitList(data)
}

@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        imgView.load(imgUri) {
            placeholder(R.drawable.ic_loading_animation)
            error(R.drawable.ic_broken_image)
        }
    }
}

@BindingAdapter("imageApiStatus")
fun bindStatus(statusImageView: ImageView, status: ImageApiStatus) {
    when (status) {
        ImageApiStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.ic_loading_animation)
        }
        ImageApiStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.ic_connection_error)
        }
        ImageApiStatus.DONE -> {
            statusImageView.visibility = View.GONE
        }
    }
}