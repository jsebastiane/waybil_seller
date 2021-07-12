package waybilmobile.company.waybil.view.profile

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_profile.*

import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.model.user.User
import waybilmobile.company.waybil.util.getProgressDrawable
import waybilmobile.company.waybil.util.loadImage

import waybilmobile.company.waybil.viewmodel.profile.ProfileViewModel
import java.io.ByteArrayOutputStream
import java.lang.Exception

private const val IMAGE_PICK_CODE = 1001
private const val PERMISSION_CODE = 1000


class ProfileFragment : Fragment() {

    private var profileImage: ByteArray? = null
    private lateinit var viewModel: ProfileViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        close_fragment_profile.setOnClickListener {
            checkChanges()
        }

        delivery_switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                delivery_range_profileLayout.visibility = View.VISIBLE
                delivery_fee_layout.visibility = View.VISIBLE
                delivery_time_layout.visibility = View.VISIBLE
            } else {
                delivery_range_profileLayout.visibility = View.GONE
                delivery_fee_layout.visibility = View.GONE
                delivery_time_layout.visibility = View.GONE
            }
        }

        manageSettings_profilePage.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToManageAccountFragment()
            Navigation.findNavController(it).navigate(action)
        }

        invite_client_button.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToInviteClient()
            Navigation.findNavController(it).navigate(action)
        }

        //Set round corners for profile picture
        pic_preview_profile.apply {
            clipToOutline = true
        }

        shareApp_button.setOnClickListener {
            shareBuyerApp()
        }

        save_profilePage.setOnClickListener {
            saveChanges()
        }



        test_seekBar.max = 30
        /**
         * Set TextView and seekBar to user data on start
         */

        test_seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                delivery_range_profile.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //do nothing
            }

        })

        observeViewModels()
        viewModel.getUser()


    }

    private fun observeViewModels() {
        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                editImage_profileLayout.visibility = View.GONE
                businessName_user_profile.text = user.userName
                if (user.operatingRadius != null) {
                    delivery_range_profile.text = user.operatingRadius.toString()
                    test_seekBar.progress = user.operatingRadius.toInt()
                }
                delivery_switch.isChecked = user.deliveryOffered

                delivery_fee_profile.setText(user.deliveryCost.toString())
                delivery_time_profile.setText(user.deliveryTime.toString())

                visibility_switch.isChecked = user.sellerVisibility
                location_user_profile.text = user.userAddress
                if(it.profileImageRef != null){
                    getProfileImage(it.id, it.profileImageRef)
                    addImage_suggestion.visibility = View.GONE
                }else{
                    pic_preview_profile.setImageResource(android.R.drawable.ic_menu_gallery)
                    addImage_suggestion.visibility = View.VISIBLE
                }

                initializeEditImage()

//                getProfilePicture(user.id)


            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            loading?.let {
                profile_progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            }
        })

        viewModel.successfulUpdate.observe(viewLifecycleOwner, Observer { successfulUpdate ->
            successfulUpdate?.let {
                if (successfulUpdate) {
                    profileImage = null
                    Toast.makeText(activity, R.string.update_success, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

//    private fun getProfilePicture(userId: String){
//
//        val gsReference = FirebaseStorage.getInstance().reference
//            .child("/$userId/profileImage.jpeg")
//
//        pic_preview_profile.loadImage(gsReference, getProgressDrawable(pic_preview_profile.context))
//    }

    private fun checkChanges() {
        val user = viewModel.user.value
        val builder = AlertDialog.Builder(activity)
        val currentDeliveryFee = user?.deliveryCost.toString()



        builder.setMessage(R.string.profile_unsaved_changes)
            .setNegativeButton(R.string.discard_changes) { dialog, _ ->
                dialog.dismiss()
                findNavController().navigateUp()
            }.setPositiveButton(R.string.close_message) { dialog, _ ->
                dialog.dismiss()
        }

        if (delivery_fee_profile.text.toString() != currentDeliveryFee ||
            delivery_range_profile.text != user?.operatingRadius.toString() ||
            visibility_switch.isChecked != user?.sellerVisibility || delivery_switch.isChecked
            != user.deliveryOffered || user.deliveryTime.toString() != delivery_time_profile.text.toString()
            || profileImage != null
        ) {

            builder.show()
        }else{
            findNavController().navigateUp()
        }
    }

    private fun saveChanges(){
        val user = viewModel.user.value
        val currentDeliveryFee = user?.deliveryCost.toString()


        if (delivery_fee_profile.text.toString() != currentDeliveryFee ||
            delivery_range_profile.text != user?.operatingRadius.toString() ||
            visibility_switch.isChecked != user?.sellerVisibility || delivery_switch.isChecked
            != user.deliveryOffered || user.deliveryTime.toString() != delivery_time_profile.text.toString()
            || profileImage != null
        ){

            viewModel.updateProfileDetails(
                delivery_fee_profile.text.toString(),
                delivery_range_profile.text.toString().toInt(),
                visibility_switch.isChecked,
                delivery_switch.isChecked,
                delivery_time_profile.text.toString().toInt(),
                profileImage
            )

        }else{
            Toast.makeText(activity, R.string.no_changes_were_made, Toast.LENGTH_SHORT).show()

        }


    }


    private fun checkImagePermissions() {

        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) ==
                    PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(activity, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                getImageFromGallery()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                showDialog()
            }

            else -> activity?.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_CODE
                )
            }
        }

    }

    private fun showDialog() {
        activity?.let { theActivity ->
            val builder = AlertDialog.Builder(theActivity)
            builder.apply {
                setMessage(getString(R.string.image_access_denied))
                setTitle(getString(R.string.permission_required))
                setPositiveButton("Ok") { dialog, which ->
                    activity?.let {
                        ActivityCompat.requestPermissions(
                            theActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            PERMISSION_CODE
                        )

                    }
                }
            }

            val dialog = builder.create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                ContextCompat.getColor(
                    theActivity,
                    R.color.colorAccent
                )
            )
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                ContextCompat.getColor(
                    theActivity,
                    R.color.colorAccent
                )
            )
        }

    }


    private fun getImageFromGallery() {
        val getPictureIntent = Intent(Intent.ACTION_PICK)
        getPictureIntent.type = "image/*"
        startActivityForResult(getPictureIntent, IMAGE_PICK_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        fun innerCheck() {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                getImageFromGallery()
            }
        }

        when (requestCode) {
            PERMISSION_CODE -> innerCheck()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            pic_preview_profile.setImageURI(data.data)
            editImage_profileLayout.visibility = View.GONE
            val bmp = (pic_preview_profile.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()
            profileImage = byteArray
        }
}

    private fun getProfileImage(userId: String, imageRef: String){
        val gsReference = FirebaseStorage.getInstance().reference
            .child("/$userId/profile/$imageRef.jpeg")

        pic_preview_profile.loadImage(gsReference, getProgressDrawable(pic_preview_profile.context))

    }

    private fun shareBuyerApp(){
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.waybil_customer_app)
            val message = getString(R.string.share_buyerside_message) + " https://play.google.com/store/apps/details?id=waybilmobile.company.waybilbuyer"
            shareIntent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.choose_one)))
        } catch (e: Exception){
            Log.d("shareAppException", "Failed open share intent")
        }

    }

    private fun initializeEditImage(){
        changeImage_button.setOnClickListener {
            checkImagePermissions()
        }
        removeImage_button.setOnClickListener {
            removeImageQuery()
        }
        pic_preview_profile.setOnClickListener {
            if(editImage_profileLayout.visibility == View.VISIBLE){
                editImage_profileLayout.visibility = View.GONE
            }else{
                editImage_profileLayout.visibility = View.VISIBLE
            }
        }

    }

    private fun removeImageQuery(){
        val builder = AlertDialog.Builder(activity)

        builder.setMessage(R.string.remove_profile_image_query)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton(R.string.yes) { _, _ ->
                viewModel.removeProfileImage()
            }

        builder.show()
    }



}
