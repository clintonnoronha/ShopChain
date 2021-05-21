package com.example.shopchain.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.shopchain.R
import com.example.shopchain.adapter.LikedStoresAdapter
import com.example.shopchain.model.LikedShops
import com.example.shopchain.model.Shops
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SavedStoresFragment : Fragment() {

    lateinit var recyclerLikedStores: RecyclerView
    private val displayList = ArrayList<LikedShops>()
    lateinit var adapter: LikedStoresAdapter
    lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_saved_stores, container, false)

        recyclerLikedStores = view.findViewById(R.id.recyclerLikedStores)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/liked-shops/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val liked_shop = it.getValue(LikedShops::class.java)
                    if (liked_shop != null) {
                        displayList.add(liked_shop)
                        adapter = LikedStoresAdapter(requireContext(), displayList)
                        recyclerLikedStores.adapter = adapter
                    }
                }
                progressLayout.visibility = View.GONE
            }
        })

        return view
    }

}
