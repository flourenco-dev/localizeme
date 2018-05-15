package com.fabiolourenco.localizeme.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.fabiolourenco.localizeme.database.entity.Journey

/**
 * JourneyAdapter is used to populate a Spinner with a list of Journey objects (to be replaced with a RecyclerView)
 */
class JourneyAdapter(context: Context, private var journeys: ArrayList<Journey>,
                     private var journeySelectedCallback: JourneySelectedCallback) :
        ArrayAdapter<Journey>(context, android.R.layout.simple_spinner_dropdown_item, journeys),
        AdapterView.OnItemSelectedListener {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //TODO: switch to ViewHolder pattern when switching the Spinner view to a RecyclerView
        val view = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        view.findViewById<TextView>(android.R.id.text1).text = journeys[position].getFullName()
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        view.findViewById<TextView>(android.R.id.text1).text = journeys[position].getName()
        return view
    }

    /**
     * Update Journey list to avoid creation of new adapter instances (not used for now)
     */
    fun setJourneyList(journeys: ArrayList<Journey>) {
        this.journeys = journeys
        notifyDataSetChanged()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // A Journey was selected, use callback to notify parent
        journeySelectedCallback.onSelected(journeys[position])
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Nothing selected, keep things as is
    }

    /**
     * ViewHolder that will contain views for when this adapter is used to a RecyclerView instead of a Spinner
     */
    private class ViewHolder {

    }

}