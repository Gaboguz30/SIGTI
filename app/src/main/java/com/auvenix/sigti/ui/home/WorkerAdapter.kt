package com.auvenix.sigti.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.auvenix.sigti.databinding.ItemWorkerBinding

class WorkerAdapter(
    private var workers: List<Worker>,
    private val onProfileClick: (Worker) -> Unit
) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    fun updateList(newList: List<Worker>) {
        workers = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val binding = ItemWorkerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorkerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        holder.bind(workers[position])
    }

    override fun getItemCount(): Int = workers.size

    inner class WorkerViewHolder(
        private val binding: ItemWorkerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(worker: Worker) {
            binding.tvWorkerName.text = worker.name
            binding.tvJob.text = worker.job
            binding.tvAvailability.text = "Disponibilidad: ${worker.availability}"
            binding.tvRating.text = worker.rating.toString()
            binding.tvDistance.text = worker.distance
            binding.tvPrice.text = worker.price

            if (worker.isSelected) {
                binding.cardWorker.strokeWidth = 4
                binding.cardWorker.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.blue_light_bg)
                )
            } else {
                binding.cardWorker.strokeWidth = 0
                binding.cardWorker.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.white)
                )
            }

            binding.btnViewProfile.setOnClickListener {
                onProfileClick(worker)
            }
        }
    }
}