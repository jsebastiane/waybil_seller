package waybilmobile.company.waybil.util

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.view.DashboardActivity

private const val CHANNEL_ID = "ORDER_CHANNEL_ID"


class MyFirebaseMessagingService : FirebaseMessagingService(){



    override fun onNewToken(token: String) {
        Log.d("Token", "Token refreshed: $token")
//        sendRegistrationTokenToServer(token)

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        //Notification id should be checked on a per notification basis
        //Could filter from list of key value pairs
        //          Order: 1, Saytech: 2
        //Each notification should have some form of identifying whether it belongs to ->
        //  Orders, Messages directly from SayTech, etc.
        val notificationId = 1

        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(DashboardActivity::class.java)
            .setGraph(R.navigation.say_navigation)
            .setDestination(R.id.ordersFragment)
            .createPendingIntent()


        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_waybil)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
//            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)){
            notify(notificationId, builder.build())
        }

    }

//    private fun sendRegistrationTokenToServer(token: String){
////        val tokenList = arrayListOf<String>(token)
//        val mFirebaseDatabase = FirebaseFirestore.getInstance()
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        val tokenUpdate = hashMapOf<String, Any>("pushToken" to token)
//        val userId = currentUser!!.uid
//        mFirebaseDatabase.collection("users")
//            .document(userId).update(tokenUpdate).addOnSuccessListener {
//                Log.d("pushTokenUpdate", "Successful")
//            }.addOnFailureListener{
//                Log.d("pushTokenUpdate", "Failed")
//            }.addOnCompleteListener {
//                Log.d("pushTokenUpdate", "Completed")
//
//            }
//
//    }
}