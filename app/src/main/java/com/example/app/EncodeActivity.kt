package com.example.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.example.app.utilities.BitmapUtils.saveBitmapToFile
import com.example.app.utilities.StatsUtils
import com.example.app.databinding.EncodeImageBinding
import com.example.app.network.MedicalImage
import com.example.app.imageprocessing.ImageOperations
import com.example.app.utilities.BitmapUtils

class EncodeActivity : AppCompatActivity() {

    private lateinit var binding: EncodeImageBinding
    private lateinit var statsUtils: StatsUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.encode_image)
        val selectedImage = intent.getParcelableExtra("selected_photo", MedicalImage::class.java)
        binding.photo = selectedImage

        statsUtils = StatsUtils(this)
        statsUtils.setup(R.id.statistics_memory, R.id.statistics_cpu)
        statsUtils.start()

        val decodedPhotoUri = intent.getStringExtra("decoded_image_uri")
        if (decodedPhotoUri != null) {
            val decodedBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(Uri.parse(decodedPhotoUri)))
            val imageView: ImageView = findViewById(R.id.image_view)
            imageView.setImageBitmap(decodedBitmap)
        }

        val originalImageUri = intent.getStringExtra("original_image_uri")
        if (originalImageUri != null) {
            val originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(Uri.parse(originalImageUri)))
            val imageView: ImageView = findViewById(R.id.image_view)
            imageView.setImageBitmap(originalBitmap)
        }

        val encodeButton: Button = findViewById(R.id.encode_button)
        encodeButton.setOnClickListener {
            val index = getCurrentIndex()
            val originalFilename = "original_image_$index.png"
            val encodedFilename = "encoded_image_$index.png"
            val startTime = System.currentTimeMillis()
            val selectedImageView: ImageView = findViewById(R.id.image_view)
            val originalBitmap = (selectedImageView.drawable as BitmapDrawable).bitmap
            val originalUri = saveBitmapToFile(originalBitmap, externalCacheDir, originalFilename)
            val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

            val encodedBitmap = ImageOperations.encode(mutableBitmap)
            val encodedUri = saveBitmapToFile(encodedBitmap, externalCacheDir, encodedFilename)

            BitmapUtils.saveImagePairToCache(this, originalUri, encodedUri)

            incrementIndex()

            val i = Intent(this@EncodeActivity, DecodeActivity::class.java)
            i.putExtra("encoded_image_uri", encodedUri.toString())
            i.putExtra("original_image_uri", originalUri.toString())
            i.putExtra("start_time", startTime)
            startActivity(i)
        }

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val i = Intent(this@EncodeActivity, MainActivity::class.java)
            startActivity(i)
        }

        val menuButton: ImageButton = findViewById(R.id.menu_button)
        menuButton.setOnClickListener {
            val intent = Intent(this@EncodeActivity, EncodedImagesListActivity::class.java)
            startActivity(intent)
        }

    }

    private fun getCurrentIndex(): Int {
        val sharedPreferences = getSharedPreferences("IMAGE_INDEX_PREF", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("image_index", 0)
    }

    private fun incrementIndex() {
        val sharedPreferences = getSharedPreferences("IMAGE_INDEX_PREF", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val currentIndex = getCurrentIndex()
        editor.putInt("image_index", currentIndex + 1)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        statsUtils.stop()
    }
}
