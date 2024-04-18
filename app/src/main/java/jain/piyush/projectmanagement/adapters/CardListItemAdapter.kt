package jain.piyush.projectmanagement.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import jain.piyush.projectmanagement.R
import jain.piyush.projectmanagement.activities.TaskListActivity
import jain.piyush.projectmanagement.models.Card
import jain.piyush.projectmanagement.models.SelectedMember

open class CardListItemAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
) : RecyclerView.Adapter<ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            if (model.labelColor.isNotEmpty()){
                holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.VISIBLE
                holder.itemView.findViewById<View>(R.id.view_label_color).setBackgroundColor(Color.parseColor(model.labelColor))
            }else{
                holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.GONE
            }

            holder.itemView.findViewById<TextView>(R.id.tv_card_name).text = model.name
            if ((context as TaskListActivity).mAssignedMemberDetailList.size > 0){
                    val selectedMemberList : ArrayList<SelectedMember> = ArrayList()
                for (i in context.mAssignedMemberDetailList.indices){
                   for (j in model.assignedTo){
                       if (context.mAssignedMemberDetailList[i].id == j){
                           val selectedMembers = SelectedMember(context.mAssignedMemberDetailList[i].id,
                               context.mAssignedMemberDetailList[i].image)
                           selectedMemberList.add(selectedMembers)
                       }
                   }
                }
                if (selectedMemberList.size >0){
                    if (selectedMemberList.size == 1 && selectedMemberList[0].id == model.createdBy){
                        holder.itemView.findViewById<RecyclerView>(R.id.rvCard_selected_MemberList).visibility = View.GONE
                    }else{
                        holder.itemView.findViewById<RecyclerView>(R.id.rvCard_selected_MemberList).visibility = View.VISIBLE
                        holder.itemView.findViewById<RecyclerView>(R.id.rvCard_selected_MemberList).layoutManager = GridLayoutManager(context,4)
                        val adapter = SelectMemberListAdapter(context,selectedMemberList,false)
                        holder.itemView.findViewById<RecyclerView>(R.id.rvCard_selected_MemberList).adapter = adapter
                        adapter.setOnClickListener(object :SelectMemberListAdapter.OnClickListener{
                            override fun onClick() {
                                if (onClickListener != null){
                                    onClickListener!!.onClick(position)
                                }

                            }

                        })
                    }
                }else{
                    holder.itemView.findViewById<RecyclerView>(R.id.rvCard_selected_MemberList).visibility = View.GONE
                }
            }

            // TODO (Step 7: Set a click listener to the card item view.)
            // START
            holder.itemView.setOnClickListener{
                if (onClickListener != null) {
                    onClickListener!!.onClick(position)
                }
            }
            // END
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A function for OnClickListener where the Interface is the expected parameter..
     */
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    /**
     * An interface for onclick items.
     */
    interface OnClickListener {
        fun onClick(cardPosition: Int)
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(view: View) : ViewHolder(view)
}