package saymobile.company.saytech.viewmodel.transactions

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import saymobile.company.saytech.R
import saymobile.company.saytech.model.customers.Customer
import saymobile.company.saytech.model.orders.Order
import saymobile.company.saytech.model.orders.OrderItem
import saymobile.company.saytech.model.transactions.TransactionSummary
import java.text.SimpleDateFormat
import java.util.*
import java.util.Collections.max
import java.util.Collections.min
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class TransactionsViewModel : ViewModel() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()

    private val transactionsDocRef = mFirebaseDatabase.collection("transactions")
        .document(userId)

    private val initialTransactionDate = mFirebaseDatabase.collection("users")
        .document(userId)

    private val sdf = SimpleDateFormat("MMM-yyyy", Locale.US)
    var _yearsActive = MutableLiveData<List<Int>>()
    var _currentYearSelected = MutableLiveData<Int>()
    var _currentMonthSelected = MutableLiveData<Int>()
    val weekRangeStrings = MutableLiveData<List<String>>()
    val transactionsSummaryList = MutableLiveData<List<HashMap<String?, Any?>>>()
    val loading = MutableLiveData<Boolean>()


    init {
        _currentYearSelected.value = Calendar.getInstance().get(Calendar.YEAR)
        _currentMonthSelected.value = Calendar.getInstance().get(Calendar.MONTH) + 1

        //does not have to be a live
        initialTransactionDate.get().addOnSuccessListener {documentSnapshot ->
            val userInfo = documentSnapshot.toObject(saymobile.company.saytech.model.user.User::class.java)
            val dateJoined = userInfo?.dateJoined
            val yearSdf = SimpleDateFormat("yyyy").format(dateJoined!!.toDate())
            createYearList(yearSdf.toInt())
        }.addOnCompleteListener {
            refresh()
        }
    }




    fun refresh() {
        val monthRef = "${_currentMonthSelected.value}-${_currentYearSelected.value}"
        println("******PRINGE*******")
        println(monthRef)

        transactionsDocRef.collection(monthRef).document("summary")
            .addSnapshotListener { snapshot, error ->
                loading.value = true
                if (error != null) {
                    Log.d("TransactionSummaries", "Listen failed")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val snapshotList = ArrayList<HashMap<String?, Any?>>()
                    val document = snapshot.toObject(TransactionSummary::class.java)
                    if (document != null) {
                        if(document.week1["ordersCompleted"].toString().toInt() > 0 || document.week1
                            ["totalWeeklyValue"].toString().toInt() > 0){
                            snapshotList.add(document.week1)
                        }
                        if(document.week2["ordersCompleted"].toString().toInt() > 0 || document.week2
                                    ["totalWeeklyValue"].toString().toInt() > 0){
                            snapshotList.add(document.week2)
                        }
                        if(document.week3["ordersCompleted"].toString().toInt() > 0 || document.week3
                                    ["totalWeeklyValue"].toString().toInt() > 0){
                            snapshotList.add(document.week3)
                        }
                        if(document.week4["ordersCompleted"].toString().toInt() > 0 || document.week4
                                    ["totalWeeklyValue"].toString().toInt() > 0){
                            snapshotList.add(document.week4)
                        }
                        if(document.week5["ordersCompleted"].toString().toInt() > 0 || document.week5
                                    ["totalWeeklyValue"].toString().toInt() > 0){
                            snapshotList.add(document.week5)
                        }

                    }
                    addWeekRanges(snapshotList)
                }
            }

    }


    //Adding the date range for each week into each week's hashmap
    fun addWeekRanges(snapshotList: ArrayList<HashMap<String?, Any?>>) {
        for (i in 0 until snapshotList.size) {
            val weekNumber = snapshotList[i]["week"].toString().toInt()
            snapshotList[i]["weekRange"] = weekRangeStrings.value?.get(weekNumber - 1)
        }
        transactionsSummaryList.value = snapshotList
    }


    fun createWeekRanges() {
        val weekRanges = ArrayList<String>()
        val sdf = SimpleDateFormat("dd/MM/yy", Locale.US)
        val weekList = ArrayList<ArrayList<Date>>()
        for (i in 1..5) {
            val week = ArrayList<Date>()
            weekList.add(week)
        }
        val cal = Calendar.getInstance()
        //FIX THIS TODAY
        cal.set(_currentYearSelected.value!!, _currentMonthSelected.value!! - 1, 1)
        val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..totalDays) {
            val dateInMonth = cal.time
            val dateWeek = SimpleDateFormat("W").format(dateInMonth)
            cal.add(Calendar.DAY_OF_MONTH, 1)
            when (dateWeek.toInt()) {
                1 -> weekList[0].add(dateInMonth)
                2 -> weekList[1].add(dateInMonth)
                3 -> weekList[2].add(dateInMonth)
                4 -> weekList[3].add(dateInMonth)
                5 -> weekList[4].add(dateInMonth)
            }
        }

        for (i in weekList) {
            val minWeekDate = sdf.format(min(i))
            val maxWeekDate = sdf.format(max(i))
            val range = "${minWeekDate}-${maxWeekDate}"
            weekRanges.add(range)
        }
        weekRangeStrings.value = weekRanges


    }

    fun selectedMonth(monthButtonId: Int){
        _currentMonthSelected.value = when(monthButtonId){
            R.id.radio_jan -> 1
            R.id.radio_feb -> 2
            R.id.radio_march -> 3
            R.id.radio_apr -> 4
            R.id.radio_may -> 5
            R.id.radio_jun -> 6
            R.id.radio_jul -> 7
            R.id.radio_aug -> 8
            R.id.radio_sep -> 9
            R.id.radio_oct -> 10
            R.id.radio_nov -> 11
            else -> 12
        }
    }

    fun selectedYear(year: Int){
        _currentYearSelected.value = year
    }

    fun createYearList(initialYear: Int){
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val yearList = ArrayList<Int>()
        for (i in initialYear..currentYear){
            yearList.add(i)
        }
        _yearsActive.value = yearList
        println(yearList)
        println(_yearsActive.value)

    }

    //temp function
//    fun addFakeOrder(){
//        val ordersRef = mFirebaseDatabase.collection("orders")
//            .document(userId).collection("orders").document()
//
////        val fakeOrderRef = transactionsDocRef.collection("10-2020").document()
//        val individualOrder = OrderItem("SKU00012", "Coca-Cola", "5", "12.50")
//        val listOfItems = arrayListOf<OrderItem>(individualOrder, individualOrder, individualOrder,
//            individualOrder)
//        val currentDate = com.google.firebase.Timestamp.now()
////        val week = SimpleDateFormat("W").format(currentDate.toDate())
//        val order = Order(ordersRef.id,"01234", "54612", "Tienda Don Juan",
//            GeoPoint(14.5580895, -90.4615244), "3ra Calle, Villa Nueva, Guatemala, Guatemala", listOfItems, 1,
//            currentDate, currentDate, currentDate, 1994.20, "3")
//
//        //add listeners
//        ordersRef.set(order)
//
////        fakeOrderRef.set(order)
//    }
//
//    fun addFakeCustomer(){
//        val customerRef = mFirebaseDatabase.collection("customers")
//            .document(userId).collection("customers").document()
//        val newCustomer = Customer("Tienda PooPoo", customerRef.id, GeoPoint(14.615813, -90.515749), 2)
//        customerRef.set(newCustomer)
//    }




}