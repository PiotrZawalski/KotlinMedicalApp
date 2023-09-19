package com.example.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EncodedImagesListActivity : AppCompatActivity() {

    private lateinit var imagePairs: List<Pair<Uri, Uri>>
    private lateinit var adapter: ImagePairsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.encoded_images_list)

        imagePairs = getImagePairsFromCache(this)
        adapter = ImagePairsAdapter(imagePairs)

        val recyclerView: RecyclerView = findViewById(R.id.encodedImagesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        val emptyTextView: TextView = findViewById(R.id.emptyTextView)
        if (imagePairs.isEmpty()) {
            emptyTextView.visibility = View.VISIBLE
        } else {
            emptyTextView.visibility = View.GONE
        }

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val i = Intent(this@EncodedImagesListActivity, MainActivity::class.java)
            startActivity(i)
        }
    }

    private fun getImagePairsFromCache(context: Context): List<Pair<Uri, Uri>> {
        val pairsList = mutableListOf<Pair<Uri, Uri>>()

        val sharedPreferences = context.getSharedPreferences("IMAGE_PAIRS", Context.MODE_PRIVATE)
        val storedPairs = sharedPreferences.getStringSet("pairs", mutableSetOf()) ?: mutableSetOf()

        storedPairs.forEach { storedPair ->
            val parts = storedPair.split("|")
            if (parts.size == 3) {
                pairsList.add(Pair(Uri.parse(parts[1]), Uri.parse(parts[2])))
            }
        }

        pairsList.sortByDescending {
            val uri = it.first.toString()
            val indexString = uri.substringAfterLast("original_image_").substringBefore(".png")
            indexString.toIntOrNull() ?: 0
        }

        return pairsList
    }

}


