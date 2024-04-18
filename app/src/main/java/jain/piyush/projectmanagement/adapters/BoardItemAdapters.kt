package jain.piyush.projectmanagement.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jain.piyush.projectmanagement.R
import jain.piyush.projectmanagement.models.Board

open class BoardItemAdapters(private val context : Context,
                             private var list : ArrayList<Board>):
RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListner:OnClickListner? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       return myViewHolder(LayoutInflater.from(context).inflate(R.layout.item_board,parent,false))
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
      val model = list[position]
        if (holder is myViewHolder){
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.itemView.findViewById(R.id.iv_board_image))

        holder.itemView.findViewById<TextView>(R.id.tv_name).text = model.name
            holder.itemView.findViewById<TextView>(R.id.tv_createdBy).text = "Created By:${model.createdBy}"
            holder.itemView.setOnClickListener {
                if (onClickListner != null){
                    onClickListner?.onClick(position,model)
                }
            }
        }
    }
    interface OnClickListner{
        fun onClick(position:Int,model:Board)
    }
    fun setOnClickListner(onClickListner : OnClickListner){
     this.onClickListner = onClickListner
    }
    private class myViewHolder(view : View):RecyclerView.ViewHolder(view){

    }
}