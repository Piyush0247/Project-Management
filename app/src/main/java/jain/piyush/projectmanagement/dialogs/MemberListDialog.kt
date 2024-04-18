package jain.piyush.projectmanagement.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jain.piyush.projectmanagement.R
import jain.piyush.projectmanagement.adapters.MemberListItemAdapter
import jain.piyush.projectmanagement.models.User

abstract class MemberListDialog
    (context: Context,
     private var list : ArrayList<User>,
     private var  title : String = "")
    :Dialog(context){
        private var adapter : MemberListItemAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.color_dialog,null,false)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecycleView(view)
    }
    private fun setUpRecycleView(view : View){
        view.findViewById<TextView>(R.id.tvTitle).text = title
        if (list.size >0){
            view.findViewById<RecyclerView>(R.id.rvList).layoutManager = LinearLayoutManager(context)
            adapter = MemberListItemAdapter(context,list)
            view.findViewById<RecyclerView>(R.id.rvList).adapter = adapter
            adapter!!.setOnClickListener (object : MemberListItemAdapter.OnClickListener{
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user,action)
                }

            })
        }

    }
    protected abstract fun onItemSelected(user: User,action : String)

}