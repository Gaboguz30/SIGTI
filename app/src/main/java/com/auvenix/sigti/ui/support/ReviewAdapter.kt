package com.auvenix.sigti.ui.support

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R

data class Review(
    val name: String,
    val date: String,
    val text: String,
    val rating: Double = 0.0
)

class ReviewAdapter(
    private val reviews: List<Review>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReviewerName: TextView = itemView.findViewById(R.id.tvReviewerName)
        val tvReviewRating: TextView = itemView.findViewById(R.id.tvReviewRating)
        val tvDate: TextView = itemView.findViewById(R.id.tvReviewDate)
        val tvComment: TextView = itemView.findViewById(R.id.tvReviewComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.tvReviewerName.text = review.name
        holder.tvReviewRating.text = if (review.rating > 0) String.format("%.1f", review.rating) else "—"
        holder.tvDate.text = review.date
        holder.tvComment.text = review.text
    }

    override fun getItemCount(): Int = reviews.size
}