package com.example.shopchain.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopchain.R
import com.example.shopchain.model.Reviews

class ReviewsAdapter(val context: Context, val reviewList: ArrayList<Reviews>):
        RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val txtTitle: TextView = view.findViewById(R.id.txtReviewTitle)
        val txtReview: TextView = view.findViewById(R.id.txtReview)
        val txtRating: TextView = view.findViewById(R.id.txtReviewRatings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reviews_single_row, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val rev = reviewList[position]
        holder.txtTitle.text = rev.title
        holder.txtReview.text = rev.review
        holder.txtRating.text = rev.rating
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }
}