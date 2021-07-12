package waybilmobile.company.waybil.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient

import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_sign_up.*
import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.model.user.Device
import waybilmobile.company.waybil.model.user.Name
import waybilmobile.company.waybil.model.user.User
import waybilmobile.company.waybil.util.getCurrentLocation
import waybilmobile.company.waybil.util.resetTempGeoPoint
import waybilmobile.company.waybil.viewmodel.signup.SignUpViewModel


class SignUpActivity : AppCompatActivity() {

    private lateinit var viewModel: SignUpViewModel
    private var userLocation: GeoPoint? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var pushToken: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if(!task.isSuccessful){
                Log.w("Token", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            pushToken = task.result
        })

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel = ViewModelProviders.of(this).get(SignUpViewModel::class.java)

        close_activity_signup.setOnClickListener {
            finish()
        }

        cancel_signup.setOnClickListener {
            finish()
        }

        save_signup.setOnClickListener { registerClicked() }

        share_location_button.setOnClickListener {
//          Check if location is enabled on device if so we start geopin activity otherwise
            //we open settings to enable locations services
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            ) {
                locationSettingsPrompt()
            } else {
                startActivity(Intent(this, GeopinActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }

//        deliveryRadius_seekBar_signUp.max = 30
//
//        deliveryRadius_seekBar_signUp.setOnSeekBarChangeListener(object :
//            SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                delivery_range_signUp.text = progress.toString()
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                //do nothing
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                //do nothing
//            }
//
//        })

        observeViewModel()


    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun observeViewModel() {
        viewModel.loading.observe(this, Observer { loading ->
            loading?.let {
                if (loading) {
                    progressBar_signup.visibility = View.VISIBLE
                } else {
                    progressBar_signup.visibility = View.GONE
                }
            }
        })
        viewModel.signUpSuccess.observe(this, Observer { signUpSuccess ->
            signUpSuccess?.let {
                if (signUpSuccess) {
                    Toast.makeText(this, R.string.signup_successful, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.signup_failed, Toast.LENGTH_SHORT).show()
                }
            }
        })

        viewModel.finishedUpload.observe(this, Observer { finishedUpload ->
            finishedUpload?.let {
                finish()
            }
        })
    }


    fun registerClicked() {
        val businessName = business_name_signup.text.toString()
        val accountManagerName = account_manager_name.text.toString()
        val accountManagerSurname = account_manager_surname.text.toString()
        val userEmail = email_fillin_signup.text.toString()
        val password = password_fillin_signup.text.toString()
        val phonenumber = phone_fillin_signup.text.toString()
        val businessAddress = address_fillin_signup.text.toString()

        if (businessName.isEmpty()) {
            Toast.makeText(applicationContext, R.string.enter_business_name, Toast.LENGTH_SHORT).show()
            return
        }
        if (accountManagerName.isEmpty()) {
            Toast.makeText(applicationContext, R.string.enter_accntManager_name, Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (accountManagerSurname.isEmpty()) {
            Toast.makeText(applicationContext, R.string.enter_accntManager_name, Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (phonenumber.isEmpty()) {
            Toast.makeText(applicationContext, R.string.enter_phone_number, Toast.LENGTH_SHORT).show()
            return
        }
        if (businessAddress.isEmpty()) {
            Toast.makeText(applicationContext, R.string.enter_business_address, Toast.LENGTH_SHORT).show()
            return
        }
        if (userEmail.isEmpty()) {
            Toast.makeText(applicationContext, R.string.enter_email_address, Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(applicationContext, R.string.enter_password, Toast.LENGTH_SHORT).show()
            return
        }
        if (confirm_password_fillin_signup.text.toString().isEmpty()) {
            Toast.makeText(applicationContext, R.string.enter_password_confirmation, Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (password_fillin_signup.text.toString() != confirm_password_fillin_signup.text.toString()) {
            Toast.makeText(applicationContext, R.string.password_match_warning, Toast.LENGTH_SHORT).show()
            return
        }


        if (userLocation == null) {
            Toast.makeText(
                applicationContext,
                R.string.no_location_warning,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val devices = arrayListOf<Device>(Device(pushToken, true,
        Timestamp.now()))

        /**
         * After registerUser (the function below) if finished creating an account this will be triggered to create
         * a database reference for this user
         */

        viewModel.currentUser.observe(this, Observer { currentUser ->
            currentUser?.let {
                val user = User(
                    currentUser.uid,
                    businessName,
                    businessAddress,
                    phonenumber,
                    userEmail,
                    Name(accountManagerName, accountManagerSurname),
                    Timestamp.now(),
                    userLocation,
                    connectedDevices = devices
                )
                viewModel.createUserDatabase(user)

            }
        })

        viewModel.registerUser(
            email_fillin_signup.text.toString(),
            password_fillin_signup.text.toString()
        )

    }


    private fun locationSettingsPrompt() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.location_permission_query)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton(R.string.yes) { _, _ ->
                //Sends user to device location settings to switch on location services
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)

            }

        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.colorAccent))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.colorAccent))


    }

    override fun onResume() {
        super.onResume()
        userLocation = getCurrentLocation()
        resetTempGeoPoint()
        if(userLocation == null){
            geolocation_status_signup.setBackgroundResource(R.drawable.circle_status_pending)
        }else{
            geolocation_status_signup.setBackgroundResource(R.drawable.circle_status_confirmed)
        }
    }
}
