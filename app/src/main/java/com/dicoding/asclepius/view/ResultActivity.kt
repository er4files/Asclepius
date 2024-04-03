package com.dicoding.asclepius.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.net.Uri
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val result = intent.getStringExtra(EXTRA_RESULT)
        val confidence = intent.getFloatExtra(EXTRA_CONFIDENCE, 0f) * 100
        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)

        if (result != null && imageUriString != null) {
            binding.resultText.text = "Hasil: $result\nConfidence Score: ${confidence}%"
            binding.resultImage.setImageURI(Uri.parse(imageUriString))
        }
    }

    companion object {
        const val EXTRA_RESULT = "extra_result"
        const val EXTRA_CONFIDENCE = "extra_confidence"
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }
}
