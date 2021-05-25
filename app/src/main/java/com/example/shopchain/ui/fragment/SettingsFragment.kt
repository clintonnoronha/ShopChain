package com.example.shopchain.ui.fragment

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import com.bumptech.glide.Glide
import com.example.shopchain.R
import com.example.shopchain.model.User
import com.example.shopchain.ui.auth.LoginActivity
import com.example.shopchain.ui.main.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.awaitAll
import java.util.*

class SettingsFragment : Fragment() {

    lateinit var txtSetupBusinessAccount: TextView
    lateinit var txtManageBusinessAccount: TextView
    lateinit var txtMyChats: TextView
    lateinit var txtLanguage: TextView
    lateinit var txtLogout: TextView
    lateinit var txtDeactivate: TextView
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
        txtLanguage = view.findViewById(R.id.txtLanguage)
        txtLogout = view.findViewById(R.id.txtLogout)
        txtScanToConnect = view.findViewById(R.id.txtScanToConnect)
        txtDeactivate = view.findViewById(R.id.txtDeactivateAccount)

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
            val intent = Intent(activity, ManageBusinessAccountActivity::class.java)
            startActivity(intent)
        }

        txtMyChats.setOnClickListener {
            val intent = Intent(activity, LatestMessagesActivity::class.java)
            startActivity(intent)
        }

        txtDeactivate.setOnClickListener {
            deactivateAccount()
        }

        txtLanguage.setOnClickListener {
            val options = arrayOf<CharSequence>("English", "हिंदी", "ಕನ್ನಡ", "తెలుగు")
            val optionDialog = android.app.AlertDialog.Builder(requireContext())
            optionDialog.setTitle(R.string.select_language)
            optionDialog.setItems(options, { dialog, item ->
                if (options[item] == "English") {
                    setAppLocale(requireContext(), "en")
                    val intent = Intent(requireContext(), HomeActivity::class.java)
                    startActivity(intent)
                } else if (options[item] == "हिंदी") {
                    setAppLocale(requireContext(), "hi")
                    val intent = Intent(requireContext(), HomeActivity::class.java)
                    startActivity(intent)
                } else if (options[item] == "ಕನ್ನಡ") {
                    setAppLocale(requireContext(), "kn")
                    val intent = Intent(requireContext(), HomeActivity::class.java)
                    startActivity(intent)
                } else if (options[item] == "తెలుగు") {
                    setAppLocale(requireContext(), "te")
                    val intent = Intent(requireContext(), HomeActivity::class.java)
                    startActivity(intent)
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

    private fun deactivateAccount() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Attention")
        dialog.setMessage("Your account will be permanently deleted. Are you sure you want to proceed?")
        dialog.setPositiveButton("YES") { dialog, item ->
            val user = FirebaseAuth.getInstance().currentUser!!
            val res = user.getIdToken(true).result
            val token = res.token
            if (token!!.isNotEmpty()) {
                val credentials = GoogleAuthProvider.getCredential(token, null)
                user.reauthenticate(credentials)
                        .addOnCompleteListener {
                            user.delete()
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Toast.makeText(
                                                    requireContext(),
                                                    "Account deleted permanently.",
                                                    Toast.LENGTH_SHORT
                                            ).show()
                                            val intent = Intent(requireContext(), LoginActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            startActivity(intent)
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                                requireContext(),
                                                "Some error occurred, please try again later.",
                                                Toast.LENGTH_SHORT
                                        ).show()
                                    }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                    requireContext(),
                                    it.message,
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
            } else {
                Toast.makeText(
                        requireContext(),
                        "Failed!",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
        dialog.setNegativeButton("CANCEL") {dialog, item ->
            dialog.dismiss()
        }
        dialog.create()
        dialog.show()
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

    private fun setAppLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
