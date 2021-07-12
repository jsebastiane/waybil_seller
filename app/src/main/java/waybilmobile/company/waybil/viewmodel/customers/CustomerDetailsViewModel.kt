package waybilmobile.company.waybil.viewmodel.customers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import waybilmobile.company.waybil.model.customers.Customer
import waybilmobile.company.waybil.model.orders.Order
import java.util.*

class CustomerDetailsViewModel : ViewModel() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var customerRef = mFirebaseDatabase.collection("customers")
        .document(userId)
        .collection("customers")
    private var transactionsRef = mFirebaseDatabase.collection("transactions")
        .document(userId)
    private val _focusedCustomer = MutableLiveData<Customer>()
    val focusedCustomer: LiveData<Customer>
        get() = _focusedCustomer

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _recentTransactions = MutableLiveData<List<Order>>()
    val recentTransactions: LiveData<List<Order>>
        get() = _recentTransactions

    private val _loadingError = MutableLiveData<Boolean>()
    val loadingError: LiveData<Boolean>
        get() = _loadingError

    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    fun refresh(customerId: String) {
        customerRef.document(customerId).addSnapshotListener { snapshot, error ->
            _loading.value = true
            if (error != null) {
                Log.d("CustomerData", "Customer data listen failed")
                _loadingError.value = true
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val customer = snapshot.toObject(Customer::class.java)
                _focusedCustomer.value = customer
            }
        }

        getRecentTransactions(customerId)
    }

    //Retrieving recent transactions for customer summary page - transactions from
    fun getRecentTransactions(customerId: String) {

        val transactions = ArrayList<Order>()
        val currentDateTransactionRef = transactionsRef.collection("$currentMonth-$currentYear")

        currentDateTransactionRef.get().addOnSuccessListener { documents ->
            for (document in documents) {
                if (document != null) {
                    val transaction = document.toObject(Order::class.java)
                    if (transaction.customer.businessId == customerId) {
                        transactions.add(transaction)
                        println(transaction.orderTimestampDelivered?.toDate())
                    }
                }
            }
            if(transactions.isNotEmpty()){
                _recentTransactions.value = transactions.sortedBy { it.orderTimestampDelivered }
            }

        }.addOnCompleteListener {
            _loading.value = false
            Log.d("customerTransactions", "Received: ${transactions.size}")
        }.addOnFailureListener {
            _loadingError.value = true
            Log.d("customerTransactions", "Failed to retrieve customer transactions")
        }


    }

}



