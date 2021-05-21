package com.example.shopchain.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopchain.R
import com.example.shopchain.model.LikedShops
import com.example.shopchain.model.Shops
import com.example.shopchain.ui.main.StoreActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LikedStoresAdapter(val context: Context, val likedStoreList: ArrayList<LikedShops>):
        RecyclerView.Adapter<LikedStoresAdapter.LikedStoresViewHolder>() {

    class LikedStoresViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val txtStoreName: TextView = view.findViewById(R.id.txtFavStoreName)
        val txtStoreType: TextView = view.findViewById(R.id.txtFavStoreType)
        val txtStoreStatusOpen: TextView = view.findViewById(R.id.txtFavStoreStatusOpen)
        val txtStoreStatusClose: TextView = view.findViewById(R.id.txtFavStoreStatusClosed)
        val txtStoreRating: TextView = view.findViewById(R.id.txtFavStoreRating)
        val imgStoreImage: ImageView = view.findViewById(R.id.imgFavStoreImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikedStoresViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.liked_stores_single_row, parent, false)
        return LikedStoresViewHolder(view)
    }

    override fun onBindViewHolder(holder: LikedStoresViewHolder, position: Int) {
        val liked_shops = likedStoreList[position]
        var shops: Shops? = null
        val ref = FirebaseDatabase.getInstance().getReference("/shops/${liked_shops.sid}")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val shop = snapshot.getValue(Shops::class.java)
                if (shop != null) {
                    shops = shop
                    holder.txtStoreName.text = shop.shopName
                    holder.txtStoreType.text = shop.shopType
                    holder.txtStoreRating.text = shop.rating
                    if (shop.shopImageUrl.isNotBlank()) {
                        Glide.with(context).load(shop.shopImageUrl).into(holder.imgStoreImage)
                    } else {
                        holder.imgStoreImage.setImageResource(R.drawable.default_store_image)
                    }
                    if (shop.currentStatus == "Open") {
                        holder.txtStoreStatusOpen.visibility = View.VISIBLE
                        holder.txtStoreStatusClose.visibility = View.GONE

                    } else {
                        holder.txtStoreStatusOpen.visibility = View.GONE
                        holder.txtStoreStatusClose.visibility = View.VISIBLE
                    }
                }
            }
        })

        holder.itemView.setOnClickListener {
            val intent = Intent(context, StoreActivity::class.java)
            intent.putExtra("SHOP_NAME", shops?.shopName)
            intent.putExtra("SHOP_ID", shops?.sid)
            intent.putExtra("SHOP_IMAGE", shops?.shopImageUrl)
            intent.putExtra("SHOP_MOBILE", shops?.mobile)
            intent.putExtra("SHOP_DESC", shops?.description)
            intent.putExtra("SHOP_RATING", shops?.rating)
            intent.putExtra("SHOP_TYPE", shops?.shopType)
            intent.putExtra("SHOP_TIMING", shops?.timing)
            intent.putExtra("SHOP_STATUS", shops?.currentStatus)
            intent.putExtra("SHOP_ADDRESS", shops?.address)
            intent.putExtra("SHOP_LAT", shops?.lat)
            intent.putExtra("SHOP_LONG", shops?.long)
            intent.putExtra("SHOP_CITY", shops?.city)
            intent.putExtra("SHOP_COUNTRY", shops?.country)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return likedStoreList.size
    }
}