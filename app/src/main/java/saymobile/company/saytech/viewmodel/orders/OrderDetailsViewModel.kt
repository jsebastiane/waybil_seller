package saymobile.company.saytech.viewmodel.orders

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import saymobile.company.saytech.model.customers.Customer
import saymobile.company.saytech.model.orders.Order
import saymobile.company.saytech.model.orders.OrderItem
import saymobile.company.saytech.model.transactions.TransactionSummary
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.HashMap

class OrderDetailsViewModel : ViewModel() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private val mFirebaseDatabase = FirebaseFirestore.getInstance()
    private val sellerCustomerRef = mFirebaseDatabase.collection("customers")
        .document(userId).collection("customers")
    private val sellerRef = mFirebaseDatabase.collection("buyersOrders")
    private val orderReference = mFirebaseDatabase.collection("orders")
        .document(userId).collection("orders")
    private val transactionReference = mFirebaseDatabase.collection("transactions")
        .document(userId)

    private val _focusedOrder = MutableLiveData<Order>()
    val focusedOrder: LiveData<Order>
        get() = _focusedOrder

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

            val orderLocation = orderReference.document(it.invoiceId)
            val sellerOrderLocation = sellerRef.document(it.customer.id)
                .collection("orders").document(it.invoiceId)
            val week = SimpleDateFormat("W").format(Timestamp.now().toDate())


            if(it.orderStatus == 1){
                updates = hashMapOf("orderStatus" to FieldValue.increment(1), "orderTimestampConfirmed"
                        to FieldValue.serverTimestamp())
                mFirebaseDatabase.runBatch { batch ->
                    //Update on buyer side then on seller side
                    batch.update(orderLocation, updates)
                    batch.update(sellerOrderLocation, updates)
                }.addOnSuccessListener {
                    Log.d("updateOrderStatus", "Order successfully updated")
                }.addOnFailureListener {
                    Log.d("updateOrderStatus", "Order update failed")
                }.addOnCompleteListener {
                    _updatingOrder.value = false
                }
            }

