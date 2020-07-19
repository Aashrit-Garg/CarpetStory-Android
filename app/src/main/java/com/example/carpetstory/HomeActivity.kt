package com.example.carpetstory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toolbar
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class HomeActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    var mGoogleApiClient: GoogleApiClient? = null
    lateinit var mostViewedFragment: MostViewedFragment
    lateinit var searchFragment: SearchFragment
    lateinit var categoryFragment: CategoryFragment
    lateinit var favoriteFragment: FavoriteFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home1)

        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this@HomeActivity  /* OnConnectionFailedListener */) { }
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        val mToolBar = findViewById<View>(R.id.toolbar) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(mToolBar)


        mostViewedFragment = MostViewedFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, mostViewedFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()

        val bottomNavigation: BottomNavigationView = findViewById(R.id.btm_nav)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.most_viewed -> {
                    mostViewedFragment = MostViewedFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frameLayout, mostViewedFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }

                R.id.search -> {
                    searchFragment = SearchFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frameLayout, searchFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }

                R.id.category -> {
                    categoryFragment = CategoryFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frameLayout, categoryFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }

                R.id.favorites -> {
                    favoriteFragment = FavoriteFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frameLayout, favoriteFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item?.itemId
        when (id) {
            R.id.Logout ->
                logOut()
        }
        return super.onOptionsItemSelected(item)
    }

    fun logOut() {
        mAuth!!.signOut()
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
