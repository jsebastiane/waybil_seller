package waybilmobile.company.waybil.viewmodel.orders

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import waybilmobile.company.waybil.model.orders.Order

class OrdersViewModel: ViewModel() {

    private var mAuth = FirebaseAuth.getInstance()
    private var userId: String = ""
    private val mFirebaseDatabase = FirebaseFirestore.getInstance()
//    private val ordersReference = mFirebaseDatabase.collection("orders")
//        .document(userId).collection("orders")
    private val _noOrders = MutableLiveData<Boolean>()
    val noOrders: LiveData<Boolean>
        get() = _noOrders
    private val _listOfOrders = MutableLiveData<List<Order>>()
    val listOfOrders: LiveData<List<Order>>
        get() = _listOfOrders
    private val _ordersLoadError = MutableLiveData<Boolean>()
    val ordersLoadError: LiveData<Boolean>
     get() = _ordersLoadError

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

init {
    val currentUser = mAuth.currentUser
    if(currentUser != null){
        userId = currentUser.uid
    }
}
    //This will download all the data from firebase for orders
    //Somewhere i will add listeners for changes in the database

    /**
     * for completed orders make sure to only get orders with today's date
     */
    fun refresh(status: Int) {
        _loading.value = true
        val ordersReference = mFirebaseDatabase.collection("orders")
            .document(userId).collection("orders")

        ordersReference.whereEqualTo("orderStatus", status)
            .addSnapshotListener { snapshot, error ->
                if(error != null){
                    Log.d("LiveOrderData", "Listen Failed")
                    _ordersLoadError.value = true
                    _loading.value = false
                }

                if(snapshot != null){
                    val snapshotList = ArrayList<Order>()
                    val document = snapshot.documents

                    document.forEach {
                        val order = it.toObject(Order::class.java)
                        if(order != null){
                            snapshotList.add(order)
                        }
                    }
                    _listOfOrders.value = snapshotList
                    _ordersLoadError.value = false
                    _loading.value = false
                }
            }


    }


}