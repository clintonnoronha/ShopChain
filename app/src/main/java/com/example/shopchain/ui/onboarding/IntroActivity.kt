package com.example.shopchain.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.shopchain.R
import com.example.shopchain.ui.auth.LoginActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.*
import java.util.*


class IntroActivity : AppCompatActivity() {

    lateinit var screenPager: ViewPager
    lateinit var introViewPagerAdapter: IntroViewPagerAdapter
    lateinit var tabIndicator: TabLayout
    lateinit var btnNext: Button
    lateinit var btnGetStarted: Button
    lateinit var btnAnim: Animation
    lateinit var tvSkip: TextView
    private var position = 0
    private var count = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // when this activity is about to be launch check if it was opened before or not
        if (restorePrefData()) {
            val intent = Intent(this@IntroActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        setContentView(R.layout.activity_intro)

        // hide the action bar
        supportActionBar?.hide()

        // find the views
        btnNext = findViewById(R.id.btn_next);
        btnGetStarted = findViewById(R.id.btn_get_started);
        tabIndicator = findViewById(R.id.tab_indicator);
        btnAnim = AnimationUtils.loadAnimation(applicationContext, R.anim.button_animation);
        tvSkip = findViewById(R.id.tv_skip)

        //fill list screen
        val mList: MutableList<ScreenItem> = ArrayList()
        mList.add(ScreenItem(getString(R.string.view_pager_title_one), getString(R.string.view_pager_title_one_desc), R.drawable.img1))
        mList.add(ScreenItem(getString(R.string.view_pager_title_two), getString(R.string.view_pager_title_two_desc), R.drawable.img2))
        mList.add(ScreenItem(getString(R.string.view_pager_title_three), getString(R.string.view_pager_title_three_desc), R.drawable.img3))

        //setup of ViewPager
        screenPager = findViewById(R.id.screen_viewpager)
        introViewPagerAdapter = IntroViewPagerAdapter(this, mList)
        screenPager.adapter = introViewPagerAdapter

        // setup tablayout with viewpager
        tabIndicator.setupWithViewPager(screenPager)
        //screenPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabIndicator))

        // next button click Listner
        btnNext.setOnClickListener {
            position = screenPager.currentItem
            if (position < mList.size) {
                position++
                screenPager.currentItem = position
            }
            if (position == mList.size - 1) {
                // when we reach to the last screen
                loadLastScreen()
            }
        }

        // tabLayout Selected Listener
        tabIndicator.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.getPosition() == mList.size - 1 && count == -1) {
                    count++
                    loadLastScreen();
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {

            }
            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })


        // Get Started button click listener
        btnGetStarted.setOnClickListener {

            val intent = Intent(this@IntroActivity, LoginActivity::class.java)
            startActivity(intent)
            /* save a boolean value to storage so next time when the user run the app
            we could know that he is already checked the intro screen activity */
            savePrefsData()
            finish()

        }

        // skip button click listener
        tvSkip.setOnClickListener {
            screenPager.currentItem = mList.size
        }

    }

    private fun restorePrefData(): Boolean {
        val pref = applicationContext.getSharedPreferences("myPrefs", MODE_PRIVATE)
        return pref.getBoolean("isIntroOpened", false)
    }

    private fun savePrefsData() {
        val pref = applicationContext.getSharedPreferences("myPrefs", MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean("isIntroOpened", true)
        editor.commit()
    }

    private fun loadLastScreen() {
        btnNext.visibility = View.INVISIBLE
        btnGetStarted.visibility = View.VISIBLE
        tvSkip.visibility = View.INVISIBLE
        tabIndicator.visibility = View.INVISIBLE
        // animation
        btnGetStarted.animation = btnAnim
    }
}