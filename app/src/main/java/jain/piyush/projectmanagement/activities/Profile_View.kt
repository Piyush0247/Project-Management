package jain.piyush.projectmanagement.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import jain.piyush.projectmanagement.R
import jain.piyush.projectmanagement.databinding.ActivityProfileViewBinding
import jain.piyush.projectmanagement.firebase.FirestoreClass
import jain.piyush.projectmanagement.models.User
import jain.piyush.projectmanagement.utiles.Constants
import jain.piyush.projectmanagement.utiles.Constants.showImageChooser
import java.io.IOException

class Profile_View : BaseActivity() {

    private lateinit var mUserDetails : User
    private var mProfileImageURI : String = ""
    private var mSelectedImageFile : Uri? = null
    private var pbinding : ActivityProfileViewBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pbinding = ActivityProfileViewBinding.inflate(layoutInflater)
        setContentView(pbinding?.root)
        setSupportActionBar(pbinding?.myProfileToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        pbinding?.myProfileToolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
        supportActionBar?.title = resources.getString(R.string.my_profile)
        FirestoreClass().signInUser(this)
        pbinding?.btnUpdate?.setOnClickListener {
            if (mSelectedImageFile != null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfile()
            }
        }
        pbinding?.myProfileImage?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED){
                showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE)
            }
        }
    }


    @SuppressLint("SuspiciousIndentation")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode ==  Constants.PICK_IMAGE_REQUEST
            && data!!.data != null) {
          mSelectedImageFile = data.data!!
            try {
                Glide
                    .with(this@Profile_View)
                    .load(mSelectedImageFile)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(pbinding?.myProfileImage!!)
            }catch (e:IOException){
                e.printStackTrace()
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode ==  Constants.READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showImageChooser(this)
            }
        }else
        {
            Toast.makeText(this,"Oops! You have denied permission",Toast.LENGTH_SHORT).show()
        }
    }
    fun setUserDataInUI(user : User){
        mUserDetails = user
        Glide
            .with(this@Profile_View)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(pbinding?.myProfileImage!!)
        pbinding?.myProfileName?.setText(user.name)
        pbinding?.myProfileEmail?.setText(user.email)
        if (user.mobile != 0L){
            pbinding?.myProfileMobile?.setText(user.mobile.toString())
        }
    }
    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageFile != null){
            val sRef : StorageReference = FirebaseStorage.getInstance().reference.child("USER_IMAGE"+
                    System.currentTimeMillis() + "."
                    + Constants.getFileExtension(this,mSelectedImageFile))
            sRef.putFile(mSelectedImageFile!!).addOnSuccessListener {
                taskSnapeshot ->
                Log.e("FireBase Image Uri",taskSnapeshot.metadata!!.reference!!.downloadUrl.toString())
                taskSnapeshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->   Log.i("Downloadable Image URL",uri.toString())
                    mProfileImageURI = uri.toString()
                  updateUserProfile()
                }
            }.addOnFailureListener {
                exception ->
                Toast.makeText(this,exception.message,Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
        }
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
   private  fun updateUserProfile(){
        val userHashMap = HashMap<String,Any>()
        if (mProfileImageURI.isNotEmpty() && mProfileImageURI != mUserDetails.image){
           userHashMap[Constants.IMAGE] = mProfileImageURI
        }
        if (pbinding?.myProfileName?.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = pbinding?.myProfileName?.text.toString()
        }
        if (pbinding?.myProfileMobile?.text.toString() != mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = pbinding?.myProfileMobile?.text.toString().toLong()
        }
        FirestoreClass().updateUserProfile(this,userHashMap)
    }

    override fun onDestroy() {
        super.onDestroy()
        pbinding = null
    }
}