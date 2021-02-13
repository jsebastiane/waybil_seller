package saymobile.company.saytech.view.customers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_customers.*

import saymobile.company.saytech.R
import saymobile.company.saytech.util.hideKeyboard
import saymobile.company.saytech.viewmodel.customers.CustomersListViewModel


class CustomersFragment : Fragment() {

    private lateinit var viewModel: CustomersListViewModel
    private var customersListAdapter = CustomersListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        close_fragment_customers.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel = ViewModelProviders.of(this).get(CustomersListViewModel::class.java)
        //Just moved this here
        observeCustomerListViewModel()
        viewModel.refresh()

        recycler_customers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = customersListAdapter
        }

        search_customers.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //Probably going to keep it this way
//                viewModel.searchQuery = query
//                viewModel.filterCustomers()
                hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchQuery = newText
                viewModel.filterCustomers()
                return true
            }
        })
    }

    fun observeCustomerListViewModel(){
        viewModel.customersList.observe(viewLifecycleOwner, Observer { customers ->
            customers?.let {
                recycler_customers.visibility = View.VISIBLE
                customersListAdapter.updateCustomersList(customers)
            }
        })
    }

}
