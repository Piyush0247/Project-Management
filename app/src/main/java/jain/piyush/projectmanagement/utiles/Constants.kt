package jain.piyush.projectmanagement.utiles

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap


object Constants {
  const val USERS : String = "users"

    const val BOARDS : String = "board"
    const val IMAGE : String = "image"
    const val MOBILE : String = "mobile"
    const val NAME : String = "name"
    const val ASSIGNED_TO : String = "assignedTo"
    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST = 2
    const val DOCUMENT_ID : String = "documentId"
    const val TASK_LIST:String = "taskList"
    const val BOARD_DETAILS : String = "board_details"
    const val ID : String = "id"
    const val EMAIL : String = "email"
    const val TASK_LIST_ITEM_POSITION = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION = "card_list_item_position"
    const val BOARD_MEMBER_LIST = "board_member_list"
    const val SELECT = "select"
    const val UNSELECT = "unselect"
    const val PROJECT_PREFERENCES = "project_preferences"
    const val FCM_TOKEN_UPDATE = "fcm_token_update"
    const val FCM_TOKEN = "fcmToken"
   const val FCM_BASE_URL: String = "https://fcm.googleapis.com/fcm/send"
   const val FCM_AUTHORIZATION: String = "authorization"
  const val FCM_KEY:String = "key"
  const val FCM_SERVER_KEY: String = "AAAAc1DJyzg:APA91bGD7QQh8pjUHZvhCw1kilZMBQzj8Tmoj05-ZkDg-hAbRVa3Dd2mJc5WOaIutmLS2AmfuWAmc-7TauZ8FnnIOeG89LJw1aQ7wwTQ6Sm_xFkHPeadirc1uT21Dv2gIUyP6pJC6ner"
  const val FCM_KEY_TITLE:String = "title"
  const val FCM_KEY_MESSAGE: String = "message"
  const val FCM_KEY_DATA:String = "data"
  const val FCM_KEY_TO:String = "to"

   fun showImageChooser(activity: Activity){
    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
  }
   fun getFileExtension(activity: Activity, uri: Uri?):String?{
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
  }

}