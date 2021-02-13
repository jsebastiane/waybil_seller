package saymobile.company.saytech.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseMessagingService : FirebaseMessagingService(){

    override fun onNewToken(token: String) {
        Log.d("Token", "Token refreshed: $token")
        sendRegistrationTokenToServer(token)

    }

    private fun sendRegistrationTokenToServer(token: String){
        val tokenList = arrayListOf<String>(token)
        val mFirebaseDatabase = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val tokenUpdate = hashMapOf<String, Any>("pushToken" to tokenList)
        val userId = currentUser!!.uid
        mFirebaseDatabase.collection("users")
            .document(userId).update(tokenUpdate).addOnSuccessListener {
                Log.d("pushTokenUpdate", "Successful")
            }.addOnFailureListener{
                Log.d("pushTokenUpdate", "Failure")
            }.addOnCompleteListener {
                Log.d("pushTokenUpdate", "Completed")

            }

    }
}