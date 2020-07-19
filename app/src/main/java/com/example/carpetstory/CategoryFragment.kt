package com.example.carpetstory


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_category.*
import kotlinx.android.synthetic.main.ticket_category.view.*

/**
 * A simple [Fragment] subclass.
 */
class CategoryFragment : Fragment() {

    var categories = ArrayList<String>()
    var db = FirebaseFirestore.getInstance()
    var dialog: ACProgressFlower? = null
    var categoryAdapter: categoryListAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//      TODO  dialog = ACProgressFlower.Builder(context)
//            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
//            .themeColor(Color.WHITE)
//            .text("Loading...")
//            .fadeColor(Color.DKGRAY).build()
//        dialog!!.show()
        categories.add("Classical")
        categories.add("Heritage")
        categories.add("Modern")
        categories.add("Shalimar")
        //TODO getCategories()
        categoryGridView.numColumns = 2
        categoryGridView.horizontalSpacing = 15
        categoryGridView.verticalSpacing = 15
        categoryGridView.stretchMode = GridView.STRETCH_COLUMN_WIDTH
        categoryAdapter = categoryListAdapter(categories, context!!)
        categoryGridView.adapter = categoryAdapter
        super.onViewCreated(view, savedInstanceState)
    }

    inner class categoryListAdapter(categories: ArrayList<String>, context: Context): BaseAdapter() {
        var categories = ArrayList<String>()
        var context: Context? = null

        init {
            this.categories = categories
            this.context = context
        }
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            var inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var currentCategory = categories[p0]
            Log.i("Category", currentCategory)
            var currentListView = inflater.inflate(R.layout.ticket_category, null)
            currentListView.categoryNameTextView.text = currentCategory
            currentListView.categoryLayout.setOnClickListener {
                var intent = Intent(context, CarpetsListViewActivity::class.java)
                intent.putExtra("category", currentCategory)
                intent.putExtra("searchResult", false)
                startActivity(intent)
            }
            return  currentListView
        }

        override fun getItem(p0: Int): Any {
            return categories[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {
            return categories.size
        }

    }

    fun getCategories() {
        db.collection("Carpets")
            .get()
            .addOnSuccessListener { documents ->
                categories.clear()
                for (document in documents) {
                    var docRef = db.collection("Carpets").document(document.id)
                    docRef.get()
                        .addOnSuccessListener { snapshot ->
                            if (snapshot != null) {
                                var dataDescription = snapshot.data!!
                                if ((categories.isEmpty()) || !(categories.contains(dataDescription["category"].toString()))) {
                                    categories.add(dataDescription["category"].toString())
                                    categoryAdapter!!.notifyDataSetChanged()
                                    dialog!!.dismiss()
                                }
                            }
                        }
                }
            }
        }
    }
