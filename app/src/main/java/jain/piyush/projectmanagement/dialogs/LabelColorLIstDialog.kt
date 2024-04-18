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
import jain.piyush.projectmanagement.adapters.LabelColorAdapter

abstract class LabelColorLIstDialog (
    context : Context,
    private var title : String = "",
    private var list : ArrayList<String>,
    private var mSelectedColor : String = ""):Dialog(context)
{
 private var adapter : LabelColorAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.color_dialog,null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecycleView(view)
    }

    private fun setUpRecycleView(view : View){
        view.findViewById<TextView>(R.id.tvTitle).text = title
        view.findViewById<RecyclerView>(R.id.rvList).layoutManager = LinearLayoutManager(context)
        adapter = LabelColorAdapter(context,list,mSelectedColor)
        view.findViewById<RecyclerView>(R.id.rvList).adapter = adapter
        adapter!!.onItemClickListner =    object :LabelColorAdapter.onItemClickListener{
            override fun onCLick(position: Int, color: String) {
                 dismiss()
                onItemSelected(color)
            }

        }
    }
    protected abstract fun onItemSelected(color : String)
}
