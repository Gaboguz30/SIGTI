package com.auvenix.sigti.ui.support

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auvenix.sigti.R
import com.google.android.material.imageview.ShapeableImageView
// Importar Glide o Picasso si vas a cargar fotos reales desde URL
// import com.bumptech.glide.Glide

// 2. EL ADAPTADOR
class ReviewAdapter(
    private val reviewList: List<Review>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviewList[position])
    }

    override fun getItemCount(): Int = reviewList.size

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUser: TextView = itemView.findViewById(R.id.tvReviewUser)
        private val tvDate: TextView = itemView.findViewById(R.id.tvReviewDate)
        private val tvComment: TextView = itemView.findViewById(R.id.tvReviewComment)
        private val rbStars: RatingBar = itemView.findViewById(R.id.rbReviewStars)
        private val ivPhoto: ShapeableImageView = itemView.findViewById(R.id.ivReviewPhoto)

        fun bind(review: Review) {
            tvUser.text = review.user
            tvDate.text = review.date
            tvComment.text = review.comment
            rbStars.rating = review.rating

            // 🔥 MAGIA DE LA FOTO: Si no hay foto, colapsamos el espacio
            if (review.imageUrl.isNullOrEmpty()) {
                ivPhoto.visibility = View.GONE
            } else {
                ivPhoto.visibility = View.VISIBLE
                // TODO: Aquí meterás Glide cuando conectes Firebase Storage
                // Glide.with(itemView.context).load(review.imageUrl).into(ivPhoto)
                ivPhoto.visibility = View.VISIBLE // Aseguramos que se vea
                com.bumptech.glide.Glide.with(itemView.context)
                    .load(review.imageUrl)
                    .centerCrop()
                    .into(ivPhoto)
            }
        }
    }
}