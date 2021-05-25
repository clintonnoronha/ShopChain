package com.example.shopchain.ui.main

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopchain.R
import com.example.shopchain.adapter.ProductsAdapter
import com.example.shopchain.adapter.ReviewsAdapter
import com.example.shopchain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StoreActivity : AppCompatActivity() {

    lateinit var btnCallNow: Button
    lateinit var btnChatNow: Button
    lateinit var imgBtnSave: ImageButton
    lateinit var imgBtnUnsave: ImageButton
    lateinit var imgBtnReview: ImageView
    lateinit var txtSave: TextView
    lateinit var txtUnsave: TextView
    lateinit var txtStoreName: TextView
    lateinit var txtStoreDesc: TextView
    lateinit var imgStoreImage: ImageView
    lateinit var txtStoreRating: TextView
    lateinit var txtStoreTiming: TextView
    lateinit var txtStoreAddress: TextView
    lateinit var txtStoreStatus: TextView
    lateinit var toolbar: Toolbar
    lateinit var spnRating: Spinner
    lateinit var etTitle: EditText
    lateinit var etReview: EditText
    lateinit var btnSubmit: Button
    lateinit var btnCancel: Button
    lateinit var rlWriteReview: RelativeLayout
    private var storeNumber: String? = null
    private var storeName: String? = null
    private var storeId: String? = null
    private var storeTiming: String? = null
    private var storeRatings: String? = null
    private var storeDesc: String? = null
    private var storeImage: String? = null
    private var storeType: String? = null
    private var storeStatus: String? = null
    private var storeAddress: String? = null
    private var storeLatitude: Double? = null
    private var storeLongitude: Double? = null
    private var user: User? = null
    private var uid: String? = null
    lateinit var recyclerProducts: RecyclerView
    private val productList = ArrayList<Products>()
    lateinit var adapter: ProductsAdapter
    lateinit var recyclerReviews: RecyclerView
    private val reviewList = ArrayList<Reviews>()
    lateinit var revAdapter: ReviewsAdapter
    private var rating: String? = ""
    private var flag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)

        storeNumber = intent.getStringExtra("SHOP_MOBILE")
        storeName = intent.getStringExtra("SHOP_NAME")
        storeId = intent.getStringExtra("SHOP_ID")
        storeTiming = intent.getStringExtra("SHOP_TIMING")
        storeRatings = intent.getStringExtra("SHOP_RATING")
        storeDesc = intent.getStringExtra("SHOP_DESC")
        storeImage = intent.getStringExtra("SHOP_IMAGE")
        storeType = intent.getStringExtra("SHOP_TYPE")
        storeStatus = intent.getStringExtra("SHOP_STATUS")
        storeAddress = intent.getStringExtra("SHOP_ADDRESS")
        storeLatitude = intent.getDoubleExtra("SHOP_LAT", 0.0)
        storeLongitude = intent.getDoubleExtra("SHOP_LONG", 0.0)

        btnCallNow = findViewById(R.id.btnCallNow)
        btnChatNow = findViewById(R.id.btnChatNow)
        txtStoreName = findViewById(R.id.tvStoreName)
        txtStoreDesc = findViewById(R.id.tvStoreDesc)
        imgStoreImage = findViewById(R.id.ivStoreImage)
        txtStoreRating = findViewById(R.id.txtStoreRatings)
        txtStoreTiming = findViewById(R.id.txtTimings)
        txtStoreAddress = findViewById(R.id.txtAddress)
        txtStoreStatus = findViewById(R.id.txtStoreStatus)
        spnRating = findViewById(R.id.spnSelectRating)
        rlWriteReview = findViewById(R.id.rlWriteReview)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnCancel = findViewById(R.id.btnCancel)
        etTitle = findViewById(R.id.etTitle)
        etReview = findViewById(R.id.etReview)
        recyclerProducts = findViewById(R.id.recyclerProducts)
        recyclerReviews = findViewById(R.id.recyclerReviews)
        imgBtnSave = findViewById(R.id.imgBtnSave)
        imgBtnUnsave = findViewById(R.id.imgBtnUnsave)
        imgBtnReview = findViewById(R.id.imgBtnReview)
        txtSave = findViewById(R.id.txtSave)
        txtUnsave = findViewById(R.id.txtUnsave)
        toolbar = findViewById(R.id.toolbarStoreDetails)
        uid = FirebaseAuth.getInstance().uid ?: ""

        val arrayAdapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, CountryCodes.ratings)
        spnRating.adapter = arrayAdapter
        spnRating.setSelection(arrayAdapter.getPosition("5"))

        spnRating.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                rating = CountryCodes.ratings.get(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Code to perform some action when nothing is selected
            }
        }

        loadDetails()

        val ref = FirebaseDatabase.getInstance().getReference("/users/$storeId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)
            }

        })

        txtStoreAddress.setOnClickListener {
            // Creates an Intent that will load a map of shop location
            val gmmIntentUri = Uri.parse("geo:$storeLatitude,$storeLongitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        btnCallNow.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$storeNumber")
            startActivity(intent)
        }

        btnChatNow.setOnClickListener {
            if (user != null) {
                val intent = Intent(this@StoreActivity, ChatLogActivity::class.java)
                intent.putExtra("CHAT_NAME", user?.username)
                intent.putExtra("CHAT_ID", user?.uid)
                intent.putExtra("CHAT_IMAGE", user?.profileImageUrl)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Some error occurred! Try again later.", Toast.LENGTH_SHORT).show()
            }
        }

        imgBtnSave.setOnClickListener {
            val reff = FirebaseDatabase.getInstance().getReference("/liked-shops/$uid/$storeId")
            val liked_shop = LikedShops(sid = storeId!!)
            reff.setValue(liked_shop)
                    .addOnSuccessListener {
                        imgBtnSave.visibility = View.GONE
                        txtSave.visibility = View.GONE
                        imgBtnUnsave.visibility = View.VISIBLE
                        txtUnsave.visibility = View.VISIBLE
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                                this,
                                "Failed! Please try again later.",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
        }

        imgBtnUnsave.setOnClickListener {
            val reff = FirebaseDatabase.getInstance().getReference("/liked-shops/$uid")
            reff.child(storeId!!).removeValue()
                    .addOnSuccessListener {
                        imgBtnSave.visibility = View.VISIBLE
                        txtSave.visibility = View.VISIBLE
                        imgBtnUnsave.visibility = View.GONE
                        txtUnsave.visibility = View.GONE
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                                this,
                                "Failed! Please try again later.",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
        }

        imgBtnReview.setOnClickListener {
            rlWriteReview.visibility = View.VISIBLE
        }

        btnCancel.setOnClickListener {
            rlWriteReview.visibility = View.GONE
        }

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString()
            val review = etReview.text.toString()
            if (title.isNotBlank() && review.isNotBlank() && rating != "") {
                submitReview(title, review, rating!!)
            } else {
                Toast.makeText(this,
                        "Please fill in all details.",
                        Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    private fun submitReview(title: String, review: String, rating: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/reviews/$storeId/$uid")
        val rev = Reviews(
                uid = uid!!,
                title = title,
                review = review,
                rating = rating
        )
        ref.setValue(rev).addOnSuccessListener {
            Toast.makeText(this, "Review Uploaded!", Toast.LENGTH_SHORT).show()
            rlWriteReview.visibility = View.GONE
        }.addOnFailureListener {
            Toast.makeText(this, "Failed! Try again later.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDetails() {
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = storeName
        txtStoreName.text = storeName
        txtStoreDesc.text = storeDesc
        txtStoreRating.text = storeRatings
        txtStoreStatus.text = storeStatus
        if (storeStatus == "Closed") {
            txtStoreStatus.setTextColor(resources.getColor(R.color.closed))
        }
        txtStoreTiming.text = storeTiming
        txtStoreAddress.text = storeAddress
        if (storeImage!!.isNotBlank()) {
            Glide.with(this).load(storeImage).into(imgStoreImage)
        } else {
            imgStoreImage.setImageResource(R.drawable.default_store_image)
        }

        var ref = FirebaseDatabase.getInstance().getReference("/liked-shops/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(storeId!!)) {
                    imgBtnSave.visibility = View.GONE
                    txtSave.visibility = View.GONE
                    imgBtnUnsave.visibility = View.VISIBLE
                    txtUnsave.visibility = View.VISIBLE
                } else {
                    imgBtnSave.visibility = View.VISIBLE
                    txtSave.visibility = View.VISIBLE
                    imgBtnUnsave.visibility = View.GONE
                    txtUnsave.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

        ref = FirebaseDatabase.getInstance().getReference("/products/$storeId")
        Log.d("Check", "load products and reviews, path = ${ref.ref}")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val prod = it.getValue(Products::class.java)
                    if (prod != null) {
                        productList.add(prod)
                        adapter = ProductsAdapter(this@StoreActivity, productList)
                        recyclerProducts.adapter = adapter
                    }
                }
            }
        })

        ref = FirebaseDatabase.getInstance().getReference("/reviews/$storeId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val rev = it.getValue(Reviews::class.java)
                    if (rev != null) {
                        reviewList.add(rev)
                        revAdapter = ReviewsAdapter(this@StoreActivity, reviewList)
                        recyclerReviews.adapter = revAdapter
                    }
                }
            }
        })
    }
}