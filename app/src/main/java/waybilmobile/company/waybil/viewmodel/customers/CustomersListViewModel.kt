package waybilmobile.company.waybil.viewmodel.customers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import waybilmobile.company.waybil.model.customers.Customer
import java.util.*
import kotlin.collections.ArrayList

class CustomersListViewModel: ViewModel() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var customerRef = mFirebaseDatabase.collection("customers")
        .document(userId)
        .collection("customers")
    private val _customersList = MutableLiveData<List<Customer>>()
    val customersList: LiveData<List<Customer>>
        get() = _customersList
    
    private val _customersListLoadError = MutableLiveData<Boolean>()
    val customersListLoadError: LiveData<Boolean>
        get() = _customersListLoadError
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    var filteredList = mutableListOf<Customer>()
    var searchQuery: String? = null

    fun refresh(){
        
        customerRef.addSnapshotListener { snapshot, error ->
            _loading.value = true
            if(error != null){
                Log.d("CustomerData", "Customer data listen failed")
                _customersListLoadError.value = true
                return@addSnapshotListener
            }

            if(snapshot != null){
                val snapshotList = ArrayList<Customer>()
                val values = snapshot.documents
                values.forEach {
                    val customer = it.toObject(Customer::class.java)
                    if(customer != null){
                        snapshotList.add(customer)
                    }
                }
                filteredList = snapshotList
                _customersListLoadError.value = false
                filterCustomers()
                _loading.value = false
            }
        }
    }

    fun filterCustomers(){
        _customersList.value = filteredList.filter {
            it.customer.businessName.toLowerCase(Locale.ROOT).contains(searchQuery?.toLowerCase()?: "")
        }
    }
}