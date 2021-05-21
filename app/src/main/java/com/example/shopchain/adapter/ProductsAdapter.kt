package com.example.shopchain.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopchain.R
import com.example.shopchain.model.Products

class ProductsAdapter(val context: Context, val productList: ArrayList<Products>):
        RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imgProduct: ImageView = view.findViewById(R.id.imgProdImage)
        val txtProductName: TextView = view.findViewById(R.id.txtProdName)
        val txtProductPrice: TextView = view.findViewById(R.id.txtProdPrice)
        val txtProductStatusAvailable: TextView = view.findViewById(R.id.txtProdAvailable)
        val txtProductStatusUnavailable: TextView = view.findViewById(R.id.txtProdUnavailable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_single_row, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val prod = productList[position]
        holder.txtProductName.text = prod.pName
        holder.txtProductPrice.text = prod.pPrice
        if (prod.pStatus) {
            holder.txtProductStatusAvailable.visibility = View.VISIBLE
            holder.txtProductStatusUnavailable.visibility = View.GONE
        } else {
            holder.txtProductStatusAvailable.visibility = View.GONE
            holder.txtProductStatusUnavailable.visibility = View.VISIBLE
        }
        if (prod.pImageUrl.isNotBlank()) {
            Glide.with(context).load(prod.pImageUrl).into(holder.imgProduct)
        } else {
            holder.imgProduct.setImageResource(R.drawable.default_prod_image)
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}