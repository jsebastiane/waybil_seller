package waybilmobile.company.waybil.viewmodel.transactions

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import waybilmobile.company.waybil.model.orders.Order

class TransactionDetailsViewModel: ViewModel() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private val mFirebaseDatabase = FirebaseFirestore.getInstance()
    private val _currentTransactionsList = MutableLiveData<List<Order>>()
    val currentTransactionsList: LiveData<List<Order>>
        get() = _currentTransactionsList
    private val transactionsDocRef = mFirebaseDatabase.collection("transactions")
        .document(userId)



    fun refresh(month: Int, year: Int, week: String){
        transactionsDocRef.collection("$month-$year").whereEqualTo("week", week)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d("LiveData", "Listen failed")
                    return@addSnapshotListener
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
                    _currentTransactionsList.value = snapshotList
                    }


                }

            }
    }