package com.example.app

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.app.databinding.CompareImagesBinding
import com.example.app.network.MedicalImage
import java.io.File

class CompareActivity : AppCompatActivity() {
    private lateinit var binding: CompareImagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.compare_images)
        val selectedImage = intent.getParcelableExtra("selected_photo", MedicalImage::class.java)
        binding.photo = selectedImage

        val originalImageView: ImageView = findViewById(R.id.original_image)
        val originalImageUri = Uri.parse(intent.getStringExtra("original_image_uri"))
        val originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(originalImageUri))
        originalImageView.setImageBitmap(originalBitmap)

        val encodedImageView: ImageView = findViewById(R.id.encoded_image)
        val encodedImageUri = Uri.parse(intent.getStringExtra("encoded_image_uri"))
        val encodedBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(encodedImageUri))
        encodedImageView.setImageBitmap(encodedBitmap)

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val i = Intent(this@CompareActivity, DecodeActivity::class.java)
            i.putExtra("encoded_image_uri", encodedImageUri.toString())
            i.putExtra("original_image_uri", originalImageUri.toString())
            startActivity(i)
        }
    }
}
