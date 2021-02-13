package saymobile.company.saytech.view.transactions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_transaction_details.*

import saymobile.company.saytech.R
import saymobile.company.saytech.viewmodel.transactions.TransactionDetailsViewModel


class TransactionDetails : Fragment() {

    private lateinit var viewModel: TransactionDetailsViewModel
    private var transactionDetailsAdapter = TransactionDetailsAdapter(arrayListOf())
    private var month: Int = 0
    private var year: Int = 0
    private var week: String = ""
    private var received: String = ""
    private var completed: String = ""
    private var weekTotal: String = ""
    private var weekRanges: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(TransactionDetailsViewModel::class.java)

        close_fragment_transactionDetails.setOnClickListener {
            findNavController().navigateUp()
        }

        arguments?.let {
            month = TransactionDetailsArgs.fromBundle(it).currentMonth
            year = TransactionDetailsArgs.fromBundle(it).currentYear
            week = TransactionDetailsArgs.fromBundle(it).weekNumber
            received = TransactionDetailsArgs.fromBundle(it).received
            completed = TransactionDetailsArgs.fromBundle(it).completed
            weekTotal = TransactionDetailsArgs.fromBundle(it).weekTotal
            weekRanges = TransactionDetailsArgs.fromBundle(it).weekRanges
        }

        orders_completed_transactionsDets.text = completed
        orders_received_transactionsDets.text = received
        date_range_transactionsDets.text = weekRanges
        week_number_transactionsDets.text = week
        total_order_viewTransactionsDets.text = weekTotal

        recyclerTransactionDets.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionDetailsAdapter
        }

        observeViewModels()
        viewModel.refresh(month, year, week)
    }

    fun observeViewModels(){
        viewModel.currentTransactionsList.observe(viewLifecycleOwner, Observer { currentTransactionsList ->
            currentTransactionsList?.let {
                transactionDetailsAdapter.updateList(currentTransactionsList)
            }
        })
    }

}
