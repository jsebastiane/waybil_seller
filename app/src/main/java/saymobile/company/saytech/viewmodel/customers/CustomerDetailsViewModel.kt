package saymobile.company.saytech.viewmodel.customers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import saymobile.company.saytech.model.customers.Customer

class CustomerDetailsViewModel: ViewModel() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var customerRef = mFirebaseDatabase.collection("customers")
        .document(userId)
        .collection("customers")
    private val _focusedCustomer = MutableLiveData<Customer>()
    val focusedCustomer: LiveData<Customer>
        get() = _focusedCustomer

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _loadingError = MutableLiveData<Boolean>()
    val loadingError: LiveData<Boolean>
        get() = _loadingError

    fun refresh(customerId: String){
        customerRef.document(customerId).addSnapshotListener { snapshot, error ->
            _loading.value = true
            if(error != null){
                Log.d("CustomerData", "Customer data listen failed")
                _loadingError.value = true
                return@addSnapshotListener
            }

            if(snapshot != null){
                val customer = snapshot.toObject(Customer::class.java)
                _focusedCustomer.value = customer
                _loadingError.value = false
                _loading.value = false
            }
        }
    }
    }



