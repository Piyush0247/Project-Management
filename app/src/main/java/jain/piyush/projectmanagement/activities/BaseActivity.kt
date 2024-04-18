package jain.piyush.projectmanagement.activities

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import jain.piyush.projectmanagement.R

open class BaseActivity : AppCompatActivity() {
    private var doubleBackPressed = false
    private lateinit var mProgrssDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_base)
    }
    fun showProgressDialog(text:String){
        mProgrssDialog = Dialog(this)
        mProgrssDialog.setContentView(R.layout.dialog_progress)
        mProgrssDialog.findViewById<TextView>(R.id.tv_progress_text).text = text
        mProgrssDialog.show()
    }
    fun hideProgressDialog(){
        mProgrssDialog.dismiss()
    }
    fun getCurrentUserId():String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
    fun doubleBackToExit(){
        if (doubleBackPressed){
            super.onBackPressed()
            return
        }
        this.doubleBackPressed = true
        Toast.makeText(this,resources.getString(R.string.please_click_back_to_exit),Toast.LENGTH_SHORT).show()
        Handler().postDelayed({doubleBackPressed = false},2000)
    }
    fun showErrorSnakeBar(message : String){
        val snakeBar = Snackbar.make(findViewById(android.R.id.content),message,Snackbar.ANIMATION_MODE_SLIDE)
        val snackbar = snakeBar.view
        snackbar.setBackgroundColor(ContextCompat.getColor(this,R.color.red))
        snakeBar.show()
    }
}