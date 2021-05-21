package com.example.shopchain.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.shopchain.R
import com.example.shopchain.model.Shops
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import io.fotoapparat.Fotoapparat
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.result.BitmapPhoto
import io.fotoapparat.view.CameraView

class ScanToConnectActivity : AppCompatActivity() {

    lateinit var btnScan: Button
    lateinit var cameraView: CameraView
    lateinit var fotoapparat: Fotoapparat
    lateinit var barcodeScanner: BarcodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_to_connect)

        btnScan = findViewById(R.id.btnScan)
        cameraView = findViewById(R.id.cameraView)

        initializeBarcodeScanner()

        initializeFotoapparat()

        btnScan.setOnClickListener {
            if (isCameraPermissionGranted())
                takeImage()
            else requestCameraPermission()
        }

        if (!isCameraPermissionGranted())
            requestCameraPermission()
    }

    private fun takeImage() {
        fotoapparat.takePicture().toBitmap().whenAvailable {
            Log.d("TAG", "bitmap obtained")
            scanImageForBarcode(it!!)
        }
    }

    private fun scanImageForBarcode(it: BitmapPhoto) {
        val inputImage = InputImage.fromBitmap(it.bitmap, it.rotationDegrees)
        val task = barcodeScanner.process(inputImage)
        Log.d("TAG", "scan image barcode")
        task.addOnSuccessListener { barCodes ->
            Log.d("TAG", "success, size = ${barCodes.size}")
            for (barcode in barCodes) {
                val barcodeValue = barcode.rawValue
                Log.d("TAG", "The code is $barcodeValue")
                launchShop(barcodeValue)
            }
        }
        task.addOnFailureListener {
            Log.d("TAG", "Failed")
            Toast.makeText(this, "Error occurred! Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchShop(id: String?) {
        Log.d("TAG", "Launch")
        val ref = FirebaseDatabase.getInstance().getReference("/shops/$id")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                val shops = snapshot.getValue(Shops::class.java)
                if (shops != null) {
                    val intent = Intent(this@ScanToConnectActivity, StoreActivity::class.java)
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
                    startActivity(intent)
                }
            }
        })
    }

    private fun initializeFotoapparat() {
        fotoapparat = Fotoapparat.with(this)
            .into(cameraView)
            .previewScaleType(ScaleType.CenterCrop)
            .build()
    }

    private fun initializeBarcodeScanner() {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)
    }

    override fun onStart() {
        super.onStart()
        fotoapparat.start()
    }

    override fun onStop() {
        super.onStop()
        fotoapparat.stop()
    }

    private fun isCameraPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            1144
        )
    }
}