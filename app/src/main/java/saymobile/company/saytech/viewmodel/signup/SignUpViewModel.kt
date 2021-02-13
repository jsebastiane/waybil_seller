package saymobile.company.saytech.viewmodel.signup

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.rotationMatrix
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import saymobile.company.saytech.model.user.User

class SignUpViewModel : ViewModel() {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private val userRef = mFirebaseDatabase.collection("users")
    private var _signUpSuccess = MutableLiveData<Boolean>()
    private var _currentUser = MutableLiveData<FirebaseUser>()
    private var _finishedUpload = MutableLiveData<Boolean>()
    val finishedUpload: LiveData<Boolean>
        get() = _finishedUpload
    val currentUser: LiveData<FirebaseUser>
        get() = _currentUser
    val signUpSuccess: LiveData<Boolean>
        get() = _signUpSuccess
    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading


    fun registerUser(email: String, password: String){
        _loading.value = true

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Log.d(TAG, "creatUserWithEmail: Success")
                    _currentUser.value = mAuth.currentUser
                }else{
                    Log.w(TAG, "createUserWithEmail: Failed", task.exception)
                    _signUpSuccess.value = false
                }
            }

    }

    fun createUserDatabase(user: User, profileImage: ByteArray){

        mFirebaseDatabase.runBatch {batch ->
            mAuth.currentUser?.let {
                batch.set(mFirebaseDatabase.collection("users").document(it.uid), user)
                batch.set(mFirebaseDatabase.collection("customers")
                    .document(it.uid), hashMapOf("numberOfCustomers" to 0))
                batch.set(mFirebaseDatabase.collection("transactions")
                    .document(it.uid), hashMapOf("numberOfTransactions" to 0))
                batch.set(mFirebaseDatabase.collection("inventory")
                    .document(it.uid), hashMapOf("numberOfInventoryItems" to 0))
                batch.set(mFirebaseDatabase.collection("orders")
                    .document(it.uid), hashMapOf("numberOfOrders" to 0))
            }

    }.addOnCompleteListener {
            uploadImage(profileImage, user.id)
//            setPushToken(profileImage, user.id)
        }.addOnFailureListener { Log.d("newProfileUpload", "Failed to create profile") }
    }

    private fun uploadImage(profileImage: ByteArray, userId: String){
        val imageRef = FirebaseStorage.getInstance().reference.child("/$userId/profileImage.jpeg")
        //just be sure this is always false at the beginning of the process
        val imageTask = imageRef.putBytes(profileImage)
        imageTask.addOnSuccessListener { _signUpSuccess.value = true }
        imageTask.addOnFailureListener { _signUpSuccess.value = false }
        imageTask.addOnCompleteListener { _finishedUpload.value = true
            _loading.value = false}
    }

    private fun setPushToken(profileImage: ByteArray, user: String){

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            val tokenUpdate = hashMapOf<String, Any>("pushToken" to token)
            mFirebaseDatabase.collection("users").document(user).update(tokenUpdate)
                .addOnCompleteListener {
                    uploadImage(profileImage, user)
                }
                .addOnSuccessListener { Log.d("Upload push token", "Successful") }
                .addOnFailureListener{Log.d("Upload push token", "Failed")}

            // Log and toast

        })

    }

}