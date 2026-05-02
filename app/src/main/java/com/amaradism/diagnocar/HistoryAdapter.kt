package com.amaradism.diagnocar

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.amaradism.diagnocar.data.history.HistoryEntity
import com.amaradism.diagnocar.databinding.HistoryCardBinding
import com.amaradism.diagnocar.view.ResultActivity

class HistoryAdapter : ListAdapter<HistoryEntity, HistoryAdapter.MyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = HistoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val history = getItem(position)
        holder.bind(history)
    }

    class MyViewHolder(private val binding: HistoryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(history: HistoryEntity) {
            binding.apply {
                Glide.with(root.context)
                    .load(history.uri)
                    .into(ivCancer)

                tvLabel.text = history.result
                tvTime.text = history.date

                root.setOnClickListener {
                    val intent = Intent(root.context, ResultActivity::class.java)
                    intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, history.uri)
                    intent.putExtra(ResultActivity.EXTRA_RESULT, history.result)
                    root.context.startActivity(intent)
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HistoryEntity>() {
            override fun areItemsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: HistoryEntity,
                newItem: HistoryEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}