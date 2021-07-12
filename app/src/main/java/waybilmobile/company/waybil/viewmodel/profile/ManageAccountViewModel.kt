package waybilmobile.company.waybil.viewmodel.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ManageAccountViewModel: ViewModel() {

    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var currentUser = FirebaseAuth.getInstance().currentUser

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean>
        get() = _deleteSuccess

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading


    fun deleteUser(email: String, password: String){
        _loading.value = true
        val credentials = EmailAuthProvider.getCredential(email, password)
        currentUser?.reauthenticate(credentials)?.addOnCompleteListener {
            if (it.isSuccessful){
                currentUser?.delete()!!.addOnCompleteListener {task->
                    if(task.isSuccessful){
                        Log.d("delete user", "Successfully deleted user")
                        _deleteSuccess.value = true
                    }else{
                        Log.d("delete user", "Failed to delete user")
                        _deleteSuccess.value = false
                    }
                }
                _loading.value = false
            }else{
                Log.d("re-authentication", "Failed to re-authenticate")
                _deleteSuccess.value = false
                _loading.value = false

            }

        }
    }

}