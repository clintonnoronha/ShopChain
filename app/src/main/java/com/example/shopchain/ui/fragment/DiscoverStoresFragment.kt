package com.example.shopchain.ui.fragment

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopchain.R
import com.example.shopchain.adapter.LatestChatAdapter
import com.example.shopchain.adapter.StoresAdapter
import com.example.shopchain.model.ChatMessages
import com.example.shopchain.model.Shops
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DiscoverStoresFragment : Fragment() {

    lateinit var recyclerDiscoverStores: RecyclerView
    private val displayList = ArrayList<Shops>()
    private val tempList = ArrayList<Shops>()
    lateinit var adapter: StoresAdapter
    lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_discover_stores, container, false)

        recyclerDiscoverStores = view.findViewById(R.id.recyclerDiscover)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)
        progressLayout.visibility = View.VISIBLE

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/shops")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val shop = it.getValue(Shops::class.java)
                    if (shop != null && shop.sid != uid) {
                        tempList.add(shop)
                        displayList.add(shop)
                        adapter = StoresAdapter(requireContext(), displayList)
                        recyclerDiscoverStores.adapter = adapter
                    }
                }
                progressLayout.visibility = View.GONE
            }
        })

        return view
    }
}
