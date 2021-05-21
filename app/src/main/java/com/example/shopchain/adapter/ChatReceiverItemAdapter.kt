package com.example.shopchain.adapter

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.shopchain.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

class ChatReceiverItemAdapter(
    val context: Context,
    val text: String = "",
    val mediaUrl: String = "",
    val fileName: String = ""
): Item<ViewHolder>() {

    private val STORAGE_PERMISSION_REQUEST_CODE: Int = 1000

    private fun startDownload() {

        val file1 = File("Go Messenger/Media/GM Videos")
        if (!file1.exists()) {
            file1.mkdirs()
        }
        val file2 = File("Go Messenger/Media/GM Images")
        if (!file2.exists()) {
            file2.mkdirs()
        }

        if (mediaUrl != "" && (getMimeType(mediaUrl) == "image/png" || getMimeType(mediaUrl) ==
                    "image/jpeg" || getMimeType(mediaUrl) == "image/gif")) {

            val request = DownloadManager.Request(Uri.parse(mediaUrl))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or  DownloadManager.Request.NETWORK_MOBILE)
            request.setTitle(fileName)
            request.setDescription("The file is downloading...")
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            Log.i("TEST", " ap = ${file2.exists()}, path = ${file2.path}")
            request.setDestinationInExternalFilesDir(context, file2.absolutePath, fileName)

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)

        } else if (mediaUrl != "" && getMimeType(mediaUrl) == "video/mp4") {

            val request = DownloadManager.Request(Uri.parse(mediaUrl))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or  DownloadManager.Request.NETWORK_MOBILE)
            request.setTitle(fileName)
            request.setDescription("The file is downloading...")
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalFilesDir(context, file1.path, fileName)

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)

        }
    }

    fun getMimeType(url: String?): String? {
        val type: String?
        val extension: String = MimeTypeMap.getFileExtensionFromUrl(url)
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return type
    }

    override fun getLayout(): Int {
        return R.layout.chat_receiver_single_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        if (text != "") {
            viewHolder.itemView.findViewById<TextView>(R.id.txtReceiverMessage).visibility = View.VISIBLE
            viewHolder.itemView.findViewById<RelativeLayout>(R.id.rlMediaReceiverMessage).visibility = View.GONE
            viewHolder.itemView.findViewById<ImageView>(R.id.imgReceiverView).visibility = View.GONE
            viewHolder.itemView.findViewById<TextView>(R.id.txtReceiverMessage).text = text
        } else if (mediaUrl != "") {
            if (getMimeType(mediaUrl) == "image/png" || getMimeType(mediaUrl) == "image/jpeg" ||
                getMimeType(mediaUrl) == "image/gif") {
                viewHolder.itemView.findViewById<ImageView>(R.id.imgReceiverView).visibility = View.VISIBLE
                Glide.with(context).load(mediaUrl).into(viewHolder.itemView.findViewById(R.id.imgReceiverView))
            }
            viewHolder.itemView.findViewById<TextView>(R.id.txtReceiverMessage).visibility = View.GONE
            viewHolder.itemView.findViewById<RelativeLayout>(R.id.rlMediaReceiverMessage).visibility = View.VISIBLE
            viewHolder.itemView.findViewById<TextView>(R.id.txtReceiverMediaName).text = fileName

            viewHolder.itemView.findViewById<Button>(R.id.btnDownloadMedia).setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!EasyPermissions.hasPermissions(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        // permission denied, request for permission
                        EasyPermissions.requestPermissions(
                            context as Activity,
                            context.getString(R.string.rationale_storage),
                            STORAGE_PERMISSION_REQUEST_CODE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    } else {
                        //permissions already granted, start download
                        startDownload()
                    }
                } else {
                    // Android OS less than Marshmallow, no need for runtime permission, start download
                    startDownload()
                }
            }
        }
    }
}