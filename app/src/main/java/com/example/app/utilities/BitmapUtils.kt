package com.example.app.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

object BitmapUtils {

    fun saveBitmapToFile(bitmap: Bitmap, externalCacheDir: File?, filename: String): Uri {
        val file = File(externalCacheDir, filename)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return Uri.fromFile(file)
    }

    fun saveImagePairToCache(context: Context, originalImageUri: Uri, encodedImageUri: Uri) {
        val originalBitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(originalImageUri))

        val hash = getSHA256Hash(originalBitmap)

        val sharedPreferences = context.getSharedPreferences("IMAGE_PAIRS", Context.MODE_PRIVATE)
        val existingPairs = sharedPreferences.getStringSet("pairs", mutableSetOf()) ?: mutableSetOf()

        if (!existingPairs.any { it.startsWith(hash) }) {
            existingPairs.add("$hash|${originalImageUri.toString()}|${encodedImageUri.toString()}")
            sharedPreferences.edit().putStringSet("pairs", existingPairs).apply()
        }
    }

    private fun getSHA256Hash(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()

        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
