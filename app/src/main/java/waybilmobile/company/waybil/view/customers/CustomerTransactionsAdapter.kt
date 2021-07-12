package waybilmobile.company.waybil.view.customers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.customer_details_history.view.*
import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.model.orders.Order
import waybilmobile.company.waybil.util.formatCurrency
import java.text.SimpleDateFormat

class CustomerTransactionsAdapter (val recentTransactions: ArrayList<Order>) : RecyclerView.Adapter<CustomerTransactionsAdapter.TransactionViewHolder>() {

    fun updateRecentTransactions(newTransactList: List<Order>){
        recentTransactions.clear()
        recentTransactions.addAll(newTransactList)
        notifyDataSetChanged()
    }

    class TransactionViewHolder(var view: View): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.customer_details_history, parent, false)
        return TransactionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return recentTransactions.size
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val orderDate = recentTransactions[position].orderTimestampDelivered?.toDate()
        orderDate?.let {
            val formattedDate = SimpleDateFormat("dd/MM/yyyy").format(it)
            holder.view.transactionDate_customerDetails.text = formattedDate
        }

        holder.view.transactionAmount_customerDetails.text =
            formatCurrency(recentTransactions[position].orderTotal)
    }
}