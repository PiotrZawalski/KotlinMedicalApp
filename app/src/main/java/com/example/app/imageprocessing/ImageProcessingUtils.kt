package com.example.app.imageprocessing

import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.ceil
import kotlin.math.floor

object ImageProcessingUtils {

    fun decodingDNAImage(m: Int, n: Int, I: IntArray, keyDecimal: IntArray, keyFeature: Int): Array<IntArray> {
        val len4mn = 4 * n * m
        var xx = (keyDecimal.take(8).reduce { acc, i -> acc xor i } xor keyFeature) / 256.0
        val u = 3.89 + xx * 0.01
        var x = (keyDecimal.slice(8 until 16).reduce { acc, i -> acc xor i } xor keyFeature) / 256.0
        val len = keyDecimal.slice(0 until 3).sum() + keyFeature
        repeat(len) {
            x *= u * (1 - x)
        }

        val logisticSeq = DoubleArray(len4mn)
        logisticSeq[0] = x
        for (i in 1 until len4mn) {
            logisticSeq[i] = u * logisticSeq[i - 1] * (1 - logisticSeq[i - 1])
        }

        val R = logisticSeq.map { (it * 8).toInt() + 1 }.toIntArray()

        val decodeDNA = IntArray(len4mn)
        for (i in 0 until len4mn) {
            val ri = R[i]
            val ii = I[i]
            decodeDNA[i] = when (ri) {
                1 -> when (ii) {
                    0 -> 1
                    1 -> 0
                    2 -> 3
                    else -> 2
                }
                2 -> when (ii) {
                    0 -> 2
                    1 -> 0
                    2 -> 3
                    else -> 1
                }
                3 -> when (ii) {
                    0 -> 0
                    1 -> 1
                    2 -> 2
                    else -> 3
                }
                4 -> when (ii) {
                    0 -> 0
                    1 -> 2
                    2 -> 1
                    else -> 3
                }
                5 -> when (ii) {
                    0 -> 3
                    1 -> 1
                    2 -> 2
                    else -> 0
                }
                6 -> when (ii) {
                    0 -> 3
                    1 -> 2
                    2 -> 1
                    else -> 0
                }
                7 -> when (ii) {
                    0 -> 1
                    1 -> 3
                    2 -> 0
                    else -> 2
                }
                8 -> when (ii) {
                    0 -> 2
                    1 -> 3
                    2 -> 0
                    else -> 1
                }
                else -> throw IllegalArgumentException("Unexpected value: $ri")
            }
        }

        val imageDecoding = IntArray(m * n)
        var sign = 0
        var num = 0
        for (i in 0 until len4mn step 4) {
            for (j in i until i + 4) {
                num += when (j % 4) {
                    0 -> decodeDNA[j] * 64
                    1 -> decodeDNA[j] * 16
                    2 -> decodeDNA[j] * 4
                    else -> decodeDNA[j] * 1
                }
                if (j % 4 == 3) {
                    imageDecoding[sign] = num
                    sign += 1
                    num = 0
                }
            }
        }

        val reshapedImageDecoding = Array(m) { IntArray(n) }
        for (i in 0 until m) {
            for (j in 0 until n) {
                reshapedImageDecoding[i][j] = imageDecoding[i * n + j]
            }
        }

        return reshapedImageDecoding
    }

