package com.example.ecommerce.viewmodel.mlkit

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageLabelViewModel @Inject constructor() : ViewModel() {

    private val _labels = MutableLiveData<List<String>>()
    val labels: LiveData<List<String>> get() = _labels

    fun analyzeImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                _labels.value = labels.map { it.text }
            }
            .addOnFailureListener { e ->
                Log.e("ImageLabeling", "Error labeling image", e)
            }
    }
}