package com.example.shopchain.ui.fragment

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.shopchain.R
import com.example.shopchain.model.User
import com.example.shopchain.ui.auth.LoginActivity
import com.example.shopchain.ui.main.CreateBusinessAccountActivity
import com.example.shopchain.ui.main.LatestMessagesActivity
import com.example.shopchain.ui.main.ScanToConnectActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class SettingsFragment : Fragment() {

    lateinit var txtSetupBusinessAccount: TextView
    lateinit var txtManageBusinessAccount: TextView
    lateinit var txtMyChats: TextView
    lateinit var txtChangePassword: TextView
    lateinit var txtLanguage: TextView
    lateinit var txtLogout: TextView
    lateinit var userImage: CircleImageView
    lateinit var userName: TextView
    lateinit var userNumber: TextView
    lateinit var txtScanToConnect: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        userImage = view.findViewById(R.id.imgProfile)
        userName = view.findViewById(R.id.usernameTextView)
        userNumber = view.findViewById(R.id.mobileTextView)
        txtSetupBusinessAccount = view.findViewById(R.id.txtSetupBusinessAccount)
        txtManageBusinessAccount = view.findViewById(R.id.txtManageBusinessAccount)
        txtMyChats = view.findViewById(R.id.txtMyChats)
        txtChangePassword = view.findViewById(R.id.txtChangePassword)
        txtLanguage = view.findViewById(R.id.txtLanguage)
        txtLogout = view.findViewById(R.id.txtLogout)
        txtScanToConnect = view.findViewById(R.id.txtScanToConnect)

        loadUserProfile()

        txtScanToConnect.setOnClickListener {
            val intent = Intent(activity, ScanToConnectActivity::class.java)
            startActivity(intent)
        }

        txtSetupBusinessAccount.setOnClickListener {
            val intent = Intent(activity, CreateBusinessAccountActivity::class.java)
            startActivity(intent)
            //activity?.finish()
        }

        txtManageBusinessAccount.setOnClickListener {

        }

        txtMyChats.setOnClickListener {
            val intent = Intent(activity, LatestMessagesActivity::class.java)
            startActivity(intent)
        }

        txtChangePassword.setOnClickListener {

        }

        txtLanguage.setOnClickListener {
            val options = arrayOf<CharSequence>("English", "हिंदी", "ಕನ್ನಡ", "తెలుగు")
            val optionDialog = android.app.AlertDialog.Builder(requireContext())
            optionDialog.setTitle(R.string.select_language)
            optionDialog.setItems(options, { dialog, item ->
                if (options[item] == "English") {

                } else if (options[item] == "हिंदी") {

                } else if (options[item] == "ಕನ್ನಡ") {

                } else if (options[item] == "తెలుగు") {

                }
            })
            optionDialog.create()
            optionDialog.show()
        }

        txtLogout.setOnClickListener {
            performSignOut()
        }

        return view
    }

    private fun loadUserProfile() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    if (user.profileImageUrl.isNotBlank()) {
                        Glide.with(requireActivity())
                            .load(user.profileImageUrl)
                            .into(userImage)
                    } else {
                        userImage.setImageResource(R.drawable.default_user_image)
                    }
                    userName.text = user.username
                    userNumber.text = user.mobile
                    if (user.shopOwner) {
                        txtSetupBusinessAccount.visibility = View.GONE
                        txtManageBusinessAccount.visibility = View.VISIBLE
                    } else {
                        txtSetupBusinessAccount.visibility = View.VISIBLE
                        txtManageBusinessAccount.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun performSignOut() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Sign Out")
        dialog.setMessage("Are you sure you want to sign out?")
        dialog.setPositiveButton("YES") { dialog, item ->
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        dialog.setNegativeButton("CANCEL") {dialog, item ->
            dialog.dismiss()
        }
        dialog.create()
        dialog.show()
    }
}
