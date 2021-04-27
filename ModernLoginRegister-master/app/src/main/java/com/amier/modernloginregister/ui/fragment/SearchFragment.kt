package com.amier.modernloginregister.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.amier.modernloginregister.R

class SearchFragment : Fragment(R.layout.fragment_search) {
    var etType: EditText? = null
    var etZipcode: EditText? = null
    var etDistance: EditText? = null
    var xploreButton: Button? = null

    interface onXploreClickListener {
        fun onXploreClicked(type: String, zipcode: String, nearbyDistance: String)
    }

    private lateinit var xploreClickListener: onXploreClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is onXploreClickListener) {
            xploreClickListener = context
        } else {
            throw ClassCastException(
                context.toString() + " must implement OnProductSelected."
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
    }

    private fun setUpViews(view: View) {
        etType = view.findViewById(R.id.etType)
        etZipcode = view.findViewById(R.id.et_zipcode)
        xploreButton = view.findViewById(R.id.xploreButton)


        xploreButton?.setOnClickListener {
            val typeValue = etType?.text.toString().trim { it <= ' ' }
            val zipcodeValue = etZipcode?.text.toString().trim { it <= ' ' }
            val distanceValue = etDistance?.text.toString().trim { it <= ' ' }
            xploreClickListener.onXploreClicked(typeValue, zipcodeValue, distanceValue)
        }
    }
}