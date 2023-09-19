package com.example.app

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.example.app.databinding.DecodeImageBinding
import com.example.app.network.MedicalImage
import com.example.app.imageprocessing.ImageOperations
import com.example.app.utilities.BitmapUtils.saveBitmapToFile
import com.example.app.utilities.StatsUtils

class DecodeActivity : AppCompatActivity() {

    private lateinit var binding: DecodeImageBinding
    private lateinit var statsUtils: StatsUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.decode_image)
        val selectedImage = intent.getParcelableExtra("selected_photo", MedicalImage::class.java)
        binding.photo = selectedImage

        statsUtils = StatsUtils(this)
        statsUtils.setup(R.id.statistics_memory, R.id.statistics_cpu)
        statsUtils.start()

        val imageUri = Uri.parse(intent.getStringExtra("encoded_image_uri"))
        val encodedBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        val imageView: ImageView = findViewById(R.id.encoded_image)
        imageView.setImageBitmap(encodedBitmap)

        val startTime = intent.getLongExtra("start_time", -1)
        if (startTime != -1L) {
            val timeTaken = System.currentTimeMillis() - startTime
            val timeTextView: TextView = findViewById(R.id.statistics_time)
            timeTextView.text = "Encoding Time:  ${timeTaken} ms"
        }

        val decodeButton: Button = findViewById(R.id.decode_button)
        decodeButton.setOnClickListener {
            val decodedBitmap = encodedBitmap?.let { it1 -> ImageOperations.decode(it1) }
            if (decodedBitmap != null) {
                val decodedUri = saveBitmapToFile(decodedBitmap, externalCacheDir, "decoded_image.png")
                val i = Intent(this@DecodeActivity, EncodeActivity::class.java)
                i.putExtra("decoded_image_uri", decodedUri.toString())
                startActivity(i)
            } else {
                Log.e("DecodeActivity", "Decoded bitmap is null.")
            }
        }

        val compareButton: Button = findViewById(R.id.compare_button)
        compareButton.setOnClickListener {
            val originalImageUri = intent.getStringExtra("original_image_uri")
            val i = Intent(this@DecodeActivity, CompareActivity::class.java)
            i.putExtra("original_image_uri", originalImageUri)
            i.putExtra("encoded_image_uri", imageUri.toString())
            startActivity(i)
        }

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val i = Intent(this@DecodeActivity, EncodeActivity::class.java)
            i.putExtra("selected_photo", selectedImage)
            i.putExtra("original_image_uri", intent.getStringExtra("original_image_uri"))
            startActivity(i)
        }

        val menuButton: ImageButton = findViewById(R.id.menu_button)
        menuButton.setOnClickListener {
            val intent = Intent(this@DecodeActivity, EncodedImagesListActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        statsUtils.stop()
    }
}
