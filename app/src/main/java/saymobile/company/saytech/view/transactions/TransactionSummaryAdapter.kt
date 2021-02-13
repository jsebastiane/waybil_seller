package saymobile.company.saytech.view.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_transaction_weekly.view.*
import saymobile.company.saytech.R

class TransactionSummaryAdapter (val transactionWeekList: ArrayList<HashMap<String?, Any?>>) : RecyclerView.Adapter<TransactionSummaryAdapter.TransactionSummaryViewHolder>() {

    var adapterCurrentMonth: Int = 0
    var adapterCurrentYear: Int = 0


    fun updateTransactionWeekList(newTransactionWeekList: List<HashMap<String?, Any?>>){
        transactionWeekList.clear()
        transactionWeekList.addAll(newTransactionWeekList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionSummaryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_transaction_weekly, parent, false)
        return TransactionSummaryViewHolder(view)
    }

    override fun getItemCount() = transactionWeekList.size

    override fun onBindViewHolder(holder: TransactionSummaryViewHolder, position: Int) {

        val week = transactionWeekList[position]["week"].toString()
        val totalWeeklyValue = String.format("%.2f", transactionWeekList[position]["totalWeeklyValue"])
        holder.view.date_range_transactions.text = transactionWeekList[position]["weekRange"].toString()
        holder.view.orders_received_transactions.text = transactionWeekList[position]["ordersReceived"].toString()
        holder.view.orders_completed_transactions.text = transactionWeekList[position]["ordersCompleted"].toString()
        holder.view.totalWeeklyValue_viewTransactions.text = totalWeeklyValue
        holder.view.week_number_transactions.text = week
        holder.view.setOnClickListener {
            val action = TransactionsFragmentDirections.actionTransactionsFragmentToTransactionDetails()
            action.weekNumber = week
            action.currentMonth = adapterCurrentMonth
            action.currentYear = adapterCurrentYear
            action.received = transactionWeekList[position]["ordersReceived"].toString()
            action.completed = transactionWeekList[position]["ordersCompleted"].toString()
            action.weekTotal = transactionWeekList[position]["totalWeeklyValue"].toString()
            action.weekRanges = transactionWeekList[position]["weekRange"].toString()
            Navigation.findNavController(it).navigate(action)
        }


    }

    class TransactionSummaryViewHolder(var view: View) : RecyclerView.ViewHolder(view)

}