package jain.piyush.projectmanagement.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import jain.piyush.projectmanagement.R
import jain.piyush.projectmanagement.databinding.ActivitySignUpBinding
import jain.piyush.projectmanagement.firebase.FirestoreClass
import jain.piyush.projectmanagement.models.User

class SignUpActivity : BaseActivity() {
    private var signUpBinding : ActivitySignUpBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        signUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(signUpBinding?.root)
        setSupportActionBar(signUpBinding?.toolbarSignUpActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        signUpBinding?.toolbarSignUpActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
        signUpBinding?.btnSignUp?.setOnClickListener {
            registerUser()
        }
    }
    fun userRegisterSuccess(){
        Toast.makeText(
            this,
            "you have successfully registered ",
            Toast.LENGTH_SHORT
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
    private fun registerUser() {
        val name: String = signUpBinding?.etName?.text.toString().trim { it <= ' ' }
        val email: String = signUpBinding?.etEmail?.text.toString().trim { it <= ' ' }
        val password: String = signUpBinding?.etPassword?.text.toString().trim { it <= ' ' }
        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser? = task.result?.user
                        firebaseUser?.let {
                            val registerEmail = it.email ?: ""
                            val user = User(it.uid, name, registerEmail)
                            FirestoreClass().registeruser(this, user)
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    hideProgressDialog()
                    Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun validateForm(name : String, email : String, password : String):Boolean{
        return when{
            TextUtils.isEmpty(name) ->{
                showErrorSnakeBar("Please enter the name")
                false
            }
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

    override fun onDestroy() {
        super.onDestroy()
        signUpBinding = null
    }
}