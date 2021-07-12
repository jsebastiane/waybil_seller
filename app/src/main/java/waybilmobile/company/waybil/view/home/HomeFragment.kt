package waybilmobile.company.waybil.view.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_home.*

import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.util.getProfileIssue

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

        val visibilityIssue = getProfileIssue()
        if(visibilityIssue){
            visibilityWarning_message_home.visibility = View.VISIBLE
        }else{
            visibilityWarning_message_home.visibility = View.GONE
        }


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
