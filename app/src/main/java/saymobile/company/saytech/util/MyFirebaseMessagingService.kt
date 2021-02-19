package saymobile.company.saytech.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage
import saymobile.company.saytech.R
import saymobile.company.saytech.view.DashboardActivity
import saymobile.company.saytech.view.orders.OrdersFragment

private const val CHANNEL_ID = "ORDER_CHANNEL_ID"


class MyFirebaseMessagingService : FirebaseMessagingService(){



    override fun onNewToken(token: String) {
        Log.d("Token", "Token refreshed: $token")
        sendRegistrationTokenToServer(token)

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
            .setSmallIcon(R.drawable.ic_saytech_logo)
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

    private fun sendRegistrationTokenToServer(token: String){
//        val tokenList = arrayListOf<String>(token)
        val mFirebaseDatabase = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val tokenUpdate = hashMapOf<String, Any>("pushToken" to token)
        val userId = currentUser!!.uid
        mFirebaseDatabase.collection("users")
            .document(userId).update(tokenUpdate).addOnSuccessListener {
                Log.d("pushTokenUpdate", "Successful")
            }.addOnFailureListener{
                Log.d("pushTokenUpdate", "Failed")
            }.addOnCompleteListener {
                Log.d("pushTokenUpdate", "Completed")

            }

    }
}