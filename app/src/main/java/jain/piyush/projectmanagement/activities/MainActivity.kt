package jain.piyush.projectmanagement.activities


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.mikhaellopez.circularimageview.CircularImageView
import jain.piyush.projectmanagement.R
import jain.piyush.projectmanagement.adapters.BoardItemAdapters
import jain.piyush.projectmanagement.firebase.FirestoreClass
import jain.piyush.projectmanagement.models.Board
import jain.piyush.projectmanagement.models.User
import jain.piyush.projectmanagement.utiles.Constants


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var mSharePreferences : SharedPreferences
    private lateinit var tv_No_Such:TextView
    private lateinit var rv_board_list:RecyclerView
    private lateinit var create_board_add:FloatingActionButton
    private lateinit var drawer_layout: DrawerLayout
    private lateinit var nav_view:NavigationView
    private lateinit var tv_username:TextView
    private lateinit var nav_header_image : CircularImageView
    private lateinit var mUserName : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        drawer_layout = findViewById(R.id.drawer_layout)
        nav_view  = findViewById(R.id.nav_view)
        actionBar()

      nav_view.setNavigationItemSelectedListener(this)
        mSharePreferences = this.getSharedPreferences(Constants.PROJECT_PREFERENCES, Context.MODE_PRIVATE)
        FirestoreClass().signInUser(this,true)
        val tokenUpdated = mSharePreferences.getBoolean(Constants.FCM_TOKEN_UPDATE,false)
        if (tokenUpdated){
            showProgressDialog(getString(R.string.please_wait))
            FirestoreClass().signInUser(this,true)
        }else{
            FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        // Now you have the FCM token, you can use it as needed
                        updatingTokenFCM(token)
                    } else {
                        Log.e("FCM Token", "Failed to get token", task.exception)
                    }
                }

        }
        create_board_add = findViewById(R.id.create_board_add)
        create_board_add.setOnClickListener {
            val intent = Intent(this,Create_Board::class.java)
            intent.putExtra(Constants.NAME,mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

    }

    fun updateNavigationUserDetail(user : User,readBoardList : Boolean){
        hideProgressDialog()
        mUserName = user.name
        tv_username = findViewById(R.id.user_name)
        nav_header_image = findViewById(R.id.nav_header_image)
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(nav_header_image)
        tv_username.text = user.name
        if(readBoardList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardList(this)
        }

    }
    private fun actionBar(){
        val toolbar_app_bar : Toolbar = findViewById(R.id.toolbar_app_bar)
        setSupportActionBar(toolbar_app_bar)
        toolbar_app_bar.setNavigationIcon(R.drawable.ic_navigation_icon)
        toolbar_app_bar.setNavigationOnClickListener {
            toggleDrawer()
        }

    }
    private fun toggleDrawer(){

        if (drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else{
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()

        if (drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else{
            doubleBackToExit()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
          FirestoreClass().signInUser(this)
        }else if (resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getBoardList(this)
        }
        
        else{
            Log.e("Canceled","Canceled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile ->{
               startActivityForResult(Intent(this,Profile_View::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                mSharePreferences.edit().clear().apply()
                val intent = Intent(this,LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
    fun populateBoardListToUI(boardList : ArrayList<Board>){
        tv_No_Such = findViewById(R.id.noSuchTask)
        rv_board_list = findViewById(R.id.rv_boards_list)
      hideProgressDialog()
        if (boardList.size > 0){
         rv_board_list.visibility = View.VISIBLE
            tv_No_Such.visibility = View.GONE
            rv_board_list.layoutManager = LinearLayoutManager(this)
            rv_board_list.setHasFixedSize(true)
            val adapter = BoardItemAdapters(this,boardList)
            rv_board_list.adapter = adapter

            adapter.setOnClickListner(object : BoardItemAdapters.OnClickListner{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentId)
                    startActivity(intent)
                }
            })
        }else{
            rv_board_list.visibility = View.GONE
            tv_No_Such.visibility = View.VISIBLE
        }
    }

    fun tokenUpdateSuccess() {
       hideProgressDialog()
        val editor : SharedPreferences.Editor = mSharePreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATE,true)
        editor.apply()
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().signInUser(this,true)
    }
   private fun updatingTokenFCM(token : String){
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().updateUserProfile(this,userHashMap)

    }


    companion object{
        const val MY_PROFILE_REQUEST_CODE :Int = 11
        const val CREATE_BOARD_REQUEST_CODE :Int = 12
    }
}