package saymobile.company.saytech.view.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_manage_account.*
import saymobile.company.saytech.R
import saymobile.company.saytech.view.LoginActivity
import saymobile.company.saytech.viewmodel.profile.ManageAccountViewModel
import saymobile.company.saytech.viewmodel.profile.ProfileViewModel


class ManageAccountFragment : Fragment() {

    private lateinit var viewModel: ManageAccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ManageAccountViewModel::class.java)

        observeViewModels()

        signOut_settingsPage.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            FirebaseAuth.getInstance().signOut()
            activity?.finish()
            startActivity(intent)
        }

        close_fragment_manageAccount.setOnClickListener {
            findNavController().navigateUp()
        }

        delete_settingsPage.setOnClickListener {
            if(email_edit_settings.text.isEmpty() || password_edit_settings.text.isEmpty()){
                Toast.makeText(activity, "Please fill in fields above", Toast.LENGTH_LONG).show()
            }else{
                viewModel.deleteUser(email_edit_settings.text.toString(),
                    password_edit_settings.text.toString())
            }

        }

    }

    private fun observeViewModels(){
        viewModel.deleteSuccess.observe(viewLifecycleOwner, Observer { deleteSuccess ->
            deleteSuccess?.let {
                if(deleteSuccess){
                    val intent = Intent(activity, LoginActivity::class.java)
                    activity?.finish()
                    startActivity(intent)
                }else{
                    Toast.makeText(activity, "Failed to delete user", Toast.LENGTH_LONG).show()
                }
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            loading?.let {
                progressBar_settingsPage.visibility = if(loading) View.VISIBLE else View.GONE
            }
        })
    }

}