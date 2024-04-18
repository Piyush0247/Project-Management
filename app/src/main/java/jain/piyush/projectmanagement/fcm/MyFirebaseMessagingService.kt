package jain.piyush.projectmanagement.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import jain.piyush.projectmanagement.R
import jain.piyush.projectmanagement.activities.MainActivity
import jain.piyush.projectmanagement.activities.SignInActivity
import jain.piyush.projectmanagement.firebase.FirestoreClass
import jain.piyush.projectmanagement.utiles.Constants

class MyFirebaseMessagingService: FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("Messages","From ${message.from}")
        message.data.isNotEmpty().let {
            Log.d("Messages","Message Form Data Payload : ${message.data}")
            val title = message.data[Constants.FCM_KEY_TITLE]!!
            val message = message.data[Constants.FCM_KEY_MESSAGE]!!
            senNotification(title,message)
        }
        message.notification?.let {
            Log.d("Messages","Message Notification Body : ${it.body}")
        }

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("Messages","Message Form Data Payload : $token")
        registrationToServer(token)

    }
    private fun senNotification(title : String, message : String) {
        val intent = if (FirestoreClass().getCurrentUserId().isNotEmpty()) {
            Intent(this, MainActivity::class.java)
        }else{
            Intent(this, SignInActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val channelId = this.resources.getString(R.string.default_notification_name)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)
         val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId,"channel Project Management",NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0,notification.build())
    }
    private fun registrationToServer(token: String){

    }
}