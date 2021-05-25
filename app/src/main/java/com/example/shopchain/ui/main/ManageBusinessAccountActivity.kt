package com.example.shopchain.ui.main

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
import com.example.shopchain.model.Products
import com.example.shopchain.model.Reviews
import com.example.shopchain.model.Shops
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManageBusinessAccountActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var txtStoreName: TextView
    lateinit var txtStoreDesc: TextView
    lateinit var imgStore: ImageView
    lateinit var txtStoreRating: TextView
    lateinit var txtStoreStatus: TextView
    lateinit var txtStoreTiming: TextView
    lateinit var txtStoreAddress: TextView
    lateinit var recyclerProducts: RecyclerView
    lateinit var recyclerReviews: RecyclerView
    lateinit var rlEditDetails: RelativeLayout
    lateinit var etEdit: EditText
    lateinit var btnSubmit: Button
    lateinit var btnCancel: Button
    lateinit var productAdapter: ProductsAdapter
    lateinit var revAdapter: ReviewsAdapter
    private var productList = ArrayList<Products>()
    private var reviewList = ArrayList<Reviews>()
    private var shopObj: Shops? = null
    private var EDIT_SHOP_NAME_CODE = 1000
    private var EDIT_SHOP_DESC_CODE = 1001
    private var EDIT_SHOP_TIMING_CODE = 1002
    private var EDIT_SHOP_ADDRESS_CODE = 1003
    private var CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_business_account)

        toolbar = findViewById(R.id.toolbarStore)
        txtStoreName = findViewById(R.id.tvStoreName)
        txtStoreDesc = findViewById(R.id.tvStoreDesc)
        imgStore = findViewById(R.id.ivStoreImage)
        txtStoreRating = findViewById(R.id.txtStoreRatings)
        txtStoreStatus = findViewById(R.id.txtStoreStatus)
        txtStoreTiming = findViewById(R.id.txtTimings)
        txtStoreAddress = findViewById(R.id.txtAddress)
        rlEditDetails = findViewById(R.id.rlEditDetails)
        etEdit = findViewById(R.id.etEdit)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnCancel = findViewById(R.id.btnCancel)
        recyclerProducts = findViewById(R.id.recyclerManageProducts)
        recyclerReviews = findViewById(R.id.recyclerManageReviews)

        loadStoreDetails()

        txtStoreName.setOnClickListener {
            rlEditDetails.visibility = View.VISIBLE
            etEdit.setText(shopObj?.shopName)
            etEdit.hint = "Shop Name"
            CODE = EDIT_SHOP_NAME_CODE
        }

        txtStoreDesc.setOnClickListener {
            rlEditDetails.visibility = View.VISIBLE
            etEdit.setText(shopObj?.description)
            etEdit.hint = "Describe your store (max 200 words)"
            CODE = EDIT_SHOP_DESC_CODE
        }

        txtStoreStatus.setOnClickListener {
            val uid = FirebaseAuth.getInstance().uid ?: ""
            val ref = FirebaseDatabase.getInstance().getReference("/shops/$uid")
            val options = arrayOf<CharSequence>("Open", "Closed")
            val optionDialog = android.app.AlertDialog.Builder(this)
            optionDialog.setTitle("Set Status")
            optionDialog.setItems(options, { dialog, item ->
                if (options[item] == "Open") {
                    ref.child("currentStatus").setValue("Open").addOnSuccessListener {
                        Toast.makeText(this, "Saved!!", Toast.LENGTH_SHORT).show()
                        txtStoreStatus.text = "Open"
                        txtStoreStatus.setTextColor(resources.getColor(R.color.opened))
                    }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed! Try again later.", Toast.LENGTH_SHORT).show()
                            }
                } else if (options[item] == "Closed") {
                    ref.child("currentStatus").setValue("Closed").addOnSuccessListener {
                        Toast.makeText(this, "Saved!!", Toast.LENGTH_SHORT).show()
                        txtStoreStatus.text = "Closed"
                        txtStoreStatus.setTextColor(resources.getColor(R.color.closed))
                    }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed! Try again later.", Toast.LENGTH_SHORT).show()
                            }
                }
            })
            optionDialog.create()
            optionDialog.show()
        }

        txtStoreTiming.setOnClickListener {
            rlEditDetails.visibility = View.VISIBLE
            etEdit.setText(shopObj?.timing)
            etEdit.hint = "Working Time of Shop"
            CODE = EDIT_SHOP_TIMING_CODE
        }

        txtStoreAddress.setOnClickListener {
            rlEditDetails.visibility = View.VISIBLE
            etEdit.setText(shopObj?.address)
            etEdit.hint = "Shop Address"
            CODE = EDIT_SHOP_ADDRESS_CODE
        }

        btnCancel.setOnClickListener {
            rlEditDetails.visibility = View.GONE
        }

        btnSubmit.setOnClickListener {
            if (etEdit.text.toString().isNotBlank()) {
                saveEditedDetails()
            } else {
                Toast.makeText(this, "Text field can't be empty.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveEditedDetails() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/shops/$uid")
        val text = etEdit.text.toString()
        when (CODE) {
            EDIT_SHOP_NAME_CODE -> {
                ref.child("shopName").setValue(text).addOnSuccessListener {
                    Toast.makeText(this, "Saved!!", Toast.LENGTH_SHORT).show()
                    rlEditDetails.visibility = View.GONE
                    txtStoreName.text = text
                }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed! Try again later.", Toast.LENGTH_SHORT).show()
                        }
            }
            EDIT_SHOP_DESC_CODE -> {
                ref.child("description").setValue(etEdit.text.toString()).addOnSuccessListener {
                    Toast.makeText(this, "Saved!!", Toast.LENGTH_SHORT).show()
                    rlEditDetails.visibility = View.GONE
                    txtStoreDesc.text = text
                }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed! Try again later.", Toast.LENGTH_SHORT).show()

                        }
            }
            EDIT_SHOP_TIMING_CODE -> {
                ref.child("timing").setValue(etEdit.text.toString()).addOnSuccessListener {
                    Toast.makeText(this, "Saved!!", Toast.LENGTH_SHORT).show()
                    rlEditDetails.visibility = View.GONE
                    txtStoreTiming.text = text
                }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed! Try again later.", Toast.LENGTH_SHORT).show()

                        }
            }
            EDIT_SHOP_ADDRESS_CODE -> {
                ref.child("address").setValue(etEdit.text.toString()).addOnSuccessListener {
                    Toast.makeText(this, "Saved!!", Toast.LENGTH_SHORT).show()
                    rlEditDetails.visibility = View.GONE
                    txtStoreAddress.text = text
                }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed! Try again later.", Toast.LENGTH_SHORT).show()

                        }
            }
            else -> {
                Toast.makeText(this, "Failed! Please try again later.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpToolbar(shopName: String?) {
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = shopName
    }

    private fun loadStoreDetails() {
        val uid = FirebaseAuth.getInstance().uid
        var ref = FirebaseDatabase.getInstance().getReference("/shops/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                val shop = snapshot.getValue(Shops::class.java)
                if (shop != null) {
                    shopObj = shop
                    txtStoreName.text = shop.shopName
                    txtStoreDesc.text = shop.description
                    txtStoreRating.text = shop.rating
                    if (shop.shopImageUrl.isNotBlank()) {
                        Glide.with(this@ManageBusinessAccountActivity).load(shop.shopImageUrl).into(imgStore)
                    } else {
                        imgStore.setImageResource(R.drawable.default_store_image)
                    }
                    if (shop.currentStatus == "Closed") {
                        txtStoreStatus.text = shop.currentStatus
                        txtStoreStatus.setTextColor(resources.getColor(R.color.closed))
                    } else {
                        txtStoreStatus.text = shop.currentStatus
                        txtStoreStatus.setTextColor(resources.getColor(R.color.opened))
                    }
                    txtStoreTiming.text = shop.timing
                    txtStoreAddress.text = shop.address
                    setUpToolbar(shop.shopName)
                }
            }
        })

        ref = FirebaseDatabase.getInstance().getReference("/products/$uid")
        Log.d("Check", "load products and reviews, path = ${ref.ref}")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    Log.d("Check", "product check")
                    val prod = it.getValue(Products::class.java)
                    if (prod != null) {
                        productList.add(prod)
                        productAdapter = ProductsAdapter(this@ManageBusinessAccountActivity, productList, 1)
                        recyclerProducts.adapter = productAdapter
                    }
                }
            }
        })

        ref = FirebaseDatabase.getInstance().getReference("/reviews/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    Log.d("Check", "review check")
                    val rev = it.getValue(Reviews::class.java)
                    if (rev != null) {
                        reviewList.add(rev)
                        revAdapter = ReviewsAdapter(this@ManageBusinessAccountActivity, reviewList)
                        recyclerReviews.adapter = revAdapter
                    }
                }
            }
        })
    }
}