    fun diffusionDNA(image: ByteArray, keyImage: ByteArray, keyDecimal: IntArray, keyFeature: Int, m: Int, n: Int, type: String): ByteArray {
        val len4mn = 4 * n * m

        val evenIndices = listOf(0, 2, 4, 6, 8, 10, 12, 14)
        val oddIndices = listOf(1, 3, 5, 7, 9, 11, 13, 15)

        var xx = evenIndices.map { keyDecimal[it] }.reduce { acc, i -> acc xor i } / 256.0
        val u = 3.89 + xx * 0.01
        val len = evenIndices.take(3).map { keyDecimal[it] }.sum() + keyFeature

        var x = oddIndices.map { keyDecimal[it] }.reduce { acc, i -> acc xor i } / 256.0
        repeat(len) {
            x *= u * (1 - x)
        }

        val chaoticSignal = DoubleArray(len4mn).apply { this[0] = x }
        for (i in 1 until len4mn) {
            chaoticSignal[i] = u * chaoticSignal[i - 1] * (1 - chaoticSignal[i - 1])
        }

        val operation = chaoticSignal.map { (it * 7).toInt() + 1 }.toIntArray()

        val diffImg = IntArray(len4mn)

        val xor = arrayOf(
            intArrayOf(0, 1, 2, 3),
            intArrayOf(1, 0, 3, 2),
            intArrayOf(2, 3, 0, 1),
            intArrayOf(3, 2, 1, 0)
        )

        val add = arrayOf(
            intArrayOf(1, 0, 3, 2),
            intArrayOf(0, 1, 2, 3),
            intArrayOf(3, 2, 1, 0),
            intArrayOf(2, 3, 0, 1)
        )

        val mul = arrayOf(
            intArrayOf(3, 2, 1, 0),
            intArrayOf(2, 3, 0, 1),
            intArrayOf(1, 0, 3, 2),
            intArrayOf(0, 1, 2, 3)
        )

        val xnor = arrayOf(
            intArrayOf(3, 2, 1, 0),
            intArrayOf(2, 3, 0, 1),
            intArrayOf(1, 0, 3, 2),
            intArrayOf(0, 1, 2, 3)
        )

        val sub = arrayOf(
            intArrayOf(1, 2, 3, 0),
            intArrayOf(0, 1, 2, 3),
            intArrayOf(3, 0, 1, 2),
            intArrayOf(2, 3, 0, 1)
        )

        val rShift = arrayOf(
            intArrayOf(0, 1, 2, 3),
            intArrayOf(1, 2, 3, 0),
            intArrayOf(2, 3, 0, 1),
            intArrayOf(3, 0, 1, 2)
        )

        val lShift = arrayOf(
            intArrayOf(0, 3, 2, 1),
            intArrayOf(1, 0, 3, 2),
            intArrayOf(2, 1, 0, 3),
            intArrayOf(3, 2, 1, 0)
        )

        for (i in 0 until len4mn) {
            when (type) {
                "Encryption" -> {
                    when (operation[i]) {
                        1 -> diffImg[i] = add[image[i].toInt() and 0xFF][keyImage[i].toInt() and 0xFF]
                        2 -> diffImg[i] = sub[image[i].toInt() and 0xFF][keyImage[i].toInt() and 0xFF]
                        3 -> diffImg[i] = xor[image[i].toInt() and 0xFF][keyImage[i].toInt() and 0xFF]
                        4 -> diffImg[i] = xnor[image[i].toInt() and 0xFF][keyImage[i].toInt() and 0xFF]
                        5 -> diffImg[i] = mul[image[i].toInt() and 0xFF][keyImage[i].toInt() and 0xFF]
                        6 -> diffImg[i] = rShift[image[i].toInt() and 0xFF][keyImage[i].toInt() and 0xFF]
                        7 -> diffImg[i] = lShift[image[i].toInt() and 0xFF][keyImage[i].toInt() and 0xFF]
                    }
                }
                "Decryption" -> {
                    when (operation[i]) {
                        1 -> diffImg[i] = add[image[i].toInt() and 0xFF][keyImage[i].toInt() and 0xFF]
                        2 -> diffImg[i] = sub[image[i].toInt() and 0xFF][keyImage[i].toInt() and 0xFF]
                        3 -> diffImg[i] = xor[image[i].toInt() and 0xFF][keyImage[i].toInt() and 0xFF]
                        4 -> diffImg[i] = xnor[image[i].toInt() and 0xFF][keyImage[i].toInt() and 0xFF]
                        5 -> diffImg[i] = mul[image[i].toInt() and 0xFF][keyImage[i].toInt() and 0xFF]
                        6 -> diffImg[i] = lShift[image[i].toInt() and 0xFF][keyImage[i].toInt() and 0xFF]
                        7 -> diffImg[i] = rShift[image[i].toInt() and 0xFF][keyImage[i].toInt() and 0xFF]
                    }
                }
            }
        }


        return intArrayToByteArray(diffImg)
    }

