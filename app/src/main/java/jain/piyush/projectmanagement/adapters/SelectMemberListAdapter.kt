package jain.piyush.projectmanagement.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.mikhaellopez.circularimageview.CircularImageView
import jain.piyush.projectmanagement.R
import jain.piyush.projectmanagement.R.id
import jain.piyush.projectmanagement.models.SelectedMember


open class SelectMemberListAdapter(
    private val context: Context,
    private var list: ArrayList<SelectedMember>,
    private val assignedMemeber:Boolean
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
                R.layout.ic_card_selected_member,
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

            if (position == list.size - 1 && assignedMemeber) {
                holder.itemView.findViewById<CircularImageView>(id.iv_add_member).visibility = View.VISIBLE
                holder.itemView.findViewById<CircularImageView>(id.iv_selected_member_image).visibility = View.GONE
            } else {
                holder.itemView.findViewById<CircularImageView>(id.iv_add_member).visibility = View.GONE
                holder.itemView.findViewById<CircularImageView>(id.iv_selected_member_image).visibility = View.VISIBLE

                Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(holder.itemView.findViewById<CircularImageView>(id.iv_selected_member_image))
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick()
                }
            }
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
        fun onClick()
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(view: View) : ViewHolder(view)
}