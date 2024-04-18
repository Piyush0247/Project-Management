package jain.piyush.projectmanagement.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import jain.piyush.projectmanagement.activities.BaseActivity
import jain.piyush.projectmanagement.activities.CardDetailsActivity
import jain.piyush.projectmanagement.activities.Create_Board
import jain.piyush.projectmanagement.activities.MainActivity
import jain.piyush.projectmanagement.activities.Member
import jain.piyush.projectmanagement.activities.Profile_View
import jain.piyush.projectmanagement.activities.SignInActivity
import jain.piyush.projectmanagement.activities.SignUpActivity
import jain.piyush.projectmanagement.activities.TaskListActivity
import jain.piyush.projectmanagement.models.Board
import jain.piyush.projectmanagement.models.User
import jain.piyush.projectmanagement.utiles.Constants

class FirestoreClass {

    private val mFirestore = FirebaseFirestore.getInstance()

    fun registeruser(activity : SignUpActivity,userInfo: User){
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId()).set(userInfo, SetOptions.merge()).addOnSuccessListener {
                activity.userRegisterSuccess()
            }.addOnFailureListener {
                e -> Log.e(activity.javaClass.simpleName,"Error Document written",e)
            }
    }
    fun createBoard(activity : Create_Board,board : Board){
        mFirestore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
               Log.e(activity.javaClass.simpleName,"Board has created")
                Toast.makeText(activity,"Board has created successfully",Toast.LENGTH_SHORT).show()
                activity.createBoardSuccessfully()
            }.addOnFailureListener {
                    exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating board",exception)
            }
    }
    fun getBoardList(activity: MainActivity){
        mFirestore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserId())
            .get()
            .addOnSuccessListener {
                documnet ->
                Log.i(activity.javaClass.simpleName,documnet.documents.toString())
                val boadList : ArrayList<Board> = ArrayList()
                for (i in documnet.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boadList.add(board)
                }
                activity.populateBoardListToUI(boadList)
            }.addOnFailureListener {
                e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating board",e)
            }
    }
    fun getCurrentUserId():String{
        val currentUser = FirebaseAuth.getInstance().currentUser
       var currentUserId = ""
        if (currentUser != null){
            currentUserId = currentUser.uid
        }
        return currentUserId
    }
    fun signInUser(activity: Activity,readBoardList : Boolean = false) {
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId()).get().addOnSuccessListener { document ->
                val loggedIn = document.toObject(User::class.java)
                loggedIn?.let {
                    when (activity) {
                        is SignInActivity -> {
                            activity.signInSuccess(it)
                        }
                        is MainActivity -> {
                            activity.updateNavigationUserDetail(it,readBoardList)
                        }
                        is Profile_View ->{
                            activity.setUserDataInUI(it)
                        }
                        else -> {
                            // Handle unexpected activity type
                            Log.e(activity.javaClass.simpleName, "Unexpected activity type")
                        }
                    }
                } ?: run {
                    // Handle null value returned from document
                    Log.e(activity.javaClass.simpleName, "Document does not exist or does not contain expected data")
                }
            }.addOnFailureListener { e ->
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    else -> {
                        // Handle unexpected activity type
                        Log.e(activity.javaClass.simpleName, "Unexpected activity type")
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error Document written", e)
            }
    }
    fun updateUserProfile(activity: Activity,userhash : HashMap<String,Any>){
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId()).update(userhash)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"profile Updated")
                Toast.makeText(activity,"Profile Updated",Toast.LENGTH_SHORT).show()
                when(activity){
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    is Profile_View -> {
                        activity.profileUpdateSuccess()
                    }
                }

            }.addOnFailureListener {
                e ->
                when(activity){
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is Profile_View -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName,"Error while creating a board",e)
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        // Check if the documentId is valid
        if (documentId.isBlank()) {
            Log.e(activity.javaClass.simpleName, "Invalid documentId provided")
            // Handle the error, e.g., show an error message to the user
            activity.hideProgressDialog()
            return
        }

        mFirestore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val board = documentSnapshot.toObject(Board::class.java)
                    if (board != null) {
                        board.documentId = documentId
                        activity.boardDetails(board)
                    } else {
                        Log.e(activity.javaClass.simpleName, "Failed to convert document to Board object")
                        // Handle the error, e.g., show an error message to the user
                    }
                } else {
                    Log.e(activity.javaClass.simpleName, "Document does not exist for documentId: $documentId")
                    // Handle the error, e.g., show an error message to the user
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error getting document", e)
                // Handle the error, e.g., show an error message to the user
            }
    }
    fun addUpdateTaskList(activity: Activity,board: Board){
     val taskListHashMap = HashMap<String,Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList
        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName,"Task List successfully added")
                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                }else if (activity is CardDetailsActivity){
                    activity.addUpdateCardDetailsSuccess()
                }
            }
            .addOnFailureListener {
                exception ->
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog() }
                if (activity is CardDetailsActivity) {
                    activity.hideProgressDialog() }
                Log.e(activity.javaClass.simpleName,"Error while creating a board",exception)
            }
    }

 fun getAssignedMembersList(activity : Activity,assignedTo : ArrayList<String>){
     mFirestore.collection(Constants.USERS)
         .whereIn(Constants.ID,assignedTo)
         .get()
         .addOnSuccessListener {
             document -> Log.e(activity.javaClass.simpleName,document.documents.toString())
             val userList : ArrayList<User> = ArrayList()
             for (i in document.documents){
                 val user = i.toObject(User::class.java)
                 userList.add(user!!)
             }
             if (activity is Member) {
                 activity.setUpMemberList(userList)
             }else if (activity is TaskListActivity){
                 activity.boardMemberDetailList(userList)
             }

         }
         .addOnFailureListener {
             exception ->
             if (activity is Member) {
                 activity.hideProgressDialog()
             }else if (activity is TaskListActivity){
                 activity.hideProgressDialog()
             }
             Log.e(activity.javaClass.simpleName,"Error while creating Member",exception)
         }
 }
    fun getMembersDetails(activity : Member,email:String){
      mFirestore.collection(Constants.USERS)
          .whereEqualTo(Constants.EMAIL,email)
          .get()
          .addOnSuccessListener {
              document ->
              if (document.documents.size > 0){
                 val user = document.documents[0].toObject(User::class.java)
              activity.memberDetails(user!!)
              }else
              {
                  activity.hideProgressDialog()
                  activity.showErrorSnakeBar("No such member found")
              }
          }
          .addOnFailureListener {e->
              activity.hideProgressDialog()
              Log.e(activity.javaClass.simpleName,"Error while creating member",e)
          }
    }
   fun assignedTOBoard(activity: Member,board: Board,user: User){
       val assignedToHashMap = HashMap<String,Any>()
       assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo
       mFirestore.collection(Constants.BOARDS)
           .document(board.documentId)
           .update(assignedToHashMap)
           .addOnSuccessListener {
               activity.memberAssignedSuccessCall(user)
           }
           .addOnFailureListener {
               activity.hideProgressDialog()
               Log.e(activity.javaClass.simpleName,"Error while creating board")
           }
   }

}