package waybilmobile.company.waybil.viewmodel.orders

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import waybilmobile.company.waybil.model.customers.Customer
import waybilmobile.company.waybil.model.orders.Order
import java.text.SimpleDateFormat
import kotlin.collections.HashMap

class OrderDetailsViewModel : ViewModel() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private val mFirebaseDatabase = FirebaseFirestore.getInstance()
    private val sellerCustomerRef = mFirebaseDatabase.collection("customers")
        .document(userId).collection("customers")
//    private val buyerRef = mFirebaseDatabase.collection("buyersOrders")
    private val orderReference = mFirebaseDatabase.collection("orders")
        .document(userId).collection("orders")
    private val transactionReference = mFirebaseDatabase.collection("transactions")
        .document(userId)

    private val _focusedOrder = MutableLiveData<Order>()
    val focusedOrder: LiveData<Order>
        get() = _focusedOrder

    private var functions = FirebaseFunctions.getInstance()

    //    private val _orderStatus = MutableLiveData<Int>()
//    val orderStatus: LiveData<Int>
//        get() = _orderStatus
//    private val _orderItems = MutableLiveData<List<OrderItem>>()
//    val orderItems: LiveData<List<OrderItem>>
//        get() = _orderItems
    private val _loadError = MutableLiveData<Boolean>()
    val loadError: LiveData<Boolean>
        get() = _loadError
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _updatingOrder = MutableLiveData<Boolean>()
    val updatingOrder: LiveData<Boolean>
        get() = _updatingOrder


    /**
     * Will need to access the transactions document data
     * Check to see if first order date is null - if it is then add the date of the current order
     * Set most recent order date to date of current order
     */


    fun retrieveOrderItems(invoiceId: String) {
        _loading.value = true

        orderReference.document(invoiceId).addSnapshotListener { snapshot, error ->

            if (error != null) {
                Log.d("OrderDetailData", "Listen Failed")
                _loadError.value = true
            }

            if (snapshot != null) {
                val document = snapshot.toObject(Order::class.java)
                if (document != null) {
                    _focusedOrder.value = document

                }
            }
        }

        _loading.value = false
    }


    @SuppressLint("SimpleDateFormat")
    fun updateOrderStatus() {
        _updatingOrder.value = false
        var updates: HashMap<String, Any>
        focusedOrder.value?.let {
            val monthYear = SimpleDateFormat("M-yyyy").format(Timestamp.now().toDate())
            println("MONTHREFYEAR -->>> $monthYear")

            val orderLocation = orderReference.document(it.orderId)
//            val sellerOrderLocation = buyerRef.document(it.customer.businessOwnerId)
//                .collection("orders").document(it.invoiceId)


            if(it.orderStatus == 1){
                updates = hashMapOf("orderStatus" to FieldValue.increment(1), "orderTimestampConfirmed"
                        to FieldValue.serverTimestamp())
                mFirebaseDatabase.runBatch { batch ->
                    //Update on buyer side then on seller side
                    batch.update(orderLocation, updates)
//                    batch.update(sellerOrderLocation, updates)
                }.addOnSuccessListener {
                    Log.d("updateOrderStatus", "Order successfully updated")
                }.addOnFailureListener {
                    Log.d("updateOrderStatus", "Order update failed")
                }.addOnCompleteListener {
                    _updatingOrder.value = false
                }
            }

            if (it.orderStatus == 2) {

                updates = hashMapOf<String, Any>("orderStatus" to FieldValue.increment(1), "orderTimestampDelivered"
                to FieldValue.serverTimestamp())
                //Check that the database reference for this month and year exists
                updateTransactions(monthYear, updates)

            }


        }

    }

    fun declineOrder() {
        _updatingOrder.value = true

        var updates = hashMapOf<String, Any>()
        focusedOrder.value?.let {
            val orderLocation = orderReference.document(it.orderId)
            if (it.orderStatus!! < 3) {
                updates = hashMapOf<String, Any>("orderStatus" to 9)

            }

            mFirebaseDatabase.runBatch { batch ->
                //Update on buyer side then on seller side
                batch.update(orderLocation, updates)
//                batch.update(sellerOrderLocation, updates)
            }.addOnSuccessListener {
                Log.d("updateOrderStatus", "Order successfully updated")
            }.addOnFailureListener {
                Log.d("updateOrderStatus", "Order update failed")
            }.addOnCompleteListener {
                _updatingOrder.value = false
            }
        }

    }



    fun updateTransactions(monthYearRef: String, updates: HashMap<String, Any>){


        //Creating a document with the order id in user's completed transactions database
        val transactionRef =
            transactionReference.collection(monthYearRef).document(focusedOrder.value!!.orderId)
        //updating summary data in user's transactions database

        //Referencing the database location of respective orders
        val orderLocation = orderReference.document(focusedOrder.value!!.orderId)

        //Retrieving info of transaction currently in focus and recently completed
        focusedOrder.value!!.orderTimestampDelivered = Timestamp.now()
        focusedOrder.value!!.orderStatus = 3



        focusedOrder.value?.let {
            mFirebaseDatabase.runBatch { batch ->
                batch.update(orderLocation, updates)
                batch.set(transactionRef, it)
            }.addOnSuccessListener {
                Log.d("batchUpdateOrder", "Batch order update successful")
            }.addOnFailureListener {
                Log.d("batchUpdateOrder", "Batch order update failed")
            }.addOnCompleteListener {task ->
                if(task.isSuccessful){
                    checkCustomerExists()
                    _updatingOrder.value = false
                }

            }
        }


    }

    /**
     * Clean up the way in which customer data is updated
     */

    fun updateCustomerData(customerExists: Boolean){


        //If customer exists call cloud function to update customer details
        println(customerExists)
        focusedOrder.value!!.orderTimestampDelivered = Timestamp.now()

        if(customerExists){

            focusedOrder.value?.let {

                val updates = hashMapOf<String, Any>(
                    "lifeTimeOrderQuantity" to FieldValue.increment(1),
                    "lifeTimeOrderValue" to FieldValue.increment(it.orderTotal),
                    "lastOrder" to it
                )

                sellerCustomerRef.document(it.customer.businessId).update(updates).addOnSuccessListener {
                    Log.d("UpdateCustomer: ", "Successful")
                }
                    .addOnFailureListener {
                    Log.d("UpdateCustomer:", "Failed")
                }
            }

        }else{
            focusedOrder.value?.let {

                //Create business if it does not exist
//                it.orderTimestampDelivered = Timestamp.now()
                val customer = Customer(it.customer, 1, it.orderTotal, it)
                sellerCustomerRef.document(customer.customer.businessId).set(customer)
            }
        }


    }

    //Check if customer exists to determine whether to create a new customer or update existing one
    fun checkCustomerExists(){
        var exists: Boolean? = null
        val clientRef = sellerCustomerRef.document(focusedOrder.value!!.customer.businessId)
        clientRef.get().addOnSuccessListener { documentSnapshot ->
            exists = documentSnapshot.exists()
        }.addOnCompleteListener {
            updateCustomerData(exists!!)
        }
    }





}