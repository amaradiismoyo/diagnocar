package com.adismoyam.diagnocar.view

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.adismoyam.diagnocar.HistoryAdapter
import com.adismoyam.diagnocar.R
import com.adismoyam.diagnocar.ViewModelFactory
import com.adismoyam.diagnocar.data.history.HistoryEntity
import com.adismoyam.diagnocar.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: HistoryViewModel by viewModels {
        ViewModelFactory.getInstance(application)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setCustomView(R.layout.app_bar)
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)
            setBackgroundDrawable(ColorDrawable(getColor(R.color.Primary)))
        }

        val layoutManager = LinearLayoutManager(this)
        binding.rvHistory.layoutManager = layoutManager

        viewModel.historyList.observe(this) {
            setHistoryData(it)
        }
    }

    private fun setHistoryData(consumer: List<HistoryEntity>) {
        val adapter = HistoryAdapter()
        adapter.submitList(consumer)
        binding.rvHistory.adapter = adapter
    }
}