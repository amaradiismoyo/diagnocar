package com.adismoyam.diagnocar.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.adismoyam.diagnocar.BuildConfig
import com.adismoyam.diagnocar.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier


class ImageClassifierHelper(
    private var threshold: Float = 0.6f,
    private var maxResults: Int = 3,
    private val modelName: String = "efficientnetv2s_with_metadata.tflite",
    val context: Context,
    val classifierListener: ClassifierListener?,
) {
    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()

        // DEBUG MODE
        if (BuildConfig.DEBUG) {
            setupDebugClassifier()
        }
    }

    private fun setupImageClassifier() {
        imageClassifier = createClassifier(threshold, maxResults, 4)
        if (imageClassifier == null) {
            classifierListener?.onError(context.getString(R.string.image_classifier_failed))
        }
    }

    // DEBUG MODE
    private var debugClassifier: ImageClassifier? = null
    private fun setupDebugClassifier() {
        debugClassifier = createClassifier(0.0f, 1000, 1) // ambil semua kelas
    }

    // membuat image classifier yang nantinya akan memproses gambar
    private fun createClassifier(
        threshold: Float,
        maxResults: Int,
        numThreads: Int
    ): ImageClassifier? {
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)

        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(numThreads)

        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        return try {
            ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Classifier creation failed: ${e.message}")
            null
        }
    }


    fun classifyStaticImage(imageUri: Uri) {
        // mengklasifikasikan imageUri dari gambar statis.
        if (imageClassifier == null) {
            setupImageClassifier()
        }

        // DEBUG MODE
        if (BuildConfig.DEBUG && debugClassifier == null) {
            setupDebugClassifier()
        }

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(CastOp(DataType.UINT8))
            .build()

        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        }.copy(Bitmap.Config.ARGB_8888, true) // false = tidak mutable -> hemat memori

        bitmap?.let {
            val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

            // hasil normal
            val result = imageClassifier?.classify(tensorImage)
            classifierListener?.onResults(result)

            // logging DEBUG MODE (hanya jalan di debug build)
            if (BuildConfig.DEBUG) {
                val debugResult = debugClassifier?.classify(tensorImage)
                debugResult?.forEach { classifications ->
                    classifications.categories.forEachIndexed { index, category ->
                        Log.d(
                            TAG,
                            "DEBUG #$index → Label: ${category.label}, Score: ${category.score}"
                        )
                    }
                }
            }
        }
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(results: List<Classifications>?)
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }
}

/**
fun close() {
imageClassifier?.close()
debugClassifier?.close()
}
 */