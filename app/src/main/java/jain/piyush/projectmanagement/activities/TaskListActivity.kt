package jain.piyush.projectmanagement.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import jain.piyush.projectmanagement.R
import jain.piyush.projectmanagement.adapters.ItemTaskAdapter
import jain.piyush.projectmanagement.databinding.ActivityTaskListBinding
import jain.piyush.projectmanagement.firebase.FirestoreClass
import jain.piyush.projectmanagement.models.Board
import jain.piyush.projectmanagement.models.Card
import jain.piyush.projectmanagement.models.Task
import jain.piyush.projectmanagement.models.User
import jain.piyush.projectmanagement.utiles.Constants

class TaskListActivity : BaseActivity() {
    private lateinit var mBoardDetails : Board
    private lateinit var mBoardDocumentId : String
     lateinit var mAssignedMemberDetailList : ArrayList<User>
    private var tbinding : ActivityTaskListBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tbinding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(tbinding?.root)
        setSupportActionBar(tbinding?.taskListToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tbinding?.taskListToolbar?.setNavigationOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this,mBoardDocumentId)
    }
    fun cardDetails(taskListPosition: Int,cardPositioon : Int){
        val intent = Intent(this,CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAILS,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPositioon)
        intent.putExtra(Constants.BOARD_MEMBER_LIST,mAssignedMemberDetailList)
        startActivityForResult(intent, CARD_DETAIL_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MEMBER_REQUEST_CODE || requestCode == CARD_DETAIL_REQUEST_CODE){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardDetails(this,mBoardDocumentId)
        }else{
            Log.e("Cancled","Cancled")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
         menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_member ->{
                val intent = Intent(this,Member::class.java)
                intent.putExtra(Constants.BOARD_DETAILS,mBoardDetails)
                startActivityForResult(intent, MEMBER_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun boardDetails(board : Board){
        mBoardDetails = board
        hideProgressDialog()
        supportActionBar?.title =  mBoardDetails.name

        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersList(this,mBoardDetails.assignedTo)
    }
    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, mBoardDetails.documentId)
    }
    fun createTasklist(taskName : String){
        val task = Task(taskName,FirestoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    fun updateTasklist(position : Int, listName : String,model : Task){
        val task = Task(listName,model.createdBy)
        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    fun createCardList(position : Int,cardName : String){

        mBoardDetails.taskList.removeAt((mBoardDetails.taskList.size-1))
        val cardAssignedUser : ArrayList<String> = ArrayList()
        cardAssignedUser.add(FirestoreClass().getCurrentUserId())
        val card = Card(cardName,FirestoreClass().getCurrentUserId(),cardAssignedUser)
        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)
        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )
        mBoardDetails.taskList[position] = task
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    fun boardMemberDetailList(list : ArrayList<User>){
        mAssignedMemberDetailList = list
        hideProgressDialog()
        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)
        tbinding?.rvTaskList?.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,false)
        tbinding?.rvTaskList?.setHasFixedSize(true)

        val adapter = ItemTaskAdapter(this,mBoardDetails.taskList)
        tbinding?.rvTaskList?.adapter = adapter
    }
      fun upDateCardInTakList(taskListPosition :Int,cards : ArrayList<Card>){
          mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
          mBoardDetails.taskList[taskListPosition].cards = cards
          showProgressDialog(getString(R.string.please_wait))
          FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    companion object{
        const val MEMBER_REQUEST_CODE = 3
        const val CARD_DETAIL_REQUEST_CODE = 4
    }

    override fun onDestroy() {
        super.onDestroy()
        tbinding = null
    }
}