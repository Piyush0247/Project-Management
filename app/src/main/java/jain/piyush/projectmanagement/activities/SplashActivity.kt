package jain.piyush.projectmanagement.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import jain.piyush.projectmanagement.R
import jain.piyush.projectmanagement.databinding.ActivitySplashScreenBinding
import jain.piyush.projectmanagement.firebase.FirestoreClass

class SplashActivity : BaseActivity() {
    private var Sbinding : ActivitySplashScreenBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Sbinding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(Sbinding?.root)
       window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val typeFace : Typeface = Typeface.createFromAsset(assets,"carbon bl.ttf")
        Sbinding?.tvAppName?.typeface = typeFace
        Sbinding?.madeByBestDeveloper?.typeface = typeFace
       Handler().postDelayed({
           val currentUserId = FirestoreClass().getCurrentUserId()
           if (currentUserId.isNotEmpty()){
               startActivity(Intent(this,MainActivity::class.java))
           }else {
               startActivity(Intent(this, LoginActivity::class.java))
           }
           finish()
       },2500)

    }

    override fun onDestroy() {
        super.onDestroy()
        Sbinding = null
    }
}