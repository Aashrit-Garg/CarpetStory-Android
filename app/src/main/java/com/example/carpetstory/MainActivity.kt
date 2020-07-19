package com.example.carpetstory

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var googleSignInButton: SignInButton
    val TAG = "CreateAccount"
    val GOOGLE_LOG_IN_RC = 1
    var googleApiClient: GoogleApiClient? = null
    private var mAuth: FirebaseAuth? = null
    var GoogleSignInAccount: GoogleSignInAccount? = null
    var currentUser: FirebaseUser? = null
    var database = FirebaseDatabase.getInstance()
    var myRef = database.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            loadMain()
        }

        val mToolBar = findViewById<View>(R.id.toolbarLogin) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(mToolBar)

        //Google
        googleSignInButton = findViewById<View>(R.id.googleSignInButton) as SignInButton
        googleSignInButton.setOnClickListener {
            googleLogin()
        }
        // Configure Google Sign In
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Creating and Configuring Google Api Client.
        googleApiClient = GoogleApiClient.Builder(this@MainActivity)
            .enableAutoManage(this@MainActivity  /* OnConnectionFailedListener */) { }
            .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
            .build()
    }

    private fun googleLogin() {
        Log.i(TAG, "Starting Google LogIn Flow.")
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, GOOGLE_LOG_IN_RC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOG_IN_RC) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            Log.i(TAG, "With Google LogIn, is result a success? ${result.isSuccess}.")
            if (result.isSuccess) {
                // Google Sign In was successful, authenticate with Firebase
                firebaseAuthWithGoogle(result.signInAccount!!)
            } else {
                Toast.makeText(this@MainActivity, "Some error occurred.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {

        Log.i(TAG, "Authenticating user with firebase.")
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)?.addOnCompleteListener(this) { task ->

            Log.i(TAG, "Firebase Authentication, is result a success? ${task.isSuccessful}.")
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                currentUser = mAuth!!.currentUser
                loadMain()
            } else {
                // If sign in fails, display a message to the user.
                Log.e(TAG, "Authenticating with Google credentials in firebase FAILED !!")
            }
        }
    }

    fun loadMain() {
        if (currentUser != null) {
            var intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.login_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item?.itemId
        when (id) {
            R.id.newCarpet ->
                newCarpet()
        }
        return super.onOptionsItemSelected(item)
    }
    fun newCarpet() {
        var intent = Intent(this, OwnerLogin::class.java)
        startActivity(intent)
    }
}
