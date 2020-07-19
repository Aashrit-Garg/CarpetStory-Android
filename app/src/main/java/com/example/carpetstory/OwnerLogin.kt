package com.example.carpetstory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser


class OwnerLogin : AppCompatActivity() {

    var mAuth: FirebaseAuth? = null
    var email: String = "12@34.com"
    var password: String = "123456"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_login)

        mAuth = FirebaseAuth.getInstance()
    }

    fun loginButton(view:View) {
        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var intent = Intent(this, AddCarpet::class.java)
                    startActivity(intent)
                    finish()
                }
                else {
                    Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show()
                }
            }
    }




}
