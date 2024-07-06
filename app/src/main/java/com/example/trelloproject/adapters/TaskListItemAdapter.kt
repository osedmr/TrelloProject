package com.example.trelloproject.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.setMargins
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloproject.R
import com.example.trelloproject.activities.TaskListActivity
import com.example.trelloproject.models.Task

class TaskListItemAdapter(private val context: Context,private var list: ArrayList<Task>)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view=LayoutInflater.from(context).inflate(R.layout.item_task,parent,false)
        val layoutParams=LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins((15.toDP().toPx()),0,(40.toDP()).toPx(),0)
        view.layoutParams=layoutParams
        return MyViewHolder(view)

    }

    override fun getItemCount(): Int {
        return list.size

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder,position: Int) {
        val model = list[position]
        if (holder is MyViewHolder){
            if (position==list.size-1){
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.VISIBLE
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility=View.GONE
            }else{
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility=View.VISIBLE
            }
            holder.itemView.findViewById<TextView>(R.id.tv_task_list_title).text=model.title
            holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility=View.VISIBLE
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_close_list_name).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.VISIBLE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility=View.GONE
            }
            holder.itemView.findViewById<ImageButton>(R.id.ib_done_list_name).setOnClickListener {
                // tablo oluştur yeri

                val listName=holder.itemView.findViewById<EditText>(R.id.et_task_list_name).text.toString()
                if (listName.isNotEmpty()){
                    if (context is TaskListActivity){
                        context.createTaskList(listName)
                    }
                }else{
                    Toast.makeText(context,"Liste adını giriniz",Toast.LENGTH_LONG).show()
                }
            }
        }
        holder.itemView.findViewById<ImageButton>(R.id.ib_edit_list_name).setOnClickListener {
            holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name).setText(model.title)
            holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility=View.GONE
            holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility=View.VISIBLE

        }
        holder.itemView.findViewById<ImageButton>(R.id.ib_close_editable_view).setOnClickListener {
            holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility = View.VISIBLE
            holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility=View.GONE
        }
        holder.itemView.findViewById<ImageButton>(R.id.ib_done_edit_list_name).setOnClickListener {
            //düzenleme buttonu yeri sonra yazıcam güncelleme
            val listName=holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name).text.toString()

            if (listName.isNotEmpty()){
                if (context is TaskListActivity){
                    context.updateTaskList(position,listName,model)
                }
            }else{
                Toast.makeText(context,"Liste adını giriniz",Toast.LENGTH_LONG).show()
            }
        }
        holder.itemView.findViewById<ImageButton>(R.id.ib_delete_list).setOnClickListener {
            alertDialogForDeleteList(position,model.title)
        }

        holder.itemView.findViewById<TextView>(R.id.tv_add_card).setOnClickListener {
            holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility=View.GONE
            holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility=View.VISIBLE
        }

        holder.itemView.findViewById<ImageButton>(R.id.ib_close_card_name).setOnClickListener {
            holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility=View.VISIBLE
            holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility=View.GONE
        }

        holder.itemView.findViewById<ImageButton>(R.id.ib_done_card_name).setOnClickListener {
            // tablo oluştur yeri

            val cardName=holder.itemView.findViewById<EditText>(R.id.et_card_name).text.toString()
            if (cardName.isNotEmpty()){
                if (context is TaskListActivity){
                    //alt card ekleme yeri
                    context. addCardToTaskList(position,cardName)
                }
            }else{
                Toast.makeText(context,"Card adını giriniz",Toast.LENGTH_LONG).show()
            }
        }

        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).layoutManager=LinearLayoutManager(context)
        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).setHasFixedSize(true)

        val adapter=CardListItemsAdapter(context,model.cards)
        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).adapter=adapter

        adapter.setOnClickListener(
            object :CardListItemsAdapter.OnClickListener{
                override fun onClick(cardPosition: Int) {
                    if (context is TaskListActivity){
                        context.cardDetails(holder.adapterPosition, cardPosition)
                    }
                }
            }
        )

    }
    private fun Int.toDP():Int=
        (this/Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx():Int=
        (this * Resources.getSystem().displayMetrics.density).toInt()


    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Uyarı!!")
        builder.setMessage("Silmek istediğine eminmisin $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Evet") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed

            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }
        builder.setNegativeButton("Hayır") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }

        val alertDialog: AlertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    class MyViewHolder(view: View):RecyclerView.ViewHolder(view)
}