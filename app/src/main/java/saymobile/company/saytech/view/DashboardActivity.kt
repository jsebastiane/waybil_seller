package saymobile.company.saytech.view

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.fragment_product.*
import saymobile.company.saytech.R
import saymobile.company.saytech.model.user.User
import saymobile.company.saytech.util.getProgressDrawable
import saymobile.company.saytech.util.loadImage
import saymobile.company.saytech.view.home.HomeFragment
import saymobile.company.saytech.view.home.HomeFragmentDirections

private const val CHANNEL_ID = "ORDER_CHANNEL_ID"


class DashboardActivity : AppCompatActivity() {

    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        createNotificationChannel()
        Firebase.messaging.isAutoInitEnabled = true

        getProfilePicture()

        profile_bar_dashboard.setOnClickListener {

            Navigation.findNavController(this, R.id.nav_host_fragment).popBackStack(R.id.homeFragment, false)
            val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment()
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(action)
        }

        home_button.setOnClickListener {
            Navigation.findNavController(this, R.id.nav_host_fragment).popBackStack(R.id.homeFragment, false)
        }

        getUser()





    }

    fun getProfilePicture(){
        val gsReference = FirebaseStorage.getInstance().reference
            .child("/$userId/profileImage.jpeg")

        profile_imageView.loadImage(gsReference, getProgressDrawable(profile_imageView.context))
    }

    private fun getUser(){
        var currentUserProf: User
        val userRef = mFirebaseDatabase.collection("users").document(userId)
        userRef.addSnapshotListener{value, error ->
            if(error != null){
                Log.d("userData", "ListenFailed")
                return@addSnapshotListener
            }
            if(value != null){
                currentUserProf = value.toObject(User::class.java)!!
                if(currentUserProf.businessName.isNotEmpty()){
                    user_name_dashboard.text = currentUserProf.businessName
                }
            }
        }

    }

    //A notification channel must be created on startup if the device is running API 26 or greater
    //This is for notifications if the app is in the foreground
    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = getString(R.string.channel_name)
            val descriptionsText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionsText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d("Notification Channel", "Notification Channel Created")
        }

    }


}