    fun encodedImageIntoDNASequence(m: Int, n: Int, I: IntArray, keyDecimal: IntArray, keyFeature: Int): IntArray {
        val len4mn = 4 * n * m

        val firstEightIndices = (0..7).toList()
        val lastEightIndices = (8..15).toList()

        var xx = firstEightIndices.map { keyDecimal[it] }.reduce { acc, i -> acc xor i } / 256.0
        val u = 3.89 + xx * 0.01

        var x = lastEightIndices.map { keyDecimal[it] }.reduce { acc, i -> acc xor i } / 256.0

        val len = lastEightIndices.take(3).map { keyDecimal[it] }.sum() + keyFeature

        repeat(len) {
            x *= u * (1 - x)
        }

        val logisticSeq = DoubleArray(len4mn).apply { this[0] = x }
        for (i in 1 until len4mn) {
            logisticSeq[i] = u * logisticSeq[i - 1] * (1 - logisticSeq[i - 1])
        }

        val R = logisticSeq.map { (it * 8).toInt() + 1 }.toIntArray()

        val encodeDNA = IntArray(len4mn)
        for (i in 0 until len4mn) {
            when (R[i]) {
                1 -> encodeDNA[i] = when (I[i]) {
                    0 -> 1
                    1 -> 0
                    2 -> 3
                    else -> 2
                }
                2 -> encodeDNA[i] = when (I[i]) {
                    0 -> 1
                    1 -> 3
                    2 -> 0
                    else -> 2
                }
                3 -> encodeDNA[i] = when (I[i]) {
                    0 -> 0
                    1 -> 1
                    2 -> 2
                    else -> 3
                }
                4 -> encodeDNA[i] = when (I[i]) {
                    0 -> 0
                    1 -> 2
                    2 -> 1
                    else -> 3
                }
                5 -> encodeDNA[i] = when (I[i]) {
                    0 -> 3
                    1 -> 1
                    2 -> 2
                    else -> 0
                }
                6 -> encodeDNA[i] = when (I[i]) {
                    0 -> 3
                    1 -> 2
                    2 -> 1
                    else -> 0
                }
                7 -> encodeDNA[i] = when (I[i]) {
                    0 -> 2
                    1 -> 0
                    2 -> 3
                    else -> 1
                }
                8 -> encodeDNA[i] = when (I[i]) {
                    0 -> 2
                    1 -> 3
                    2 -> 0
                    else -> 1
                }
            }
        }

        return encodeDNA
    }

    fun encodeImageInto4Subcell(m: Int, n: Int, plainImg: Array<IntArray>): IntArray {
        val imgSize = n * m
        val I = IntArray(4 * n * m)
        val plainImg1D = plainImg.flatMap { it.toList() }

        for (i in 1..imgSize) {
            var numToDecompose = plainImg1D[i - 1]
            for (z in 1..4) {
                val rem = numToDecompose % 4
                I[4 * (i - 1) + (5 - z) - 1] = rem
                numToDecompose /= 4
            }
        }
        return I
    }

    fun extractKeyFeature(keyDecimal: IntArray): Int {
        var keyFeature = keyDecimal[0] xor keyDecimal[1]
        for (i in 2 until keyDecimal.size) {
            keyFeature = keyFeature xor keyDecimal[i]
        }
        return keyFeature
    }

    fun hashFunction(inp: Any, meth: String): String {
        val byteArray = when (inp) {
            is String -> inp.toByteArray()
            is ByteArray -> inp
            else -> {
                val byteBuffer = ByteBuffer.allocate(4 * inp.toString().length)
                inp.toString().forEach { byteBuffer.putInt(it.toInt()) }
                byteBuffer.array()
            }
        }

        val method = when (meth.uppercase()) {
            "SHA1" -> "SHA-1"
            "SHA256" -> "SHA-256"
            "SHA384" -> "SHA-384"
            "SHA512" -> "SHA-512"
            else -> meth
        }

        val algs = listOf("MD2", "MD5", "SHA-1", "SHA-256", "SHA-384", "SHA-512")
        if (!algs.contains(method)) {
            throw IllegalArgumentException("Hash algorithm must be MD2, MD5, SHA-1, SHA-256, SHA-384, or SHA-512")
        }

        val md = MessageDigest.getInstance(method)
        md.update(byteArray)
        val hashBytes = md.digest()

        val hexString = StringBuilder()
        for (byte in hashBytes) {
            val hex = Integer.toHexString(byte.toInt() and 0xFF)
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }

        return hexString.toString().lowercase()
    }

    fun hashSumRowSumCol(plainImg: Array<IntArray>, keyHex: String): String {
        val sumRow = IntArray(plainImg.size) { i -> plainImg[i].sum() }
        val sumCol = IntArray(plainImg[0].size) { j -> plainImg.sumOf { it[j] } }

        for (i in 0 until 5) {
            println("Row $i: ${plainImg[i].take(5).joinToString(", ")}")
        }

        for (j in 0 until 5) {
            println("Column $j: ${plainImg.map { it[j] }.take(5).joinToString(", ")}")
        }


        val hashSumRow = hashFunction(sumRow, "MD5")
        val hashSumCol = hashFunction(sumCol, "MD5")
        val hashKeyHex = hashFunction(keyHex, "MD5")


        val concatenatedHashes = hashSumRow + hashSumCol + hashKeyHex
        println("concat: $concatenatedHashes")
        val finalHash = hashFunction(concatenatedHashes, "SHA-256")

        return finalHash
    }

