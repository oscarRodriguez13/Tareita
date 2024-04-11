package com.example.tareita.domain

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.tareita.R

class HistorialAdapter(context: Context, historialList: List<Historial>) :
    ArrayAdapter<Historial>(context, 0, historialList.toTypedArray()) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.historial_adaptador, parent, false)
        }

        val currentItem = getItem(position)

        val latitud = itemView?.findViewById<TextView>(R.id.latitud)
        val longitud = itemView?.findViewById<TextView>(R.id.longitud)
        val fecha = itemView?.findViewById<TextView>(R.id.fecha)

        latitud?.text = currentItem?.latitud.toString()
        longitud?.text = currentItem?.longitud.toString()
        fecha?.text = currentItem?.fecha.toString()

        return itemView!!
    }
}
