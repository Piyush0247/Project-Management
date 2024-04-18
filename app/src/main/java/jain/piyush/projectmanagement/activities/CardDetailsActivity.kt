package jain.piyush.projectmanagement.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.GridLayoutManager
import jain.piyush.projectmanagement.R
import jain.piyush.projectmanagement.adapters.SelectMemberListAdapter
import jain.piyush.projectmanagement.databinding.ActivityCardDetailsBinding
import jain.piyush.projectmanagement.dialogs.LabelColorLIstDialog
import jain.piyush.projectmanagement.dialogs.MemberListDialog
import jain.piyush.projectmanagement.firebase.FirestoreClass
import jain.piyush.projectmanagement.models.Board
import jain.piyush.projectmanagement.models.Card
import jain.piyush.projectmanagement.models.SelectedMember
import jain.piyush.projectmanagement.models.Task
import jain.piyush.projectmanagement.models.User
import jain.piyush.projectmanagement.utiles.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CardDetailsActivity : BaseActivity() {
    private var mSelectedDueData : Long = 0
    private lateinit var mMembersDetailsList : ArrayList<User>
    private var mSelectedColor = ""
    private lateinit var mBoardDetails : Board
    private var mCardPosition = -1
    private  var taskListPosition = -1
    private var cDBinding : ActivityCardDetailsBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cDBinding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(cDBinding?.root)
        setSupportActionBar(cDBinding?.cardDetailsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        cDBinding?.cardDetailsToolbar?.setNavigationOnClickListener { onBackPressed() }

        getIntentData()
        supportActionBar?.title = mBoardDetails
            .taskList[taskListPosition]
            .cards[mCardPosition].name
        cDBinding?.etNameCardDetails?.setText(mBoardDetails.taskList[taskListPosition].cards[mCardPosition].name)
        cDBinding?.etNameCardDetails?.setSelection(cDBinding?.etNameCardDetails?.text.toString().length)
        cDBinding?.btnUpdateCardDetails?.setOnClickListener {
            if ( cDBinding?.etNameCardDetails?.text.toString().isNotEmpty()){
                updateCardDetails()
            }else{
                Toast.makeText(this,"Please enter the card name",Toast.LENGTH_SHORT).show()
            }
        }
        mSelectedColor = mBoardDetails.taskList[taskListPosition].cards[mCardPosition].labelColor
        if (mSelectedColor.isNotEmpty()){
            setColor()
        }
        cDBinding?.tvSelectLabelColor?.setOnClickListener {
            labelColorListDialog()
        }
        cDBinding?.tvSelectMembers?.setOnClickListener {
            memberListDialog()
        }
        setUpSelectedMemberList()
        mSelectedDueData = mBoardDetails.taskList[taskListPosition].cards[mCardPosition].dueData
        if (mSelectedDueData > 0){
            val dateFormat = SimpleDateFormat("dd/MM//yyyy", Locale.ENGLISH)
            val selectedDate = dateFormat.format(Date(mSelectedDueData))
            cDBinding?.tvSelectDueDate?.text = selectedDate
        }
        cDBinding?.tvSelectDueDate?.setOnClickListener {
            datePicker()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)

    }
    private fun deteleCard(){
        val cardList : ArrayList<Card> = mBoardDetails.taskList[taskListPosition].cards
        cardList.removeAt(mCardPosition)
        val taskList : ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)
        taskList[taskListPosition].cards = cardList
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    private fun colorList():ArrayList<String>{
        val colorlist :ArrayList<String> = ArrayList()
        colorlist.add("#136f63")
        colorlist.add("#032b43")
        colorlist.add("#3f88c5")
        colorlist.add("#ffba08")
        colorlist.add("#d00000")
        colorlist.add("#f15bb5")
        return colorlist

    }
    private fun setColor(){
        cDBinding?.tvSelectLabelColor?.text = ""
        cDBinding?.tvSelectLabelColor?.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card -> {
                alertDialog( mBoardDetails.taskList[taskListPosition].cards[mCardPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun addUpdateCardDetailsSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
    private fun getIntentData(){
        if (intent.hasExtra(Constants.BOARD_DETAILS)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAILS)!!
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            taskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBER_LIST)){
            mMembersDetailsList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBER_LIST)!!
        }
    }
    private fun updateCardDetails(){
        val card = Card(
            cDBinding?.etNameCardDetails?.text.toString(),
            mBoardDetails.taskList[taskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[taskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,mSelectedDueData
        )
        val taskList : ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size -1)
        mBoardDetails.taskList[taskListPosition].cards[mCardPosition] = card
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    private fun labelColorListDialog(){
        val colorList : ArrayList<String> = colorList()
        val listDialog = object :LabelColorLIstDialog(
            this,resources.getString(R.string.select_label_color),colorList,mSelectedColor){
            override fun onItemSelected(color: String) {
               mSelectedColor = color
                setColor()
            }

        }
        listDialog.show()
    }
    private fun memberListDialog(){
        val cardAssignedList = mBoardDetails.taskList[taskListPosition].cards[mCardPosition].assignedTo
        if (cardAssignedList.size > 0){
            for (i in mMembersDetailsList.indices){
                for (j in cardAssignedList){
                    if (mMembersDetailsList[i].id == j){
                        mMembersDetailsList[i].selected = true
                    }
                }
            }
        }else{
            for (i in mMembersDetailsList.indices){
                mMembersDetailsList[i].selected = false
            }
        }
        val listDialog = object : MemberListDialog(this,mMembersDetailsList,getString(R.string.select_member)){
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT){
                    if (!mBoardDetails.taskList[taskListPosition].cards[mCardPosition].assignedTo.contains(user.id)){
                        mBoardDetails.taskList[taskListPosition].cards[mCardPosition].assignedTo.add(user.id)
                    }
                }
                else{
                    mBoardDetails.taskList[taskListPosition].cards[mCardPosition].assignedTo.remove(user.id)
                    for (i in mMembersDetailsList.indices){
                        if (mMembersDetailsList[i].id == user.id){
                            mMembersDetailsList[i].selected = false
                        }
                    }

                }
                setUpSelectedMemberList()
            }

        }
        listDialog.show()

    }
    private fun setUpSelectedMemberList(){
        val cardAssignedMemberList =
            mBoardDetails.taskList[taskListPosition].cards[mCardPosition].assignedTo
        val selectedMemberList : ArrayList<SelectedMember> = ArrayList()
        for (i in mMembersDetailsList.indices){
            for (j in cardAssignedMemberList){
                if (mMembersDetailsList[i].id == j){
                   val memberSelect = SelectedMember(
                       mMembersDetailsList[i].id,
                       mMembersDetailsList[i].image
                   )
                    selectedMemberList.add(memberSelect)
                    }
                }
            }
        if (selectedMemberList.size >0){
            selectedMemberList.add(SelectedMember("",""))
            cDBinding?.tvSelectMembers?.visibility = View.GONE
            cDBinding?.rvSelectedMember?.visibility = View.VISIBLE
            cDBinding?.rvSelectedMember?.layoutManager = GridLayoutManager(this,6)
           val adapter = SelectMemberListAdapter(this,selectedMemberList,true)
            cDBinding?.rvSelectedMember?.adapter = adapter
            adapter.setOnClickListener(
                object:SelectMemberListAdapter.OnClickListener{
                    override fun onClick() {
                        memberListDialog()
                    }

                }
            )
        }else{
            cDBinding?.tvSelectMembers?.visibility = View.VISIBLE
            cDBinding?.rvSelectedMember?.visibility = View.GONE
        }

    }
    private fun alertDialog(cardName : String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage(resources.getString(R.string.confirmation_to_delete_card))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){ dialogInterface, _ ->
            dialogInterface.dismiss()
            deteleCard()

        }
        builder.setNegativeButton("No"){ dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alterDialog : AlertDialog = builder.create()
        alterDialog.setCancelable(false)
        alterDialog.show()
    }
    private fun datePicker(){
        val  c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(this,DatePickerDialog.OnDateSetListener{view, year, month, dayOfMonth ->
            val sDayOfMonth = if (dayOfMonth<10) "0$dayOfMonth" else "$dayOfMonth"
            val sMonthOfYear = if ((month + 1) > 10) "0${dayOfMonth + 1}" else "${dayOfMonth +1}"
            val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
            cDBinding?.tvSelectDueDate?.text = selectedDate
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val theDate = sdf.parse(selectedDate)
            mSelectedDueData = theDate!!.time

        },
            year,month,day
        )
        dpd.show()
    }
}