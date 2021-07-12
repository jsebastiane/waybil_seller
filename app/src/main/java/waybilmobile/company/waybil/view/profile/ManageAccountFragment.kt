package waybilmobile.company.waybil.view.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_manage_account.*
import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.view.LoginActivity
import waybilmobile.company.waybil.viewmodel.profile.ManageAccountViewModel


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
                Toast.makeText(activity, R.string.complete_fields_suggestion, Toast.LENGTH_LONG).show()
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
                    Toast.makeText(activity, R.string.delete_user_failed, Toast.LENGTH_LONG).show()
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