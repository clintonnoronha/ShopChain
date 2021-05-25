package com.example.shopchain.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.shopchain.R
import com.example.shopchain.model.CountryCodes
import com.example.shopchain.model.Shops
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
import java.util.*

class CreateBusinessAccountActivity : AppCompatActivity() {

    lateinit var fabSelectPhoto: FloatingActionButton
    lateinit var etStoreName: EditText
    lateinit var etStoreContactNumber: EditText
    lateinit var etStoreAddress: EditText
    lateinit var imgStore: CircleImageView
    lateinit var imgBtnShowMap: ImageButton
    lateinit var btnFinishSetup: Button
    lateinit var spnStoreCategory: Spinner
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    private var type: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var city: String? = null
    private var country: String? = null
    private val PERMISSION_ID_LOC = 52
    private val PERMISSION_ID_STORAGE = 50
    private val OPEN_IMAGE_STORAGE = 51
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_business_account)

        fabSelectPhoto = findViewById(R.id.fabSelectPhoto)
        imgStore = findViewById(R.id.imgStore)
        etStoreName = findViewById(R.id.et_shop_name)
        etStoreContactNumber = findViewById(R.id.et_shop_contact_number)
        etStoreAddress = findViewById(R.id.et_shop_address)
        imgBtnShowMap = findViewById(R.id.imgBtnShowMap)
        spnStoreCategory = findViewById(R.id.spnStoreCategory)
        btnFinishSetup = findViewById(R.id.btnFinishSetup)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, CountryCodes.category)
        spnStoreCategory.adapter = arrayAdapter
        spnStoreCategory.setSelection(arrayAdapter.getPosition("Grocery"))

        spnStoreCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                type = CountryCodes.category.get(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Code to perform some action when nothing is selected
            }
        }

        fabSelectPhoto.setOnClickListener {
            browseGallery()
        }

        // Get latitude and longitude of shop location
        imgBtnShowMap.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(R.string.attention)
            alertDialog.setMessage(R.string.access_store_location_alert)
            alertDialog.setPositiveButton(R.string.proceed) { diag, which ->
                getLastLocation()
            }
            alertDialog.setNegativeButton(R.string.cancel) { diag, which ->
                diag.dismiss()
            }
            alertDialog.create()
            alertDialog.show()
        }

        btnFinishSetup.setOnClickListener {
            if (!etStoreName.text.isNullOrEmpty() && !etStoreContactNumber.text.isNullOrEmpty()
                && !etStoreAddress.text.isNullOrEmpty() && latitude != null && longitude != null) {
                setupShop()
            } else {
                Toast.makeText(
                    this,
                    "Please fill in all detail!",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

        }
    }

    private fun browseGallery() {
        if (checkPermissionStorage()) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, OPEN_IMAGE_STORAGE)
        } else {
            requestPermissionStorage()
        }
    }

    private fun getCityandCountry(lat: Double?, long: Double?) {
        val geoCoder = Geocoder(this, Locale.getDefault())
        val addr = geoCoder.getFromLocation(lat!!, long!!, 1)
        city = addr.get(0).locality
        country = addr.get(0).countryName
    }

    private fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task->
                    var location: Location? = task.result
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        getCityandCountry(latitude, longitude)
                    } else {
                        getNewLocation()
                    }
                }
            } else {
                Toast.makeText(this, "Please enable location service!", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }

    private  val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            val lastLocation = p0.lastLocation
            latitude = lastLocation.latitude
            longitude = lastLocation.longitude
            getCityandCountry(latitude, longitude)
        }
    }

    private fun setupShop() {
        uploadImageToFirebaseStorage()
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) {
            saveUserToFirebaseDatabase("")
            return
        }

        val filename = "profile_image"
        val  uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseStorage.getInstance().getReference("/images/$uid/$filename")

        //upload image to firebase storage
        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("LogCheck", "Photo uploaded Successfully : ${it.metadata?.path}")
                    ref.downloadUrl
                            .addOnSuccessListener {
                                Log.d("LogCheck", "File Location : $it")
                                saveUserToFirebaseDatabase(it.toString())
                            }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image! Please Try Again", Toast.LENGTH_SHORT).show()
                }
    }

    private fun saveUserToFirebaseDatabase(url: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/shops/$uid")
        val shop = Shops(
                sid = uid,
                shopName = etStoreName.text.toString(),
                shopType = type!!,
                mobile = etStoreContactNumber.text.toString(),
                address = etStoreAddress.text.toString(),
                shopImageUrl = url,
                lat = latitude,
                long = longitude,
                city = city!!,
                country = country!!
        )
        ref.setValue(shop).addOnSuccessListener {
            Log.d("LogCheck", "Shop registered to Firebase Database")
            val reff = FirebaseDatabase.getInstance().getReference("/users/$uid")
            reff.child("shopOwner").setValue(true).addOnSuccessListener {
                Toast.makeText(this, "You're now a Store Owner!", Toast.LENGTH_SHORT).show()
            }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed! Try again later.", Toast.LENGTH_SHORT).show()
                    }
            reff.child("usid").setValue(uid).addOnSuccessListener {
                //Toast.makeText(this, "You're now a Store Owner!", Toast.LENGTH_SHORT).show()
            }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed! Try again later.", Toast.LENGTH_SHORT).show()
                    }
            startActivity(Intent(this, HomeActivity::class.java))
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            finish()
        }.addOnFailureListener {
            Log.d("LogCheck", "${it.message}")
            Toast.makeText(this, "Failed to register. Please try later.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun checkPermissionStorage(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_ID_LOC)
    }

    private fun requestPermissionStorage() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_ID_STORAGE)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID_LOC) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        } else if (requestCode == PERMISSION_ID_STORAGE) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, OPEN_IMAGE_STORAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OPEN_IMAGE_STORAGE && data != null && resultCode == Activity.RESULT_OK) {
            //proceed and check which image was selected...
            try {
                selectedPhotoUri = data.data
                CropImage.activity(selectedPhotoUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK){
                selectedPhotoUri = result.uri
                imgStore.setImageURI(selectedPhotoUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(
                        this,
                        "Some error occurred, please try again later.",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}