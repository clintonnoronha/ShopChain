package com.example.shopchain.ui.main

import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.shopchain.R
import com.example.shopchain.adapter.LatestChatAdapter
import com.example.shopchain.model.ChatMessages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class LatestMessagesActivity : AppCompatActivity() {

    lateinit var latestChatRecycler: RecyclerView
    lateinit var toolbar: Toolbar
    private val displayList = ArrayList<ChatMessages>()
    private val tempList = ArrayList<ChatMessages>()
    var adapter: LatestChatAdapter = LatestChatAdapter(this, displayList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        toolbar = findViewById(R.id.toolbarLatestMessage)
        latestChatRecycler = findViewById(R.id.latestMessagesRecyclerView)

        setUpToolbar()

        latestChatRecycler.adapter = adapter

        listenForLatestChat()

    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Recent Chats"
    }

    val latestMessageMap = HashMap<String, ChatMessages>()

    private fun listenForLatestChat() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference(
                "/latest-message/$fromId"
        )
        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {

            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }
            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val latestChatMessage = snapshot.getValue(ChatMessages::class.java) ?: return
                displayList.clear()
                latestMessageMap[snapshot.key!!] = latestChatMessage
                latestMessageMap.values.forEach {
                    displayList.add(it)
                }
                latestChatRecycler.adapter?.notifyDataSetChanged()
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val latestChatMessage = snapshot.getValue(ChatMessages::class.java) ?: return
                tempList.add(latestChatMessage)
                latestMessageMap[snapshot.key!!] = latestChatMessage
                displayList.clear()
                latestMessageMap.values.forEach {
                    displayList.add(it)
                }
                latestChatRecycler.adapter?.notifyDataSetChanged()
            }
        })
    }

}