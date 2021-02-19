package saymobile.company.saytech.view.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.fragment_home.*

import saymobile.company.saytech.R
import saymobile.company.saytech.view.LoginActivity

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        orders_button_home.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToOrdersFragment()
            Navigation.findNavController(it).navigate(action)
        }

        customers_button_home.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToCustomersFragment()
            Navigation.findNavController(it).navigate(action)
        }

        transactions_button_home.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToTransactionsFragment()
            Navigation.findNavController(it).navigate(action)
        }

        inventory_button_home.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToInventoryFragment()
            Navigation.findNavController(it).navigate(action)
        }


    }


}
