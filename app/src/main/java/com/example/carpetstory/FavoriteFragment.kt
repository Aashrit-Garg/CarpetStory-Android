package com.example.carpetstory


import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_favorite.*
import kotlinx.android.synthetic.main.ticket_carpet.view.*
import java.lang.Exception
import kotlin.concurrent.timerTask

/**
 * A simple [Fragment] subclass.
 */
class FavoriteFragment : Fragment() {

    var carpets = ArrayList<Carpet>()
    var db = FirebaseFirestore.getInstance()
    var docID = ArrayList<String>()
    var carpetAdapter: FavouriteCarpetListView? = null
    var dialog: ACProgressFlower? = null
    var favCarpetsID = ArrayList<String>()
    private var mAuth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser
        dialog = ACProgressFlower.Builder(context)
            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
            .themeColor(Color.WHITE)
            .text("Loading...")
            .fadeColor(Color.DKGRAY).build()
        dialog!!.show()
        getFavouriteCarpets()
        carpetAdapter = FavouriteCarpetListView(carpets, context!!)
        listViewFavorites.adapter = carpetAdapter
        super.onViewCreated(view, savedInstanceState)

    }

    inner class FavouriteCarpetListView(carpets: ArrayList<Carpet>, context: Context): BaseAdapter() {

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
                currentListView.textView3.text = currentCarpet.name
                Picasso.get().load(currentCarpet.modelURL).fit().centerCrop().into(currentListView.imageView2,
                    object : Callback {
                        override fun onSuccess() {
                            dialog!!.dismiss()
                        }

                        override fun onError(e: Exception?) {
                            //kuch nahi
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
                    intent.putExtra("loadSave", false)
                    startActivity(intent)
                }

                currentListView.ticketConstraint.setOnLongClickListener {
                    deleteDialog(currentCarpet.docID)
                    Log.i("Logcat Working ??", "Yes")
                    true
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

    fun getFavouriteCarpets() {
        db.collection("Favourites").document(user!!.uid).collection("Carpets")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    favCarpetsID.add(document.id)
                }
                if (favCarpetsID.isEmpty()) {
                    showText()
                    dialog!!.dismiss()
                } else {
                    loadFavouriteCarpets(favCarpetsID)
                }
            }
            .addOnFailureListener { exception ->
                Snackbar.make(relativeLayoutFavorites, exception.toString(), Snackbar.LENGTH_LONG).show()
            }
        }

    fun loadFavouriteCarpets(carpetsID: ArrayList<String>) {
        for (carpetId in carpetsID) {
            db.collection("Carpets").document(carpetId)
                .get()
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
                            carpetId))
                        carpetAdapter!!.notifyDataSetChanged()
                        docID.add(carpetId)
                    }
                }
                .addOnFailureListener { exception ->
                    Snackbar.make(relativeLayoutFavorites, exception.toString(), Snackbar.LENGTH_LONG).show()
                }
            }
        }

    fun deleteDialog(docID: String) {
        var builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("DELETE CARPET")
        builder.setMessage("Are you sure you want to delete this carpet from favourites ?")
        builder.setIcon(R.drawable.ic_warning_black_24dp)
        builder.setPositiveButton("Yes", object : DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                dialog!!.show()
                db.collection("Favourites").document(user!!.uid).collection("Carpets").document(docID).delete()
                    .addOnSuccessListener {
                        for (carpet in carpets) {
                            if (docID == carpet.docID) {
                                carpets.remove(carpet)
                                carpetAdapter!!.notifyDataSetChanged()
                                dialog!!.dismiss()
                            }
                        }
                        if (carpets.isEmpty()) {
                            showText()
                        }
                    }
            }

        })
        builder.setNegativeButton("No", object : DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {

            }

        })
        var alertDialog: AlertDialog = builder.create()
        alertDialog.show()
        }

    fun showText() {
        noFavTextView.alpha = 1.toFloat()
        }
    }
