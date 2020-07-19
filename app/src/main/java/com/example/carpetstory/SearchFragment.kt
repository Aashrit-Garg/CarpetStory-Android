package com.example.carpetstory


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_search.*

/**
 * A simple [Fragment] subclass.
 */
class SearchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchButton.setOnClickListener {
            onClick()
        }

    }

    fun onClick() {
        var intent = Intent(context, CarpetsListViewActivity::class.java)
        intent.putExtra("Length", editTextLength.text.toString())
        intent.putExtra("Breadth", editTextBreadth.text.toString())
        intent.putExtra("searchResult", true)
        startActivity(intent)
    }


}
