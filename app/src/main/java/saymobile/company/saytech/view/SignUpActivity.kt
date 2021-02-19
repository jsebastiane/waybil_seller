package saymobile.company.saytech.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.fragment_add_product_inventory.*
import kotlinx.android.synthetic.main.fragment_profile.*
import saymobile.company.saytech.R
import saymobile.company.saytech.model.user.User
import saymobile.company.saytech.viewmodel.signup.SignUpViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private lateinit var currentPhotoPath: String
private const val REQUEST_TAKE_PHOTO = 1002
private const val IMAGE_PICK_CODE = 1001
private const val PERMISSION_CODE = 1000

class SignUpActivity : AppCompatActivity() {

    private lateinit var viewModel: SignUpViewModel
    private var imageUri: Uri? = null
    private var productImage: ByteArray? = null
    private var userLocation: GeoPoint? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mLocationRequest = LocationRequest.create()
    private var mLocationCallback = LocationCallback()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel = ViewModelProviders.of(this).get(SignUpViewModel::class.java)

        close_activity_signup.setOnClickListener {
            finish()
        }

        cancel_signup.setOnClickListener {
            finish()
        }

        save_signup.setOnClickListener { registerClicked() }

        capture_image_signup.setOnClickListener { dispatchTakePictureIntent() }
        browse_images_signup.setOnClickListener { checkImagePermissions() }

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

        deliveryRadius_seekBar_signUp.max = 30

        deliveryRadius_seekBar_signUp.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                delivery_range_signUp.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //do nothing
            }

        })

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
                    Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show()
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
        val accountManager = account_manager_signup.text.toString()
        val userEmail = email_fillin_signup.text.toString()
        val password = password_fillin_signup.text.toString()
        val phonenumber = phone_fillin_signup.text.toString()
        val businessAddress = address_fillin_signup.text.toString()
        val deliveryRadius = delivery_range_signUp.text.toString()
        val deliveryCost = deliveryCost_fillin_signup.text.toString()
        if (businessName.isEmpty()) {
            Toast.makeText(applicationContext, "Enter business name", Toast.LENGTH_SHORT).show()
            return
        }
        if (accountManager.isEmpty()) {
            Toast.makeText(applicationContext, "Enter account manager name", Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (phonenumber.isEmpty()) {
            Toast.makeText(applicationContext, "Enter phone number", Toast.LENGTH_SHORT).show()
            return
        }
        if (businessAddress.isEmpty()) {
            Toast.makeText(applicationContext, "Enter business address", Toast.LENGTH_SHORT).show()
            return
        }
        if (userEmail.isEmpty()) {
            Toast.makeText(applicationContext, "Enter email", Toast.LENGTH_SHORT).show()
            return
        }

        if (deliveryRadius.toInt() <= 0) {
            Toast.makeText(
                applicationContext, "Delivery range must be greater than 0",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (deliveryCost.isEmpty()) {
            Toast.makeText(applicationContext, "Enter delivery cost or make 0", Toast.LENGTH_SHORT)
                .show()
        }

        if (password.isEmpty()) {
            Toast.makeText(applicationContext, "Enter password", Toast.LENGTH_SHORT).show()
            return
        }
        if (confirm_password_fillin_signup.text.toString().isEmpty()) {
            Toast.makeText(applicationContext, "Enter password confirmation", Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (password_fillin_signup.text.toString() != confirm_password_fillin_signup.text.toString()) {
            Toast.makeText(applicationContext, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        if (productImage == null) {
            Toast.makeText(applicationContext, "Please upload a picture", Toast.LENGTH_SHORT).show()
            return
        }

        if (userLocation == null) {
            Toast.makeText(
                applicationContext,
                "No podemos continuar sin su ubicacion",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

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
                    accountManager,
                    Timestamp.now(),
                    userLocation,
                    deliveryCost,
                    deliveryRadius
                )
                viewModel.createUserDatabase(user, productImage!!)

            }
        })

        viewModel.registerUser(
            email_fillin_signup.text.toString(),
            password_fillin_signup.text.toString()
        )

    }

    fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(applicationContext.packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        applicationContext,
                        "saymobile.company.saytech.fileprovider",
                        it
                    )
//                    imageUri = photoURI
//                    testingPath = photoFile
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    private fun getImageFromGallery() {
        val getPictureIntent = Intent(Intent.ACTION_PICK)
        getPictureIntent.type = "image/*"
        startActivityForResult(getPictureIntent, IMAGE_PICK_CODE)
    }

    private fun checkImagePermissions() {
        if (ContextCompat.checkSelfPermission(
                this@SignUpActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            getImageFromGallery()
        } else {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            requestPermissions(permissions, PERMISSION_CODE)
        }
    }

    //Results of image capture or image selected from gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            val imageTaken = BitmapFactory.decodeFile(currentPhotoPath)
            pic_preview_signup.setImageBitmap(imageTaken)

            val baos = ByteArrayOutputStream()
            imageTaken.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()
            productImage = byteArray
        }

        //If request was for an image from the gallery
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            pic_preview_signup.setImageURI(data?.data)

            val bmp = (pic_preview_signup.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()
            productImage = byteArray
        }


    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name and add unique file name. Will change this to and ID associated
        // with worker or product in the saytech app
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? =
            applicationContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }




    private fun locationSettingsPrompt() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you give this app permission to use your location?")
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton("Si") { _, _ ->
                //Sends user to device location settings to switch on location services
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)

            }

        builder.show()


    }

    //On permission granted to read user storage user is sent to gallery
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImageFromGallery()
                } else {
                    Toast.makeText(this, "Permission acccess photos denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    }
}
