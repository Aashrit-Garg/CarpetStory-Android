package com.example.carpetstory

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_view_carpet.*
import java.lang.Exception
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore




class ViewCarpetActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    var bundle: Bundle? = null
    var dialog: ACProgressFlower? = null
    var db = FirebaseFirestore.getInstance()
    var docID: String? = null
    var loadSave: Boolean? = null
    val data = hashMapOf(
        "wanted" to true
    ) as HashMap<String, Any>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_carpet)

        ARButton.setOnClickListener {

            onARButtonClicked()
        }

        val mToolBar = findViewById<View>(R.id.toolbarVewCarpet) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(mToolBar)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser
        bundle = intent.extras
        dialog = ACProgressFlower.Builder(this)
            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
            .themeColor(Color.WHITE)
            .text("Loading...")
            .fadeColor(Color.DKGRAY).build()
        dialog!!.show()

        var name = bundle!!.getString("name")
        var category = bundle!!.getString("category")
        var description = bundle!!.getString("description")
        var modelURL = bundle!!.getString("modelURL")
        var length = bundle!!.getInt("length")
        var breadth = bundle!!.getInt("breadth")
        loadSave = bundle!!.getBoolean("loadSave")
        docID = bundle!!.getString("docID")

        txtName.text = name
        txtCategory.text = category
        txtDescription.text = description
        txtLengthNum.text = length.toString()
        txtBreadthNum.text = breadth.toString()
        Picasso.get().load(modelURL).fit().centerCrop().into(ivCarpetImage,
            object : Callback {
                override fun onSuccess() {
                    dialog!!.dismiss()
                }

                override fun onError(e: Exception?) {
                    // kuch nahi
                }

            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (loadSave!!) {
            menuInflater.inflate(R.menu.favorite_save, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item?.itemId
        when (id) {
            R.id.saveFav ->
                addToFav()
        }
        return super.onOptionsItemSelected(item)
    }

    fun addToFav() {
        dialog!!.show()
        db.collection("Favourites").document(user!!.uid)
            .collection("Carpets").document(docID.toString())
            .set(data)
            .addOnSuccessListener {
                Snackbar.make(linearLayoutCarpetView, "Carpet Added to Favorites", Snackbar.LENGTH_LONG).show()
                dialog!!.dismiss()
            }
    }

    fun onARButtonClicked() {

        bundle = intent.extras

        var intent = Intent(this, AR::class.java)
        intent.putExtra("carpetName", bundle!!.getString("name"))
        Toast.makeText(this, "Showing " + bundle!!.getString("name").toString(), Toast.LENGTH_LONG).show()
        startActivity(intent)
    }

}
