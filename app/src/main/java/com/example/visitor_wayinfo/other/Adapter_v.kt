package com.example.visitor_wayinfo.other


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.visitor_wayinfo.R


class Adapter_v(


    val context: Context, val rvtwo: ArrayList<Demo>): RecyclerView.Adapter<Adapter_v.MyViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.listitem, parent, false))

    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
         holder.tvCount.text = "${position + 1}"
        // holder.itemName.text= list[position].itemName
        holder.barcodetxt.text = rvtwo[position].barcode
        holder.remarks_txt.text = rvtwo[position].remarks
        holder.name_txt.text=rvtwo[position].user
        holder.dep_txt.text=rvtwo[position].depa



    }


    open class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val barcodetxt: TextView = itemView.findViewById(R.id.barcodetxt)

        //val itemName: TextView = itemView.findViewById(R.id.tvItemNameRv)
        val remarks_txt: TextView = itemView.findViewById(R.id.remarks_txt)

        val tvCount: TextView = itemView.findViewById(R.id.tvCount)
        val name_txt:TextView=itemView.findViewById(R.id.name_txt)
        val dep_txt:TextView=itemView.findViewById(R.id.dep_txt)


    }


    override fun getItemCount(): Int {
        return rvtwo.size
    }




}