//            if (it.orderStatus == 1) {
//                updates = hashMapOf<String, Any>("orderStatus" to FieldValue.increment(1))
//                orderLocation.update(updates).addOnSuccessListener {
//                    Log.d("updateOrderStatus", "Order successfully updated")
//                }.addOnFailureListener {
//                    Log.d("updateOrderStatus", "Order update failed")
//                }.addOnCompleteListener {
//                    _updatingOrder.value = false
//                }
//            }
            if (it.orderStatus == 2) {
                val summaryUpdates = hashMapOf<String, Any>(
                    "week$week.ordersCompleted" to FieldValue.increment(1),
                    "week$week.totalWeeklyValue" to FieldValue.increment(focusedOrder.value!!.orderTotal)
                )
                updates = hashMapOf<String, Any>("orderStatus" to FieldValue.increment(1), "orderTimestampDelivered"
                to FieldValue.serverTimestamp())
                //Check that the database reference for this month and year exists
                checkMonthYearRefExists(monthYear, updates, summaryUpdates)

            }


        }

    }

    fun declineOrder() {
        _updatingOrder.value = true

        var updates = hashMapOf<String, Any>()
        focusedOrder.value?.let {
            val sellerOrderLocation = sellerRef.document(it.customer.id)
                .collection("orders").document(it.invoiceId)
            val orderLocation = orderReference.document(it.invoiceId)
            if (it.orderStatus!! < 3) {
                updates = hashMapOf<String, Any>("orderStatus" to 9)

            }

            mFirebaseDatabase.runBatch { batch ->
                //Update on buyer side then on seller side
                batch.update(orderLocation, updates)
                batch.update(sellerOrderLocation, updates)
            }.addOnSuccessListener {
                Log.d("updateOrderStatus", "Order successfully updated")
            }.addOnFailureListener {
                Log.d("updateOrderStatus", "Order update failed")
            }.addOnCompleteListener {
                _updatingOrder.value = false
            }
        }

    }

    //Check if the collection and document exist otherwise create it
    fun checkMonthYearRefExists(monthYearRef: String, updates: HashMap<String, Any>,
                                summaryUpdates: HashMap<String, Any>){

        val transactionMonthYearRef =
            transactionReference.collection(monthYearRef).document("summary")
        var exists: Boolean? = null

        transactionMonthYearRef.get().addOnSuccessListener { documentSnapshot ->
            exists = documentSnapshot.exists()
        }.addOnCompleteListener {
            //When month year reference exists
            if(exists == true){
                updateTransactions(monthYearRef, updates,
                    summaryUpdates)
            }else{
                //If not true we create the corresponding month year reference with a summary doc
                val newTransactionSummary = createSummaryObject()

                transactionReference.collection(monthYearRef).document("summary").set(
                    newTransactionSummary
                ).addOnCompleteListener {
                    updateTransactions(monthYearRef,
                    updates, summaryUpdates)

                }
            }
        }
    }

    fun updateTransactions(monthYearRef: String, updates: HashMap<String, Any>,
                           summaryUpdates: HashMap<String, Any>){


        //Creating a document with the order id in user's completed transactions database
        val transactionRef =
            transactionReference.collection(monthYearRef).document(focusedOrder.value!!.invoiceId)
        //updating summary data in user's transactions database
        val transactionMonthYearRef =
            transactionReference.collection(monthYearRef).document("summary")
        //Referencing the database location of respective orders
        val orderLocation = orderReference.document(focusedOrder.value!!.invoiceId)
        val sellerOrderLocation = sellerRef.document(focusedOrder.value!!.customer.id)
            .collection("orders").document(focusedOrder.value!!.invoiceId)
        //Retrieving info of transaction currently in focus and recently completed



        focusedOrder.value?.let {
            mFirebaseDatabase.runBatch { batch ->
                batch.update(orderLocation, updates)
                batch.update(sellerOrderLocation, updates)
                batch.set(transactionRef, it)
                //updating transaction summary data
                batch.update(transactionMonthYearRef, summaryUpdates)
            }.addOnSuccessListener {
                Log.d("batchUpdateOrder", "Batch order update successful")
            }.addOnFailureListener {
                Log.d("batchUpdateOrder", "Batch order update failed")
            }.addOnCompleteListener {
                /**
                 * move updatingOrder to different function
                 */
                checkCustomerExists()
                _updatingOrder.value = false
            }
        }


    }

    fun createSummaryObject() : TransactionSummary{
        val week1 = hashMapOf<String?, Any?>("ordersReceived" to 0,
        "ordersCompleted" to 0, "totalWeeklyValue" to 0, "week" to "1")
        val week2 = hashMapOf<String?, Any?>("ordersReceived" to 0,
            "ordersCompleted" to 0, "totalWeeklyValue" to 0, "week" to "2")
        val week3 = hashMapOf<String?, Any?>("ordersReceived" to 0,
            "ordersCompleted" to 0, "totalWeeklyValue" to 0, "week" to "3")
        val week4 = hashMapOf<String?, Any?>("ordersReceived" to 0,
            "ordersCompleted" to 0, "totalWeeklyValue" to 0, "week" to "4")
        val week5 = hashMapOf<String?, Any?>("ordersReceived" to 0,
            "ordersCompleted" to 0, "totalWeeklyValue" to 0, "week" to "5")

        val initialTransactionSummary = TransactionSummary(week1, week2, week3, week4, week5)

        return initialTransactionSummary
    }

    /**
     * Clean up the way in which customer data is updated
     */

    fun updateCustomerData(customerExists: Boolean){
        var updates = hashMapOf<String, Any?>()

        if(customerExists){
            focusedOrder.value?.let {
                val customerId = it.customer.id
                val orderSize = it.productsOrderList.size
                val orderValue = it.orderTotal
                updates =
                    hashMapOf<String, Any?>(
                        "lifeTimeOrderQuantity" to FieldValue.increment(orderSize.toLong()),
                        "lifeTimeOrderValue" to FieldValue.increment(orderValue))
                sellerCustomerRef.document(customerId).update(updates)
            }

        }else{
            focusedOrder.value?.let {
                val customer = Customer(it.customer, it.productsOrderList.size, it.orderTotal)
                sellerCustomerRef.document(customer.customer.id).set(customer)
            }
        }


    }

    fun checkCustomerExists(){

        var exists: Boolean? = null
        val clientRef = sellerCustomerRef.document(focusedOrder.value!!.customer.id)
        clientRef.get().addOnSuccessListener { documentSnapshot ->
            exists = documentSnapshot.exists()
        }.addOnCompleteListener {
            updateCustomerData(exists!!)
        }
    }


}