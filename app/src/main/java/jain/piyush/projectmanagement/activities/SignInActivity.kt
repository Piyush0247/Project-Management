package jain.piyush.projectmanagement.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import jain.piyush.projectmanagement.R
import jain.piyush.projectmanagement.databinding.ActivitySignInBinding
import jain.piyush.projectmanagement.models.User

class SignInActivity : BaseActivity() {
    private var signBinding : ActivitySignInBinding? = null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        signBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(signBinding?.root)
        auth = Firebase.auth
        setSupportActionBar(signBinding?.toolbarSignInActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
            signBinding?.toolbarSignInActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
        signBinding?.btnSignIn?.setOnClickListener {
            registerUserSignIn()
        }



    }
    private fun registerUserSignIn() {
        val email: String = signBinding?.etEmailInSignIn?.text.toString().trim { it <= ' ' }
        val password: String = signBinding?.etPasswordInSignIn?.text.toString().trim { it <= ' ' }
        if (validateFormSignIn(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Sign In", "signInWithEmail:success")
                        val user = auth.currentUser
                        user?.let {
                            signInSuccess(User(it.uid, it.email ?: "", ""))
                            finish()
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Sign In Failure", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }.addOnFailureListener { e ->
                    hideProgressDialog()
                    Log.e("Sign In Failure", "Failed to sign in", e)
                    Toast.makeText(
                        baseContext,
                        "Failed to sign in. Please try again.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
        }
    }

    private fun validateFormSignIn( email : String, password : String):Boolean{
        return when{

            TextUtils.isEmpty(email) ->{
                showErrorSnakeBar("Please enter the email")
                false
            }
            TextUtils.isEmpty(password) ->{
                showErrorSnakeBar("Password should not be kept empty")
                false
            }

            else -> {true}
        }
    }

    fun signInSuccess(user: User) {
    hideProgressDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        signBinding = null
    }
}