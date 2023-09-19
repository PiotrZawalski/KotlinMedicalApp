package com.example.app.imageprocessing

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.ceil

object ImageOperations {

    private val keyHex = "6b679b3c77826d30a79e612114a8c18df984c176f4e529f684748ad052241b17"
    private var hashPlainImage: String? = null

    fun getIntensityArray(bitmap: Bitmap): Array<IntArray> {
        val intensityArray = Array(bitmap.height) { IntArray(bitmap.width) { 0 } }
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val red = (pixel shr 16) and 0xff
                val green = (pixel shr 8) and 0xff
                val blue = pixel and 0xff
                intensityArray[y][x] = ceil((0.299 * red) + (0.587 * green) + (0.114 * blue)).toInt()
            }
        }
        return intensityArray
    }

    fun intensityArrayToBitmap(intensityArray: Array<IntArray>): Bitmap {
        val width = intensityArray[0].size
        val height = intensityArray.size
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val intensity = intensityArray[y][x]
                val pixel = Color.argb(255, intensity, intensity, intensity)
                bitmap.setPixel(x, y, pixel)
            }
        }
        return bitmap
    }


    fun encode(bitmap: Bitmap): Bitmap {
        val plainImg = getIntensityArray(bitmap)
        val (m, n) = bitmap.width to bitmap.height


        hashPlainImage = ImageProcessingUtils.hashSumRowSumCol(plainImg, keyHex)

        val keyDecimal = ImageProcessingUtils.hashToDecimal(keyHex, hashPlainImage!!)
        val keyFeature = ImageProcessingUtils.extractKeyFeature(keyDecimal)
        val keyImage = ImageProcessingUtils.keyDNA5HyperchaoticSystem(m, n, keyDecimal, keyFeature)
        val encImg = ImageProcessingUtils.encryption(plainImg, keyImage, keyDecimal, keyFeature, m, n)

        return intensityArrayToBitmap(encImg)
    }


    fun decode(bitmap: Bitmap): Bitmap {
        val enImg = getIntensityArray(bitmap)
        val (m, n) = bitmap.width to bitmap.height

        val keyDecimal = ImageProcessingUtils.hashToDecimal(keyHex, hashPlainImage!!)
        val keyFeature = ImageProcessingUtils.extractKeyFeature(keyDecimal)
        val keyImage = ImageProcessingUtils.keyDNA5HyperchaoticSystem(m, n, keyDecimal, keyFeature)
        val decImg = ImageProcessingUtils.decryption(enImg, keyImage, keyDecimal, keyFeature, m, n)

        return intensityArrayToBitmap(decImg)
    }
}

