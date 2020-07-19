package com.example.carpetstory

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_carpet.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddCarpet : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    var db = FirebaseFirestore.getInstance()
    var dialog: ACProgressFlower? = null
    val storage = FirebaseStorage.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_carpet)

        dialog = ACProgressFlower.Builder(this)
            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
            .themeColor(Color.WHITE)
            .text("Loading...")
            .fadeColor(Color.DKGRAY).build()
        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser
        val mToolBar = findViewById<View>(R.id.toolbarAddCarpet) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(mToolBar)
        uploadImage.setOnClickListener {
            checkPermission()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_carpet, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item?.itemId
        when (id) {
            R.id.logoutAddCarpet ->
                logOut()

        }
        return super.onOptionsItemSelected(item)
    }

    fun logOut() {
        mAuth!!.signOut()
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    val PICK_IMAGE_CODE = 123
    fun chooseImage() {
        var intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    var readImage = 1
    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), readImage)
            } else {
                chooseImage()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            readImage-> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImage()
                } else {
                    Toast.makeText(this, "Cannot access your images", Toast.LENGTH_LONG).show()
                }
            }
            else-> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_CODE && resultCode == Activity.RESULT_OK && data != null) {
            var selectedImage = data.data!!
            val bitmap: Bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImage))
            uploadImage.setImageBitmap(bitmap)
        }
    }

    fun onClicked(view: View) {
        dialog!!.show()
        loadCarpet()
    }

    var downloadUrl:Uri? = null
    fun loadCarpet() {
        val email:String = user!!.email.toString()
        val storageRef = storage.getReferenceFromUrl("gs://carpetstory-e5a8f.appspot.com/")
        val dateFormat = SimpleDateFormat("ddMMyyHHmmss")
        val dataObject = Date()
        val imageName = splitString(email) + "." + dateFormat.format(dataObject) + ".jpeg"
        uploadImage.isDrawingCacheEnabled = true
        uploadImage.buildDrawingCache()
        val bitmap = (uploadImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val imageRef = storageRef.child("carpetPics/$imageName")
        var uploadTask = imageRef.putBytes(data)

        val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    Log.i("Exception", task.exception.toString())
                }
            }
            return@Continuation imageRef.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                downloadUrl = task.result
                Log.i("Download URL", downloadUrl.toString())
                db.collection("Carpets").add(CarpetAdd(tvName.text.toString(),
                    tvLength.text.toString().toInt(),
                    tvBreadth.text.toString().toInt(),
                    downloadUrl.toString(),
                    tvDescription.text.toString(),
                    tvCategory.text.toString(),
                    true ))
                    .addOnSuccessListener {
                        dialog!!.dismiss()
                        Toast.makeText(this, "Carpet Uploaded Successfully", Toast.LENGTH_LONG).show()
                        mAuth!!.signOut()
                        var intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
            } else {
                Toast.makeText(this, "Carpet Not Uploaded Successfully", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun splitString (text:String):String {
        val split = text.split("@")
        return split[0]
    }
}
