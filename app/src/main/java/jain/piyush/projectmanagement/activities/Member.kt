package jain.piyush.projectmanagement.activities

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jain.piyush.projectmanagement.R
import jain.piyush.projectmanagement.adapters.MemberListItemAdapter
import jain.piyush.projectmanagement.databinding.ActivityMemberBinding
import jain.piyush.projectmanagement.firebase.FirestoreClass
import jain.piyush.projectmanagement.models.Board
import jain.piyush.projectmanagement.models.User
import jain.piyush.projectmanagement.utiles.Constants
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class Member : BaseActivity() {
    private var anyChangesMade : Boolean = false
    private lateinit var mAssignedMemberList : ArrayList<User>
    private lateinit var mBoardDetails : Board
    private var mBinding : ActivityMemberBinding? = null
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityMemberBinding.inflate(layoutInflater)
        setContentView(mBinding?.root)
        setSupportActionBar(mBinding?.memberToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding?.memberToolbar?.setNavigationOnClickListener {
            onBackPressed()

        }
        supportActionBar?.title = resources.getString(R.string.members)

        if (intent.hasExtra(Constants.BOARD_DETAILS)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAILS)!!
        }
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersList(this,mBoardDetails.assignedTo)
    }
    fun setUpMemberList(list : ArrayList<User>){
        mAssignedMemberList = list
        val rv_member_list : RecyclerView = findViewById(R.id.rv_members_list)
        hideProgressDialog()
        rv_member_list.layoutManager = LinearLayoutManager(this)
        rv_member_list.setHasFixedSize(true)
        val adapter = MemberListItemAdapter(this,list)
        rv_member_list.adapter = adapter
    }
    fun memberDetails(user : User){
      mBoardDetails.assignedTo.add(user.id)
        FirestoreClass().assignedTOBoard(this,mBoardDetails,user)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member ->{
                dialogSearch()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearch(){
        val dialog = Dialog(this)
            dialog.setContentView(R.layout.add_member)
        dialog.setCancelable(false)
        dialog.findViewById<TextView>(R.id.addButton).setOnClickListener {
          val email = dialog.findViewById<EditText>(R.id.et_email_member).text.toString()
            if (email.isNotEmpty()){
                dialog.dismiss()
                showProgressDialog(getString(R.string.please_wait))
                FirestoreClass().getMembersDetails(this,email)
            }else{
                Toast.makeText(this,"Please enter Email",Toast.LENGTH_SHORT).show()
            }
        }
        dialog.findViewById<TextView>(R.id.cancleButton).setOnClickListener {
           dialog.dismiss()
        }

        dialog.show()
    }
    fun memberAssignedSuccessCall(user: User){
        hideProgressDialog()
        mAssignedMemberList.add(user)
        anyChangesMade = true
        setUpMemberList(mAssignedMemberList)
        SendNotificationToUserAsyncTask(mBoardDetails.name,user.fcmToken).execute()
    }

    private inner class SendNotificationToUserAsyncTask(val boardName : String,val token: String) : AsyncTask<Any,Any,String>(){
        @Deprecated("Deprecated in Java")
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(getString(R.string.please_wait))
        }
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Any?): String {
           var result : String
           var connection : HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty(Constants.FCM_AUTHORIZATION,"${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}")
                connection.useCaches = false
                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE,"Assigned to the Board $boardName")
                dataObject.put(Constants.FCM_KEY_MESSAGE,"You have been assigned to new Board by ${mAssignedMemberList[0].name}")
                jsonRequest.put(Constants.FCM_KEY_DATA,dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO,token)
                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()
                val httpResult:Int = connection.responseCode
                if (httpResult == HttpURLConnection.HTTP_OK){
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val sb = StringBuilder()
                    var line : String?
                    try {
                        while (reader.readLine().also {line =it} != null){
                            sb.append(line+"\n")
                        }

                    }catch (e:IOException){
                        e.printStackTrace()
                    }finally {
                        try {
                            inputStream.close()
                        }catch (e:IOException){
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                }else{
                    result = connection.responseMessage
                }

            }catch (e:SocketTimeoutException){
                result = "connection TimeOut"
            }catch (e:Exception){
                result = "Error : " + e.message
            }finally {
                connection?.disconnect()
            }
           return result
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            hideProgressDialog()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
       if (anyChangesMade){
           setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }
    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }

}