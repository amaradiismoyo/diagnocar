package com.adismoyam.diagnocar.view

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.adismoyam.diagnocar.R
import com.adismoyam.diagnocar.ViewModelFactory
import com.adismoyam.diagnocar.data.history.HistoryEntity
import com.adismoyam.diagnocar.databinding.ActivityMainBinding
import com.adismoyam.diagnocar.helper.ImageClassifierHelper
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: HistoryViewModel by viewModels {
        ViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.sleep(3000)
        installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActionBar()

        binding.analyzeButton.apply {
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.alsoGray))
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.Secondary))
        }

        viewModel.currentImageUri.observe(this) { uri ->
            if (uri != null) {
                showImage(uri)
            }
        }

        binding.apply {
            analyzeButton.setOnClickListener{

                val uri = viewModel.currentImageUri.value

                uri?.let {
                    analyzeImage(it)
                } ?: showToast("No image selected")
            }
            galleryButton.setOnClickListener{
                startGallery()
            }
        }
    }

    private fun setActionBar() {
        supportActionBar?.apply {
            setCustomView(R.layout.app_bar)
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)
            setBackgroundDrawable(ColorDrawable(getColor(R.color.Primary)))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val historyIntent = Intent(this, HistoryActivity::class.java)
        startActivity(historyIntent)
        return super.onOptionsItemSelected(item)
    }

    private fun startGallery() {
        // Mendapatkan gambar dari Gallery.
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }


    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
        crop(it)
    } ?: Log.d("Photo Picker", "No media selected")
    }

    private fun crop(uri: Uri) {
        val destinationUri = Uri.fromFile(cacheDir.resolve("${System.currentTimeMillis()}.jpg"))
        UCrop.of(uri, destinationUri)
            .withAspectRatio(4F, 4F)
            .withMaxResultSize(2000, 2000)
            .start(this)
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "super.onActivityResult(requestCode, resultCode, data)",
        "androidx.appcompat.app.AppCompatActivity"
    ))
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            viewModel.setCurrentImageUri(resultUri)
            resultUri?.let {
                viewModel.setCurrentImageUri(it)
                showImage(it)
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Log.e("Crop Error", "onActivityResult: $cropError")
        }
    }

    private fun showImage(uri: Uri) {
        // Menampilkan gambar sesuai Gallery yang dipilih.
//        currentImageUri?.let {
            binding.apply {
                previewImageView.setImageURI(uri)
                analyzeButton.visibility = android.view.View.VISIBLE
                galleryButton.apply {
                    text = resources.getString(R.string.ganti_gambar)
                    setBackgroundColor(ContextCompat.getColor(this@MainActivity,R.color.OnSecondary))
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.Secondary))
                }
                analyzeButton.apply {
                    setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.Primary))
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.OnPrimary))
                }
            }
//        }
    }

    private fun analyzeImage(image: Uri) {
        // Menganalisa gambar yang berhasil ditampilkan.

        val imageHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    showToast(error)
                }

                override fun onResults(results: List<Classifications>?) {

                    val resultString = results?.joinToString("\n") { classification ->
                        if (classification.categories.isNotEmpty()) {
                            val threshold = (classification.categories[0].score * 100).toInt()
                            "${classification.categories[0].label} \n ${threshold}%"
                        } else {
                            "Coba Lagi"
                        }
                    }

                    // yang di bawah kode lama ku yang error:
                    // java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0

//                    val resultString = results?.joinToString("\n") {
//                        val threshold = (it.categories[0].score * 100).toInt()
//                        "${it.categories[0].label} \n ${threshold}%"
//                    }

                    resultString?.let {
                        val dataHasil = HistoryEntity(
                            date = millisToDate(System.currentTimeMillis()),
                            uri = image.toString(),
                            result = it
                        )
                        lifecycleScope.launch(Dispatchers.IO) {
                            runOnUiThread {
                                viewModel.tambahRiwayat(dataHasil)
                                moveToResult(image, it)
                            }
                        }
                    }
                }
            }
        )
        imageHelper.classifyStaticImage(image)
    }

    private fun moveToResult(image: Uri, result: String) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_IMAGE_URI, image.toString())
            putExtra(ResultActivity.EXTRA_RESULT, result)
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun millisToDate(millis: Long): String {
        val formatTanggal = SimpleDateFormat("yyyy-MM-dd | HH:mm", Locale.getDefault())
        val kalender = Calendar.getInstance()
        kalender.timeInMillis = millis
        return formatTanggal.format(kalender.time)
    }
}