    fun hexToBin(h: String, n: Int): String {
        if (h.isEmpty()) return "0".repeat(n)
        val hexStr = h.uppercase().trimStart()

        if (hexStr.any { !it.isDigit() && (it < 'A' || it > 'F') }) {
            throw IllegalArgumentException("Input string found with characters other than 0-9, A-F.")
        }

        val decimalValue = BigInteger(hexStr, 16)
        val requiredBits = n.coerceAtLeast(decimalValue.toString(2).length)
        val binaryStr = decimalValue.toString(2).padStart(requiredBits, '0')

        return binaryStr
    }


    fun binToDec(binStr: String): Int {
        return Integer.parseInt(binStr, 2)
    }

    fun hashToDecimal(keyHex: String, hashVal: String): IntArray {
        val n = keyHex.length / 2
        val hexBinKey = hexToBin(keyHex, n * 8)
        val hexBinHashVal = hexToBin(hashVal, n * 8)

        val hexDecimal = IntArray(n) {
            binToDec(hexBinKey.substring(it * 8, (it + 1) * 8))
        }

        val hashDecimal = IntArray(n) {
            binToDec(hexBinHashVal.substring(it * 8, (it + 1) * 8))
        }

        val key = IntArray(n) {
            hexDecimal[it] xor hashDecimal[it]
        }

        return key
    }

    fun keyDNA5HyperchaoticSystem(M: Int, N: Int, keyDecimal: IntArray, keyFeature: Int): IntArray {
        val c1 = 30.0
        val c2 = 10.0
        val c3 = 15.7
        val c4 = 5.0
        val c5 = 2.5
        val c6 = 4.45
        val c7 = 38.5

        val x = DoubleArray(4 * ceil((M * N) / 5.0).toInt() + keyDecimal[30] + keyDecimal[31] + keyFeature) { 0.0 }
        val y = DoubleArray(x.size) { 0.0 }
        val z = DoubleArray(x.size) { 0.0 }
        val u = DoubleArray(x.size) { 0.0 }
        val w = DoubleArray(x.size) { 0.0 }

        x[0] = (keyDecimal.slice(0..5).reduce { acc, i -> acc xor i } xor keyFeature) / 256.0
        y[0] = (keyDecimal.slice(6..11).reduce { acc, i -> acc xor i } xor keyFeature) / 256.0
        z[0] = (keyDecimal.slice(12..17).reduce { acc, i -> acc xor i } xor keyFeature) / 256.0
        u[0] = (keyDecimal.slice(18..23).reduce { acc, i -> acc xor i } xor keyFeature) / 256.0
        w[0] = (keyDecimal.slice(24..29).reduce { acc, i -> acc xor i } xor keyFeature) / 256.0

        val discard = keyDecimal[30] + keyDecimal[31] + keyFeature
        val sizeSignal = 4 * ceil((M * N) / 5.0).toInt() + discard

        for (i in 1 until discard) {
            updateVariables(i, x, y, z, u, w, c1, c2, c3, c4, c5, c6, c7)
        }

        val key = IntArray(4 * M * N)
        var j = 0

        for (i in discard until sizeSignal) {
            if (j + 4 >= key.size) break
            updateVariables(i, x, y, z, u, w, c1, c2, c3, c4, c5, c6, c7)

            key[j] = (x[i] * 4).toInt() % 4
            key[j + 1] = (y[i] * 4).toInt() % 4
            key[j + 2] = (z[i] * 4).toInt() % 4
            key[j + 3] = (u[i] * 4).toInt() % 4
            key[j + 4] = (w[i] * 4).toInt() % 4

            j += 5
        }


        return key
    }

    fun updateVariables(i: Int, x: DoubleArray, y: DoubleArray, z: DoubleArray, u: DoubleArray, w: DoubleArray, c1: Double, c2: Double, c3: Double, c4: Double, c5: Double, c6: Double, c7: Double) {
        x[i] = -c1 * x[i - 1] + c1 * y[i - 1]
        y[i] = c2 * x[i - 1] + c2 * y[i - 1] + w[i - 1] - x[i - 1] * z[i - 1] * u[i - 1]
        z[i] = -c3 * y[i - 1] - c4 * z[i - 1] - c5 * u[i - 1] + x[i - 1] * y[i - 1] * u[i - 1]
        u[i] = -c6 * u[i - 1] + x[i - 1] * y[i - 1] * z[i - 1]
        w[i] = -c7 * x[i - 1] - c7 * y[i - 1]

        x[i] = (x[i] * 10000) - floor(x[i] * 10000)
        y[i] = (y[i] * 10000) - floor(y[i] * 10000)
        z[i] = (z[i] * 10000) - floor(z[i] * 10000)
        u[i] = (u[i] * 10000) - floor(u[i] * 10000)
        w[i] = (w[i] * 10000) - floor(w[i] * 10000)
    }

