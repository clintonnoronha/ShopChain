package com.example.shopchain.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.shopchain.R
import com.example.shopchain.ui.auth.LoginActivity
import com.example.shopchain.ui.fragment.AboutAppFragment
import com.example.shopchain.ui.fragment.DiscoverStoresFragment
import com.example.shopchain.ui.fragment.SavedStoresFragment
import com.example.shopchain.ui.fragment.SettingsFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var drawerLayout: DrawerLayout
    lateinit var toolbar: Toolbar
    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var frameLayout: FrameLayout
    lateinit var navigationLayout: NavigationView

    var previousMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.toolbar)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        frameLayout = findViewById(R.id.frame)
        navigationLayout = findViewById(R.id.navigationView)

        auth= FirebaseAuth.getInstance()
        var currentUser=auth.currentUser

        if(currentUser==null){
            startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
            finish()
        }

        setUpToolbar()

        openDashboard()

        val actionBarDrawerToggle =  ActionBarDrawerToggle(this@HomeActivity, drawerLayout,
                R.string.open_drawer,
                R.string.close_drawer
        )

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState() //Changes back icon to hamburger icon and vise-versa

        navigationLayout.setNavigationItemSelectedListener {

            if (previousMenuItem != null) {
                previousMenuItem?.isChecked = false
            }

            it.isCheckable = true
            it.isChecked = true
            previousMenuItem = it

            when(it.itemId) {
                R.id.settings -> {
                    supportFragmentManager.beginTransaction()
                            .replace(
                                    R.id.frame,
                                    SettingsFragment()
                            )
                            .commit()

                    supportActionBar?.title = getString(R.string.settings)

                    drawerLayout.closeDrawers()

                }
                R.id.discover -> {
                    openDashboard()
                    drawerLayout.closeDrawers()
                }
                R.id.saved -> {
                    supportFragmentManager.beginTransaction()
                            .replace(
                                    R.id.frame,
                                    SavedStoresFragment()
                            )
                            .commit()

                    supportActionBar?.title = getString(R.string.saved_stores)

                    drawerLayout.closeDrawers()
                }
                R.id.about_app -> {
                    supportFragmentManager.beginTransaction()
                            .replace(
                                    R.id.frame,
                                    AboutAppFragment()
                            )
                            .commit()

                    supportActionBar?.title = getString(R.string.about_app)

                    drawerLayout.closeDrawers()
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

    fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        if(id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    fun openDashboard() {
        val fragment = DiscoverStoresFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
        supportActionBar?.title = getString(R.string.discover)
        navigationLayout.setCheckedItem(R.id.discover)
    }

    override fun onBackPressed() {
        val frag = supportFragmentManager.findFragmentById(R.id.frame)

        when (frag) {
            !is DiscoverStoresFragment -> openDashboard()

            else -> super.onBackPressed()
        }
    }

}