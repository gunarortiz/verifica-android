package com.verifica.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gunar.uta.data.PairData
import com.verifica.R
import com.verifica.model.firestore.SolicitudFire
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_service.view.*
import java.util.*
import kotlin.collections.ArrayList

class SolicitudAdapter(
    val items: ArrayList<PairData<SolicitudFire>>,
    val applicationContext: Context
): RecyclerView.Adapter<SolicitudAdapter.ChoferViewHolder>() {

    var onItemClick: ((PairData<SolicitudFire>) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoferViewHolder {
        return ChoferViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_service,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ChoferViewHolder, position: Int) {
        val pairData = items.get(position)
        var data = pairData.data

        holder.nombre.text = data.servicio!!.nombre

        val cal: Calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Bolivia"))
        cal.setTime(data.fecha_solicitud)
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DAY_OF_MONTH)

        val date = "$day/$month/$year"


        holder.fechaSolicitud.text = date
        holder.messagge.text = data.descripcion

        Glide.with(applicationContext)
            .load(data.servicio!!.logo)
            .into(holder.photo)


    }

    inner class ChoferViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val nombre = view.tv_name_service as TextView
        val fechaSolicitud = view.tv_date_service as TextView
        val messagge = view.tv_messagge_service as TextView
        val photo = view.iv_photo_service as ImageView

        init {
            view.setOnClickListener {
                onItemClick?.invoke(items[adapterPosition])
            }
        }
    }

}