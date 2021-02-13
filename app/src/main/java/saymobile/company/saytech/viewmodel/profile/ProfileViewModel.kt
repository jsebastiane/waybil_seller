package saymobile.company.saytech.viewmodel.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import saymobile.company.saytech.model.user.User

class ProfileViewModel: ViewModel() {

    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _successfulUpdate = MutableLiveData<Boolean>()
    val successfulUpdate: LiveData<Boolean>
        get() = _successfulUpdate

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user



    fun getUser(){
        _loading.value = true
        var currentUserProf: User
        val userRef = mFirebaseDatabase.collection("users").document(userId)
        userRef.addSnapshotListener{value, error ->
            if(error != null){
                Log.d("userData", "ListenFailed")
                return@addSnapshotListener
            }
            if(value != null){
                currentUserProf = value.toObject(User::class.java)!!
                _user.value = currentUserProf
                _loading.value = false
            }
        }

    }

    fun saveChanges(deliveryCost: String, deliveryRange: String, visibility: Boolean){
        _loading.value = true
        val userRef = mFirebaseDatabase.collection("users").document(userId)
        val updates = mutableMapOf<String, Any>()
        if(deliveryCost != user.value?.deliveryCost){
            updates["deliveryCost"] = deliveryCost
        }

        if(visibility != user.value?.sellerVisibility){
            updates["sellerVisibility"] = visibility
        }

        if(deliveryRange != user.value?.operatingRadius){
            updates["operatingRadius"] = deliveryRange
        }



        userRef.update(updates).addOnSuccessListener {
            Log.d("updateProfile", "Update successful")
            _successfulUpdate.value = true
        }.addOnFailureListener{
            Log.d("updateProfile", "Update unsuccessful")
        }.addOnCompleteListener {
            Log.d("updateProfile", "Update completed")
            _loading.value = false
            //In case another update happens must be changed back to true
            _successfulUpdate.value = false
        }
    }
}