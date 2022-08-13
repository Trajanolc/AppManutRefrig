package com.example.myapplication.adpters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.entities.SimplifiedOrderDTO
import com.example.myapplication.services.CalendarServices

class HomeListAdapter (private val simplifiedOrderArray: List<SimplifiedOrderDTO>) :
    RecyclerView.Adapter<HomeListAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val unitID: TextView
        val unitEquip: TextView
        val unitDate: TextView
        init {
            // Define click listener for the ViewHolder's View.
            unitID = view.findViewById(R.id.orderUnitID)
            unitEquip = view.findViewById(R.id.orderUnitEquip)
            unitDate = view.findViewById(R.id.orderUnitDate)
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.order_unit, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.unitID.text = simplifiedOrderArray[position].ordemID.toString()
        viewHolder.unitEquip.text = "${simplifiedOrderArray[position].local} - ${simplifiedOrderArray[position].equipamento}"
        viewHolder.unitDate.text = CalendarServices.milisToDate(simplifiedOrderArray[position].dataFim)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = simplifiedOrderArray.size

}
