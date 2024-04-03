package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class ImageClassifierHelper(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val modelPath = "cancer_classification.tflite"
    private val imageSizeX = 224
    private val imageSizeY = 224
    private val modelInputSize = 224 * 224 * 3 * 4

    init {
        try {
            interpreter = Interpreter(loadModelFile())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(): ByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun classifyStaticImage(imageUri: Uri): Pair<String, Float>? {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSizeX, imageSizeY, true)
            val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)

            val output = Array(1) { FloatArray(2) }
            interpreter?.run(byteBuffer, output)

            val disease = if (output[0][0] > output[0][1]) "Tidak ada kanker" else "Ada kanker"
            val confidenceScore = if (output[0][0] > output[0][1]) output[0][0] else output[0][1]

            return Pair(disease, confidenceScore)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(imageSizeX * imageSizeY)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            byteBuffer.putFloat(((pixelValue shr 16 and 0xFF) - 127.5f) / 127.5f)
            byteBuffer.putFloat(((pixelValue shr 8 and 0xFF) - 127.5f) / 127.5f)
            byteBuffer.putFloat(((pixelValue and 0xFF) - 127.5f) / 127.5f)
        }

        return byteBuffer
    }
}
