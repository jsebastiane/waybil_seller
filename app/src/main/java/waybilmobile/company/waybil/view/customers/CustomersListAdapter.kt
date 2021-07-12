package waybilmobile.company.waybil.view.customers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_customer.view.*
import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.model.customers.Customer

class CustomersListAdapter (val customersList: ArrayList<Customer>) : RecyclerView.Adapter<CustomersListAdapter.CustomersListViewHolder>() {

    fun updateCustomersList(newCustomersList: List<Customer>){
        customersList.clear()
        customersList.addAll(newCustomersList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomersListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_customer, parent, false)
        return CustomersListViewHolder(view)
    }

    override fun getItemCount() = customersList.size

    override fun onBindViewHolder(holder: CustomersListViewHolder, position: Int) {
        holder.view.customer_name.text = customersList[position].customer.businessName
        holder.view.setOnClickListener {
            val action = CustomersFragmentDirections.actionCustomersFragmentToCustomerDetailsFragment()
            action.customerId = customersList[position].customer.businessId
            Navigation.findNavController(it).navigate(action)
        }
    }

    class CustomersListViewHolder(var view: View) : RecyclerView.ViewHolder(view)

}