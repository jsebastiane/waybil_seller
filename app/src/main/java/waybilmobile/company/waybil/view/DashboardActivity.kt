package waybilmobile.company.waybil.view

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_dashboard.*
import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.util.setProfileIssue
import waybilmobile.company.waybil.view.home.HomeFragmentDirections
import waybilmobile.company.waybil.viewmodel.dashboard.DashboardViewModel

private const val CHANNEL_ID = "ORDER_CHANNEL_ID"


class DashboardActivity : AppCompatActivity() {

    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private lateinit var viewModel: DashboardViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        viewModel = ViewModelProviders.of(this).get(DashboardViewModel::class.java)


        createNotificationChannel()

        profile_bar_dashboard.setOnClickListener {

            Navigation.findNavController(this, R.id.nav_host_fragment).popBackStack(R.id.homeFragment, false)
            val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment()
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(action)
        }

        home_button.setOnClickListener {
            Navigation.findNavController(this, R.id.nav_host_fragment).popBackStack(R.id.homeFragment, false)
        }

        viewModel.getUser()
        observeViewModels()




    }

    private fun observeViewModels(){
        viewModel.user.observe(this, Observer { user ->
            user?.let {
                setProfileIssue(!it.sellerVisibility)
                if(it.sellerVisibility){
                    profileWarning_icon.visibility = View.GONE
                }else{
                    profileWarning_icon.visibility = View.VISIBLE
                }
                user_name_dashboard.text = it.userName
                viewModel.getUserToken()

            }
        })

        viewModel.signedIn.observe(this, Observer{ signedIn ->
            signedIn?.let {
                if(!it){
                    val intent = Intent(this, LoginActivity::class.java)
                    FirebaseAuth.getInstance().signOut()
                    this.finish()
                    startActivity(intent)
                }
            }
        })

    }


    //A notification channel must be created on startup if the device is running API 26 or greater
    //This is for notifications if the app is in the foreground
    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = getString(R.string.channel_name)
            val descriptionsText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
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
// Comment initial testing task