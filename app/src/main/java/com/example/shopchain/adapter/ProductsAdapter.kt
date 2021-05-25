package com.example.shopchain.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopchain.R
import com.example.shopchain.model.Products
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProductsAdapter(val context: Context, val productList: ArrayList<Products>, val check: Int = 0):
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

        holder.itemView.setOnClickListener {
            if (check == 1) {
                val uid = FirebaseAuth.getInstance().uid ?: ""
                val ref = FirebaseDatabase.getInstance().getReference("/products/$uid/${prod.pid}")
                val options = arrayOf<CharSequence>(context.resources.getString(R.string.in_stock), context.resources.getString(R.string.out_of_stock))
                val optionDialog = android.app.AlertDialog.Builder(context)
                optionDialog.setTitle("Set Availability")
                optionDialog.setItems(options, { dialog, item ->
                    if (options[item] == context.resources.getString(R.string.in_stock)) {
                        ref.child("pStatus").setValue(true)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Saved!!", Toast.LENGTH_SHORT).show()
                                    holder.txtProductStatusAvailable.visibility = View.VISIBLE
                                    holder.txtProductStatusUnavailable.visibility = View.GONE
                                 }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed! Try again later.", Toast.LENGTH_SHORT).show()
                                }
                    } else if (options[item] == context.resources.getString(R.string.out_of_stock)) {
                        ref.child("pStatus").setValue(false)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Saved!!", Toast.LENGTH_SHORT).show()
                                    holder.txtProductStatusAvailable.visibility = View.GONE
                                    holder.txtProductStatusUnavailable.visibility = View.VISIBLE
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed! Try again later.", Toast.LENGTH_SHORT).show()
                                }
                    }
                })
                optionDialog.create()
                optionDialog.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}