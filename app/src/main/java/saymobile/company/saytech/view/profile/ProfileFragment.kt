package saymobile.company.saytech.view.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.item_product.view.*

import saymobile.company.saytech.R
import saymobile.company.saytech.util.getProgressDrawable
import saymobile.company.saytech.util.loadImage
import saymobile.company.saytech.view.LoginActivity
import saymobile.company.saytech.viewmodel.profile.ProfileViewModel

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {

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
            findNavController().navigateUp()
        }

        manageSettings_profilePage.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToManageAccountFragment()
            Navigation.findNavController(it).navigate(action)
        }

        //Set round corners for profile picture
        pic_preview_profile.apply {
            clipToOutline = true
        }

        save_profilePage.setOnClickListener {
            checkChanges()
        }

        test_seekBar.max = 30
        /**
         * Set TextView and seekBar to user data on start
         */

        test_seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
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

    private fun observeViewModels(){
        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                businessName_user_profile.text = user.businessName
                if (user.operatingRadius != null){
                    delivery_range_profile.text = user.operatingRadius
                    test_seekBar.progress = user.operatingRadius.toInt()
                }
                if(user.deliveryCost != null){
                    delivery_fee_profile.setText(user.deliveryCost)
                }
                visibility_switch.isChecked = user.sellerVisibility
                visibility_text.text = if(visibility_switch.isChecked) "On" else "Off"

                location_user_profile.text = user.userAddress
                getProfilePicture(user.id)


            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            loading?.let {
                profile_progressBar.visibility = if(loading) View.VISIBLE else View.GONE
            }
        })

        viewModel.successfulUpdate.observe(viewLifecycleOwner, Observer { successfulUpdate ->
            successfulUpdate?.let {
                if (successfulUpdate){
                    Toast.makeText(activity, "Update Successful", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getProfilePicture(userId: String){

        val gsReference = FirebaseStorage.getInstance().reference
            .child("/$userId/profileImage.jpeg")

        pic_preview_profile.loadImage(gsReference, getProgressDrawable(pic_preview_profile.context))
    }

    private fun checkChanges(){
        val user = viewModel.user.value
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("Press continue to save changes").setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }.setPositiveButton("Continue") { _, _ ->
            viewModel.saveChanges(delivery_fee_profile.text.toString(),
                delivery_range_profile.text.toString(), visibility_switch.isChecked)
        }
        if(delivery_fee_profile.text.toString() != user?.deliveryCost ||
            delivery_range_profile.text != user.operatingRadius ||
                visibility_switch.isChecked != user.sellerVisibility){

            builder.show()
        }else{
            Toast.makeText(activity, "No changes were made", Toast.LENGTH_SHORT).show()
        }
    }

}
