package com.example.shopchain.adapter

import android.content.Context
import android.content.Intent
import android.drm.DrmStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopchain.R
import com.example.shopchain.model.Shops
import com.example.shopchain.model.User
import com.example.shopchain.ui.main.StoreActivity

class StoresAdapter (val context: Context, private val shopsArrayList: ArrayList<Shops>) :
        RecyclerView.Adapter<StoresAdapter.StoresViewHolder>() {

    class StoresViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val txtStoreName: TextView = view.findViewById(R.id.txtStoreName)
        val txtStoreType: TextView = view.findViewById(R.id.txtStoreType)
        val imgStoreImage: ImageView = view.findViewById(R.id.imgStoreImage)
        val txtRating: TextView = view.findViewById(R.id.txtRatings)
        val txtStatusOpen: TextView = view.findViewById(R.id.txtOpen)
        val txtStatusClosed: TextView = view.findViewById(R.id.txtClosed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoresViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.discover_stores_single_row, parent,false)
        return StoresViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoresViewHolder, position: Int) {
        val shops = shopsArrayList[position]
        holder.txtStoreName.text = shops.shopName
        holder.txtStoreType.text = shops.shopType
        holder.txtRating.text = shops.rating
        if (shops.shopImageUrl.isNotEmpty()) {
            Glide.with(context).load(shops.shopImageUrl).into(holder.imgStoreImage)
        } else {
            holder.imgStoreImage.setImageResource(R.drawable.default_store_image)
        }
        if (shops.currentStatus == "Open") {
            holder.txtStatusOpen.visibility = View.VISIBLE
            holder.txtStatusClosed.visibility = View.GONE
        } else {
            holder.txtStatusOpen.visibility = View.GONE
            holder.txtStatusClosed.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, StoreActivity::class.java)
            intent.putExtra("SHOP_NAME", shops.shopName)
            intent.putExtra("SHOP_ID", shops.sid)
            intent.putExtra("SHOP_IMAGE", shops.shopImageUrl)
            intent.putExtra("SHOP_MOBILE", shops.mobile)
            intent.putExtra("SHOP_DESC", shops.description)
            intent.putExtra("SHOP_RATING", shops.rating)
            intent.putExtra("SHOP_TYPE", shops.shopType)
            intent.putExtra("SHOP_TIMING", shops.timing)
            intent.putExtra("SHOP_STATUS", shops.currentStatus)
            intent.putExtra("SHOP_ADDRESS", shops.address)
            intent.putExtra("SHOP_LAT", shops.lat)
            intent.putExtra("SHOP_LONG", shops.long)
            intent.putExtra("SHOP_CITY", shops.city)
            intent.putExtra("SHOP_COUNTRY", shops.country)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return shopsArrayList.size
    }
}