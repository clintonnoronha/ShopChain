package com.example.shopchain.ui.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopchain.R
import com.example.shopchain.adapter.ChatReceiverItemAdapter
import com.example.shopchain.adapter.ChatSenderItemAdapter
import com.example.shopchain.model.ChatMessages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ChatLogActivity : AppCompatActivity(),
        EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks{

    private var cameraFile: File? = null
    private var selectedPhotoUri: Uri? = null
    lateinit var etTypeMessage: EditText
    lateinit var fabSendMessage: View
    lateinit var imgProfileSender: CircleImageView
    lateinit var txtSenderName: TextView
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var recyclerViewChatLog: RecyclerView
    lateinit var btnAttach: ImageButton
    private val adapter = GroupAdapter<ViewHolder>()
    lateinit var progressBar: ProgressBar
    lateinit var rlProgress: RelativeLayout
    lateinit var progressText: TextView
    private var fileName: String? = null
    private val STORAGE_IMAGE_PREMISSION_CODE = 101
    private val OPEN_IMAGE_STORAGE = 103
    private val TAG = "ChatLog"
    private var chatUserName: String? = null
    private var chatUserImage: String? = null
    private var chatUserUid: String? = null
    private val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        chatUserName = intent.getStringExtra("CHAT_NAME")
        chatUserImage = intent.getStringExtra("CHAT_IMAGE")
        chatUserUid = intent.getStringExtra("CHAT_ID")

        etTypeMessage = findViewById(R.id.etTypeMessage)
        fabSendMessage = findViewById(R.id.fabSendMessage)
        imgProfileSender = findViewById(R.id.imgProfileSender)
        txtSenderName = findViewById(R.id.txtSenderName)
        btnAttach = findViewById(R.id.btnAttach)
        toolbar = findViewById(R.id.toolbar)
        progressText = findViewById(R.id.txtProgressText)
        rlProgress = findViewById(R.id.rlProgress)
        rlProgress.visibility = View.GONE
        progressBar = findViewById(R.id.progressBar3)
        recyclerViewChatLog = findViewById(R.id.recyclerViewChatLog)
        recyclerViewChatLog.adapter = adapter

        setUpToolbar()

        if (chatUserName != null && chatUserImage != null) {
            if (chatUserImage!!.isNotEmpty()) {
                Glide.with(this@ChatLogActivity).load(chatUserImage).into(imgProfileSender)
            } else {
                imgProfileSender.setImageResource(R.drawable.default_user_image)
            }
            txtSenderName.text = chatUserName
        } else {
            txtSenderName.text = "Chat"
            imgProfileSender.setImageResource(R.drawable.default_user_image)
        }

        if (chatUserUid != null) {
            listenForMessages(chatUserUid!!)
        } else {
            Toast.makeText(this@ChatLogActivity, "Error!! Please try Again", Toast.LENGTH_SHORT).show()
        }

        btnAttach.setOnClickListener {
            storagePermissionRequest()
        }

        fabSendMessage.setOnClickListener {
            //send the message
            if (chatUserUid != null) {
                performSendMessage(chatUserUid!!)
                //after message is sent clear text in etTypeMessage
                etTypeMessage.text.clear()
            }
        }

    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun hasStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(this, *perms)
    }

    private fun storagePermissionRequest() {
        if (hasStoragePermission()) {
            //open gallery to select photo or video
            browseGallery()
        } else {
            EasyPermissions
                .requestPermissions(
                    this@ChatLogActivity,
                    getString(R.string.rationale_storage),
                    STORAGE_IMAGE_PREMISSION_CODE,
                    *perms
                )
        }
    }

    private fun browseGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/mp4"))
        startActivityForResult(Intent.createChooser(intent, "Choose video/image"), OPEN_IMAGE_STORAGE)
    }

    private fun listenForMessages(toId: String) {
        val fromId: String = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(error: DatabaseError) {

            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }
            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessages = snapshot.getValue(ChatMessages::class.java)
                rlProgress.visibility = View.GONE
                if (chatMessages != null && chatMessages.text != "" &&
                        chatMessages.mediaUrl == "") {
                    if (chatMessages.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatSenderItemAdapter(this@ChatLogActivity,
                                chatMessages.text))
                    } else {
                        adapter.add(ChatReceiverItemAdapter(this@ChatLogActivity,
                                chatMessages.text))
                    }
                } else if (chatMessages != null && chatMessages.text == "" && chatMessages.mediaUrl != "") {
                    if (chatMessages.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatSenderItemAdapter(context = this@ChatLogActivity,
                                mediaUrl = chatMessages.mediaUrl, fileName = chatMessages.fileName))
                    } else {
                        adapter.add(ChatReceiverItemAdapter(context = this@ChatLogActivity,
                                mediaUrl = chatMessages.mediaUrl, fileName = chatMessages.fileName))
                    }
                }
                recyclerViewChatLog.scrollToPosition(adapter.itemCount - 1)
            }
        })
    }

    private fun performSendMessage(toId: String) {
        val fromId = FirebaseAuth.getInstance().uid ?: return
        val text = etTypeMessage.text.toString().trim()
        //Prevent blank messages from being sent
        if (text.isBlank()) return

        val fromRef = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessages = ChatMessages(fromId = fromId, id = fromRef.key!!, text = text,
                timestamp = System.currentTimeMillis()/1000, toId = toId)

        fromRef.setValue(chatMessages)
                .addOnSuccessListener {
                    //scroll to latest message sent
                    recyclerViewChatLog.scrollToPosition(adapter.itemCount - 1)
                }
        toRef.setValue(chatMessages)

        val latestMessageFromRef = FirebaseDatabase.getInstance().getReference(
                "/latest-message/$fromId/$toId"
        )
        latestMessageFromRef.setValue(chatMessages)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference(
                "/latest-message/$toId/$fromId"
        )
        latestMessageToRef.setValue(chatMessages)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OPEN_IMAGE_STORAGE && data != null && resultCode == Activity.RESULT_OK) {
            //proceed and check which image was selected...
            try {
                selectedPhotoUri = data.data
                val mimeType: String? = data.data?.let { returnUri ->
                    contentResolver.getType(returnUri)
                }
                if (mimeType == "image/jpeg" || mimeType == "image/png" || mimeType == "image/gif") {
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    val imageName = "IMG_SC_" + timestamp + "_"
                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val image = File.createTempFile(imageName, ".jpg", storageDir)
                    fileName = image.name
                    image.delete()
                    CropImage.activity(selectedPhotoUri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setCropShape(CropImageView.CropShape.RECTANGLE)
                            .setOutputCompressQuality(60)
                            .start(this)
                } else if (mimeType == "video/mp4") {
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    val imageName = "VID_SC_" + timestamp + "_"
                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                    val image = File.createTempFile(imageName, ".mp4", storageDir)
                    fileName = image.name
                    image.delete()
                    uploadImageToFirebaseStorage()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            //Do something after user returned from app settings screen
            Log.d(TAG, "Returned from application settings")
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK){
                selectedPhotoUri = result.uri
                uploadImageToFirebaseStorage()
                if (cameraFile != null) {
                    cameraFile?.delete()
                    cameraFile = null
                }
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

    private fun uploadImageToFirebaseStorage() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = chatUserUid
        val fromRef = FirebaseStorage.getInstance().getReference("/user-images/$fromId/$toId/$fileName")

        fromRef.putFile(selectedPhotoUri!!)
                .addOnProgressListener {
                    val progress: Int = ((100 * it.bytesTransferred) / it.totalByteCount).toInt()
                    rlProgress.visibility = View.VISIBLE
                    progressBar.progress = progress
                    progressText.text = "$progress%"
                }
                .addOnSuccessListener {
                    fromRef.downloadUrl
                            .addOnSuccessListener {
                                performSendMedia(it.toString())
                            }
                }
                .addOnFailureListener {
                    Toast.makeText(
                            this,
                            "Some error occurred, please try again later.",
                            Toast.LENGTH_SHORT
                    ).show()
                }
    }

    private fun performSendMedia(mediaUrl: String) {
        val fromId = FirebaseAuth.getInstance().uid ?: return
        val toId = chatUserUid

        //Prevent blank messages from being sent
        if (mediaUrl.isBlank()) return

        val fromRef = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessages = ChatMessages(fromId = fromId, id = fromRef.key!!,
                mediaUrl = mediaUrl, fileName = fileName!!,timestamp = System.currentTimeMillis()/1000, toId = toId!!)

        fromRef.setValue(chatMessages)
                .addOnSuccessListener {
                    //scroll to latest message sent
                    recyclerViewChatLog.scrollToPosition(adapter.itemCount - 1)
                }
        toRef.setValue(chatMessages)

        val latestMessageFromRef = FirebaseDatabase.getInstance().getReference(
                "/latest-message/$fromId/$toId"
        )
        latestMessageFromRef.setValue(chatMessages)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference(
                "/latest-message/$toId/$fromId"
        )
        latestMessageToRef.setValue(chatMessages)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //forwarding results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults,
                this
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size)
        when (requestCode) {
            STORAGE_IMAGE_PREMISSION_CODE -> {
                //open gallery to select photo or video
                browseGallery()
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size)

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms))
        {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {
        Log.d(TAG, "onRationaleAccept:" + requestCode)
    }

    override fun onRationaleDenied(requestCode: Int) {
        Log.d(TAG, "onRationaleDenied:" + requestCode)
    }
}