    fun permutationDNA(image: ByteArray, keyDecimal: IntArray, keyFeature: Int, m: Int, n: Int, type: String): ByteArray {
        val d1 = keyDecimal[16]
        val d2 = keyDecimal[17]
        val d3 = keyDecimal[18]
        val d4 = keyDecimal[19]
        val d5 = keyDecimal[20]
        val d6 = keyDecimal[21]
        val d7 = keyDecimal[22]
        val d8 = keyDecimal[23]

        val xx = (arrayOf(d1, d2, d3, d4, d5, d6, d7, d8).reduce { acc, i -> acc xor i } xor keyFeature) / 256.0
        val u = 3.89 + xx * 0.01
        val len = d1 + d2 + d3 + keyFeature

        val d1New = keyDecimal[24]
        val d2New = keyDecimal[25]
        val d3New = keyDecimal[26]
        val d4New = keyDecimal[27]
        val d5New = keyDecimal[28]
        val d6New = keyDecimal[29]
        val d7New = keyDecimal[30]
        val d8New = keyDecimal[31]

        var x = (arrayOf(d1New, d2New, d3New, d4New, d5New, d6New, d7New, d8New).reduce { acc, i -> acc xor i } xor keyFeature) / 256.0

        repeat(len) {
            x = u * x * (1 - x)
        }

        val len4mn = 4 * n * m
        val chaoticSignal = DoubleArray(len4mn)
        chaoticSignal[0] = x

        for (i in 1 until len4mn) {
            chaoticSignal[i] = u * chaoticSignal[i - 1] * (1 - chaoticSignal[i - 1])
        }

        val sortedChaoticSignalWithIndices = chaoticSignal.withIndex().sortedBy { it.value }
        val pos = sortedChaoticSignalWithIndices.map { it.index }

        val perImage = ByteArray(len4mn)

        when (type) {
            "Encryption" -> {
                for (i in 0 until len4mn) {
                    perImage[i] = image[pos[i]]
                }
            }
            "Decryption" -> {
                for (i in 0 until len4mn) {
                    perImage[pos[i]] = image[i]
                }
            }
        }

        return perImage
    }

    fun encryption(plainImg: Array<IntArray>, keyImage: IntArray, keyDecimal: IntArray, keyFeature: Int, m: Int, n: Int): Array<IntArray> {
        val encodedDifImg = encodeImageInto4Subcell(m, n, plainImg)
        val encodedDNADifImg = encodedImageIntoDNASequence(m, n, encodedDifImg, keyDecimal, keyFeature)
        val encodedDNAPerImage = permutationDNA(intArrayToByteArray(encodedDNADifImg), keyDecimal, keyFeature, m, n, "Encryption")
        val difImgDNA = diffusionDNA(encodedDNAPerImage, intArrayToByteArray(keyImage), keyDecimal, keyFeature, m, n, "Encryption")
        val encImage = decodingDNAImage(m, n, byteArrayToIntArray(difImgDNA), keyDecimal, keyFeature)

        return encImage
    }

    fun decryption(enImg: Array<IntArray>, keyImage: IntArray, keyDecimal: IntArray, keyFeature: Int, m: Int, n: Int): Array<IntArray> {
        val encodedEnImg = encodeImageInto4Subcell(m, n, enImg)
        val encodedDNADNAEnImg = encodedImageIntoDNASequence(m, n, encodedEnImg, keyDecimal, keyFeature)
        val difImgDNA = diffusionDNA(intArrayToByteArray(encodedDNADNAEnImg), intArrayToByteArray(keyImage), keyDecimal, keyFeature, m, n, "Decryption")
        val perImageDNA = permutationDNA(difImgDNA, keyDecimal, keyFeature, m, n, "Decryption")
        val decImage = decodingDNAImage(m, n, byteArrayToIntArray(perImageDNA), keyDecimal, keyFeature)

        return decImage
    }


    fun byteArrayToIntArray(byteArray: ByteArray): IntArray {
        return IntArray(byteArray.size) { byteArray[it].toInt() and 0xFF }
    }

    fun intArrayToByteArray(array: IntArray): ByteArray {
        return ByteArray(array.size) { array[it].toByte() }
    }


}
