package com.adismoyam.diagnocar.view

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.adismoyam.diagnocar.ArticleAdapter
import com.adismoyam.diagnocar.R
import com.adismoyam.diagnocar.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    private val viewModel: ResultViewModel by viewModels()
    private val adapter = ArticleAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActionBar()

        // Menampilkan hasil gambar, prediksi, dan confidence score.
        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
        val result = intent.getStringExtra(EXTRA_RESULT)

        Log.d("Image URI", "showImage: $imageUri")
        Log.d("Result", "showResult: $result")

        binding.resultImage.setImageURI(imageUri)
        binding.resultText.text = "Hasil Analisis  : $result"


        val layoutManager = LinearLayoutManager(this)
        binding.rvArticle.layoutManager = layoutManager
        binding.rvArticle.adapter = adapter

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.articlesData.observe(this) { articles ->
            Log.d("ResultActivity", articles.toString())
            if (articles != null) {
                adapter.submitList(articles)
                hideError()
            }
        }
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage != null && viewModel.articlesData.value == null) {
                showArticlesError(errorMessage)
            } else {
                hideError()
            }
        }
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
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
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
        return super.onOptionsItemSelected(item)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showArticlesError(message: String) {
        binding.apply {
            tvErrorMessage.text = message
            tvErrorMessage.visibility = View.VISIBLE
            if (viewModel.articlesData.value.isNullOrEmpty()) {
                rvArticle.visibility = View.INVISIBLE
            }
        }
    }

    private fun hideError() {
        binding.tvErrorMessage.visibility = View.GONE
    }


    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
    }
}