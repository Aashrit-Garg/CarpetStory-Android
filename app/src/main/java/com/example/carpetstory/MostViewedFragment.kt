package com.example.carpetstory


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
import com.google.android.gms.auth.api.Auth
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_most_viewed.*
import kotlinx.android.synthetic.main.ticket_carpet.view.*
import java.lang.Exception
import java.util.zip.Inflater

/**
 * A simple [Fragment] subclass.
 */
class MostViewedFragment : Fragment() {

    var carpets = ArrayList<Carpet>()
    var db = FirebaseFirestore.getInstance()
    var docID = ArrayList<String>()
    var carpetAdapter: CarpetListAdapter? = null
    var dialog: ACProgressFlower? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_most_viewed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog = ACProgressFlower.Builder(context)
            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
            .themeColor(Color.WHITE)
            .text("Loading...")
            .fadeColor(Color.DKGRAY).build()
        dialog!!.show()
        getCarpetDocs()
        Log.i("Document retrieval success", carpets.size.toString())
        carpetAdapter = CarpetListAdapter(carpets, context!!)
        carpetListView.adapter = carpetAdapter
        super.onViewCreated(view, savedInstanceState)
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

    fun getCarpetDocs() {
        db.collection("Carpets")
            .get()
            .addOnSuccessListener { documents ->
                carpets.clear()
                docID.clear()
                for (document in documents) {
                    var docRef = db.collection("Carpets").document(document.id)
                    docRef.get()
                        .addOnSuccessListener { snapshot ->
                            if (snapshot != null) {
                                var dataDescription = snapshot.data!!
                                carpets.add(Carpet(dataDescription["name"].toString(),
                                    dataDescription["length"].toString().toInt(),
                                    dataDescription["breadth"].toString().toInt(),
                                    dataDescription["modelURL"].toString(),
                                    dataDescription["description"].toString(),
                                    dataDescription["category"].toString(),
                                    dataDescription["mostViewed"].toString().toBoolean(),
                                    document.id))
                                carpetAdapter!!.notifyDataSetChanged()
                                docID.add(document.id)
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                Snackbar.make(relativeLayout, exception.toString(), Snackbar.LENGTH_LONG).show()
            }
    }
}
