package saymobile.company.saytech.view.transactions

import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ToggleButton
import androidx.core.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_customer_details.*
import kotlinx.android.synthetic.main.fragment_transactions.*

import saymobile.company.saytech.R
import saymobile.company.saytech.viewmodel.transactions.TransactionsViewModel


class TransactionsFragment : Fragment() {

    private lateinit var viewModel: TransactionsViewModel
    private var transactionSummariesAdapter = TransactionSummaryAdapter(arrayListOf())
    private var transactionYearsAdapter = TransactionYearsAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        close_fragment_transactions.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel = ViewModelProviders.of(this).get(TransactionsViewModel::class.java)
        viewModel.createWeekRanges()
//        viewModel.refresh()

        years_filter_recycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = transactionYearsAdapter
        }

        //Delete later
//        random_button.setOnClickListener {
//            //Change this later
//            viewModel.addFakeOrder()
//        }

        toggle_month.setFilterToggleListener(radio_group_month)
        toggle_year.setFilterToggleListener(years_filter_recycler)



        radio_group_month.setOnCheckedChangeListener { _, checkedId ->
            viewModel.selectedMonth(checkedId)
            toggle_month.text = radio_group_month.findViewById<RadioButton>(checkedId).text
            toggle_month.isChecked = false
            radio_group_month.visibility = View.GONE
        }


        //Checking the radio button corresponding to current month
        val initialMonthCheck = radio_group_month.getChildAt(viewModel._currentMonthSelected.value!! - 1)
        radio_group_month.check(initialMonthCheck.id)

        toggle_year.text = viewModel._currentYearSelected.value.toString()
        println(viewModel._currentYearSelected.value.toString())



        transaction_summaries_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionSummariesAdapter
        }

        observeTransactionViewModel()

    }

    fun observeTransactionViewModel() {
        viewModel.transactionsSummaryList.observe(
            viewLifecycleOwner,
            Observer { transactionSummaries ->
                if (transactionSummaries.isEmpty()){
                    no_data_message.visibility = View.VISIBLE
                    transaction_summaries_recycler.visibility = View.GONE
                }else{
                    transactionSummaries?.let {
                        transaction_summaries_recycler.visibility = View.VISIBLE
                        no_data_message.visibility = View.GONE
                        transactionSummariesAdapter.updateTransactionWeekList(transactionSummaries)
                    }
                }
            })
        viewModel._currentMonthSelected.observe(viewLifecycleOwner, Observer { currentMonth ->
            currentMonth?.let {
                viewModel.createWeekRanges()
                viewModel.refresh()
                transactionSummariesAdapter.adapterCurrentMonth = currentMonth
            }
        })
        viewModel._currentYearSelected.observe(viewLifecycleOwner, Observer { currentYear ->
            currentYear?.let {
                viewModel.createWeekRanges()
                viewModel.refresh()
                transactionSummariesAdapter.adapterCurrentYear = currentYear
            }
        })
        viewModel._yearsActive.observe(viewLifecycleOwner, Observer { yearsActive ->
            yearsActive?.let {
                transactionYearsAdapter.updateTransactionYears(yearsActive as ArrayList<Int>)
            }
        })

        transactionYearsAdapter.selectedYear.observe(viewLifecycleOwner, Observer{ currentYear ->
            currentYear?.let {
                viewModel.selectedYear(currentYear)
                toggle_year.text = currentYear.toString()
                toggle_year.isChecked = false

            }
        })

    }

    private fun ToggleButton.setFilterToggleListener(subCategoryContainer: View) {
        this.setOnFocusChangeListener { _, hasFocus -> this.isChecked = hasFocus }
        this.setOnCheckedChangeListener { _, isChecked ->
            subCategoryContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }






}
