package com.example.carpetstory

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_carpets_list_view.*
import kotlinx.android.synthetic.main.activity_home1.*
import kotlinx.android.synthetic.main.fragment_most_viewed.*
import kotlinx.android.synthetic.main.ticket_carpet.view.*
import java.lang.Exception

class CarpetsListViewActivity : AppCompatActivity() {

    var carpets = ArrayList<Carpet>()
    var db = FirebaseFirestore.getInstance()
    var docID = ArrayList<String>()
    var carpetAdapter: CarpetListAdapter? = null
    var dialog: ACProgressFlower? = null
    var category: String? = null
    var length: Int? = null
    var breadth: Int? = null
    var searchResult: Boolean? = null
    var bundle: Bundle? = null
    var resultOfLength = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carpets_list_view)

        bundle = intent.extras
        searchResult = bundle!!.getBoolean("searchResult")
        dialog = ACProgressFlower.Builder(this)
            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
            .themeColor(Color.WHITE)
            .text("Loading...")
            .fadeColor(Color.DKGRAY).build()
        dialog!!.show()

        carpets.clear()
        docID.clear()

        if (searchResult!!) {
            length = bundle!!.getString("Length")!!.toInt()
            breadth = bundle!!.getString("Breadth")!!.toInt()

            val queryLength: Query = db.collection("Carpets").whereLessThanOrEqualTo("length", length!!)
            val queryBreadth: Query = db.collection("Carpets").whereLessThanOrEqualTo("breadth", breadth!!)

            getDocsFromQuery(queryLength, 1)
            getDocsFromQuery(queryBreadth, 2)

        } else {

            category = bundle!!.getString("category")

            val query: Query = db.collection("Carpets").whereEqualTo("category", category!!)

            getDocsFromQuery(query, 3)

        }
        Log.i("Document retrieval success", carpets.size.toString())
        carpetAdapter = CarpetListAdapter(carpets, this)
        allCarpetsListView.adapter = carpetAdapter
    }

    inner class CarpetListAdapter(carpets: ArrayList<Carpet>, context: Context): BaseAdapter() {
        var carpets = ArrayList<Carpet>()
        var context: Context? = null
        init {
            this.carpets = carpets
            this.context = context
        }
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            var inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var currentCarpet = carpets[p0]
            var currentListView = inflater.inflate(R.layout.ticket_carpet, null)
            Log.i("Document listView Load success", currentCarpet.name)
            currentListView.textView3.text = currentCarpet.name
            Picasso.get().load(currentCarpet.modelURL).fit().centerCrop().into(currentListView.imageView2,
                object : Callback {
                    override fun onSuccess() {
                        dialog!!.dismiss()
                    }

                    override fun onError(e: Exception?) {
                        // kuch nahi
                    }

                })

            currentListView.ticketConstraint.setOnClickListener {
                var intent = Intent(context, ViewCarpetActivity::class.java)
                intent.putExtra("name", currentCarpet.name)
                intent.putExtra("category", currentCarpet.category)
                intent.putExtra("description", currentCarpet.description)
                intent.putExtra("modelURL", currentCarpet.modelURL)
                intent.putExtra("length", currentCarpet.length)
                intent.putExtra("breadth", currentCarpet.breadth)
                intent.putExtra("docID", currentCarpet.docID)
                intent.putExtra("loadSave", true)
                startActivity(intent)
            }

            return currentListView
        }

        override fun getItem(p0: Int): Any {
            return carpets[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {
            return carpets.size
        }
    }

    fun getDocsFromQuery(query: Query, selection: Int) {

        query
            .get()
            .addOnSuccessListener {documents ->

                if (documents.size() != 0) {

                    if (selection == 1) {

                        for (document in documents) {

                            resultOfLength.add(document.id)
                        }
                    } else if (selection == 2) {

                        for (document in documents) {

                            if (resultOfLength.contains(document.id)) {

                                getCarpetFromDoc(document.id)
                            }
                        }
                    } else if (selection == 3) {

                        for (document in documents) {

                            getCarpetFromDoc(document.id)
                        }
                    }
                } else {

                    dialog!!.dismiss()
                    Toast.makeText(this, "Invalid Search", Toast.LENGTH_LONG).show()
                    finish()
                }
            }

    }

    private fun getCarpetFromDoc(id: String) {

        var docRef = db.collection("Carpets").document(id)
        docRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    var dataDescription = snapshot.data!!
                    if (dataDescription["category"].toString() == category) {
                        carpets.add(Carpet(dataDescription["name"].toString(),
                            dataDescription["length"].toString().toInt(),
                            dataDescription["breadth"].toString().toInt(),
                            dataDescription["modelURL"].toString(),
                            dataDescription["description"].toString(),
                            dataDescription["category"].toString(),
                            dataDescription["mostViewed"].toString().toBoolean(),
                            id))
                        carpetAdapter!!.notifyDataSetChanged()
                        docID.add(id)
                    }
                }
            }
    }
}
