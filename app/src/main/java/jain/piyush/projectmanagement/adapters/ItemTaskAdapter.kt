package jain.piyush.projectmanagement.adapters

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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import jain.piyush.projectmanagement.R
import jain.piyush.projectmanagement.activities.TaskListActivity
import jain.piyush.projectmanagement.models.Task
import java.util.Collections

open class ItemTaskAdapter(private val context : Context,
                           private var list : ArrayList<Task>)
    : RecyclerView.Adapter<ViewHolder>()
{
  private var mPositionDragedFrom = -1
    private var mPositionDragedTo = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view = LayoutInflater.from(context).inflate(R.layout.item_task,parent,false)
        val layoutParameter = LinearLayout.LayoutParams(
            (parent.width*0.7).toInt(),LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParameter.setMargins((15.toDp().toPx()),0,(40.toDp()).toPx(),0)
        view.layoutParams = layoutParameter
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SuspiciousIndentation", "CutPasteId")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder){
            if (position == list.size-1){
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.VISIBLE
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility = View.GONE
            }else{
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility = View.VISIBLE
            }
            holder.itemView.findViewById<TextView>(R.id.tv_task_list_title).text = model.title
            holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility = View.VISIBLE
            }
            holder.itemView.findViewById<ImageButton>(R.id.ib_close_list_name).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.VISIBLE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility = View.GONE
            }
            holder.itemView.findViewById<ImageButton>(R.id.ib_done_list_name).setOnClickListener{
              val listName = holder.itemView.findViewById<EditText>(R.id.et_task_list_name).text.toString()

                if (listName.isNotEmpty()){
                    if (context is TaskListActivity){
                        context.createTasklist(listName)
                    }else{
                        Toast.makeText(context,"Please Enter list name",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            holder.itemView.findViewById<ImageButton>(R.id.ib_edit_list_name).setOnClickListener {
                holder.itemView.findViewById<EditText>(R.id.et_task_list_name).setText(model.title)
                holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility = View.VISIBLE
            }
            holder.itemView.findViewById<ImageButton>(R.id.ib_close_editable_view).setOnClickListener {
                holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility = View.VISIBLE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility = View.GONE
            }
            holder.itemView.findViewById<ImageButton>(R.id.ib_done_edit_list_name).setOnClickListener {
                val listName = holder.itemView
                    .findViewById<EditText>(R.id.et_edit_task_list_name)
                    .text.toString()
                if (listName.isNotEmpty()){
                    if (context is TaskListActivity){
                        context.updateTasklist(position,listName,model)
                    }else{
                        Toast.makeText(context,"Please Enter list name",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            holder.itemView.findViewById<ImageButton>(R.id.ib_delete_list).setOnClickListener {
                alertDialog(position,model.title)

            }
            holder.itemView.findViewById<TextView>(R.id.tv_add_card).setOnClickListener{
                holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility = View.VISIBLE

            }
            holder.itemView.findViewById<ImageButton>(R.id.ib_close_card_name).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility = View.VISIBLE
                holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility = View.GONE
            }
            holder.itemView.findViewById<ImageButton>(R.id.ib_done_card_name).setOnClickListener {
                val cardName = holder.itemView
                    .findViewById<EditText>(R.id.et_card_name)
                    .text.toString()
                if (cardName.isNotEmpty()){
                    if (context is TaskListActivity){
                        context.createCardList(position,cardName)
                    }
                }else{
                    Toast.makeText(context,"Please enter card name",Toast.LENGTH_SHORT).show()
                }
            }
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).layoutManager = LinearLayoutManager(context)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).setHasFixedSize(true)
            val adapter = CardListItemAdapter(context,model.cards)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).adapter = adapter
            adapter.setOnClickListener(
                object : CardListItemAdapter.OnClickListener{
                    override fun onClick(cardPosition: Int) {
                        if (context is TaskListActivity){
                            context.cardDetails(position,cardPosition)
                        }
                    }
                }
            )
            val dividerItem = DividerItemDecoration(context,DividerItemDecoration.VERTICAL)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).addItemDecoration(dividerItem)
            val itemTouchHelper = ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN,0
                ){
                    override fun onMove(
                        recyclerView: RecyclerView,
                        dragged: ViewHolder,
                        target: ViewHolder
                    ): Boolean {
                        val dragedPosition = dragged.adapterPosition
                        val targetPosition = target.adapterPosition
                        if (mPositionDragedFrom == -1){
                            mPositionDragedFrom = dragedPosition
                        }
                        mPositionDragedTo = targetPosition
                        Collections.swap(list[position].cards,dragedPosition,targetPosition)
                        adapter.notifyItemMoved(dragedPosition,targetPosition)
                        return false
                    }

                    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                    }

                    override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
                        super.clearView(recyclerView, viewHolder)
                        if (mPositionDragedFrom != -1 && mPositionDragedTo != -1 && mPositionDragedFrom != mPositionDragedTo){
                            (context as TaskListActivity).upDateCardInTakList(
                                position,list[position].cards
                            )
                        }
                        mPositionDragedFrom = -1
                        mPositionDragedTo = -1
                    }

                }

            )
            itemTouchHelper.attachToRecyclerView(holder.itemView.findViewById(R.id.rv_card_list))
        }
    }
    private fun alertDialog(position : Int, title : String){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete it ")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){ dialogInterface, _ ->
            dialogInterface.dismiss()
            if (context is TaskListActivity){
                context.deleteTaskList(position)
            }
        }
        builder.setNegativeButton("No"){ dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alterDialog : AlertDialog = builder.create()
        alterDialog.setCancelable(false)
        alterDialog.show()
    }
    private fun Int.toDp():Int = (this/ Resources.getSystem().displayMetrics.density).toInt()
    private fun Int.toPx():Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    class MyViewHolder(view : View): ViewHolder(view)

}