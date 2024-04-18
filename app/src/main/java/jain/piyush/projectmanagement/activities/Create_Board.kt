package jain.piyush.projectmanagement.activities

import android.Manifest
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
import jain.piyush.projectmanagement.databinding.ActivityCreateBoardBinding
import jain.piyush.projectmanagement.firebase.FirestoreClass
import jain.piyush.projectmanagement.models.Board
import jain.piyush.projectmanagement.utiles.Constants
import java.io.IOException

class Create_Board : BaseActivity() {

    private var mSelectedImageFile: Uri? = null
    private var cbinding: ActivityCreateBoardBinding? = null
    private var mBoardImageURL: String = ""
    private lateinit var mUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cbinding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(cbinding?.root)
        setSupportActionBar(cbinding?.createBoardToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }

        cbinding?.createBoardToolbar?.setNavigationOnClickListener {
            onBackPressed()
        }

        cbinding?.btnCreate?.setOnClickListener {
            if (mSelectedImageFile != null) {
                uploadBoardImage()
            } else {
                showProgressDialog(getString(R.string.please_wait))
            }
        }

        supportActionBar?.title = getString(R.string.create)

        cbinding?.boardImage?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST
            && data?.data != null
        ) {
            mSelectedImageFile = data.data
            try {
                Glide.with(this)
                    .load(mSelectedImageFile)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(cbinding?.boardImage!!)
            } catch (e: IOException) {
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
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            }
        } else {
            Toast.makeText(this, "Oops! You have denied permission", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createBoard() {
        val assignedUserArrayList: ArrayList<String> = ArrayList()
        assignedUserArrayList.add(getCurrentUserId())

        val board = Board(
            cbinding?.boardName?.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUserArrayList
        )
        FirestoreClass().createBoard(this, board)
    }

    private fun uploadBoardImage() {
        showProgressDialog(getString(R.string.please_wait))
        val sRef: StorageReference =
            FirebaseStorage.getInstance().reference.child(
                "BOARD_IMAGE" +
                        System.currentTimeMillis() + "." +
                        Constants.getFileExtension(this, mSelectedImageFile)
            )
        sRef.putFile(mSelectedImageFile!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    Log.i("Downloadable Image URL", uri.toString())
                    mBoardImageURL = uri.toString()
                    createBoard()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
    }

    fun createBoardSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cbinding = null
    }
}
