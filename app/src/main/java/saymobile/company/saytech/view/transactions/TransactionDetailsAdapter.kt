package saymobile.company.saytech.view.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_transaction_details.view.*
import saymobile.company.saytech.R
import saymobile.company.saytech.model.orders.Order

class TransactionDetailsAdapter (val transactionsDetailList: ArrayList<Order>) :
    RecyclerView.Adapter<TransactionDetailsAdapter.TransactionsDetailsViewHolder>()
{

    fun updateList(newTransactionsDetailList: List<Order>){
        transactionsDetailList.clear()
        transactionsDetailList.addAll(newTransactionsDetailList)
        notifyDataSetChanged()

    }

    class TransactionsDetailsViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionsDetailsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_transaction_details, parent, false)
        return TransactionsDetailsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return transactionsDetailList.size
    }

    override fun onBindViewHolder(holder: TransactionsDetailsViewHolder, position: Int) {
        val transactionDetItem = transactionsDetailList[position]
        holder.view.customerName_transaction.text = transactionDetItem.customer.businessName
        holder.view.numberOfItems_transaction.text = transactionDetItem.productsOrderList?.size.toString()
        holder.view.totalValue_transactions.text = transactionDetItem.orderTotal.toString()
        holder.view.invoiceNumber_transactions.text = transactionDetItem.invoiceId

    }
}