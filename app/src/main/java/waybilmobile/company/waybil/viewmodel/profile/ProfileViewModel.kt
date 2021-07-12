package waybilmobile.company.waybil.viewmodel.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import waybilmobile.company.waybil.model.user.User
import java.util.*

class ProfileViewModel: ViewModel() {

    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _errorUpdating = MutableLiveData<Boolean>()
    val errorUpdating: LiveData<Boolean>
        get() = _errorUpdating

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
        userRef.addSnapshotListener{ value, error ->
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



    fun saveChanges(updates: Map<String, Any>){
        _loading.value = true
        val userRef = mFirebaseDatabase.collection("users").document(userId)

        userRef.update(updates).addOnFailureListener{
            Log.d("updateProfile", "Update unsuccessful")
        }.addOnCompleteListener {task ->
            Log.d("updateProfile", "Update completed")
            _loading.value = false
            _successfulUpdate.value = task.isSuccessful

        }
    }

    private fun uploadNewProductImage(profileImage: ByteArray, imageRef: String, updates: Map<String, Any>) {

//        val userId = mAuth.currentUser!!.uid
//        val productImageRef = FirebaseStorage.getInstance().reference.child("/$userId/inventory/$skuNumber.jpeg")
        val productImageRef =
            FirebaseStorage.getInstance().reference.child("/$userId/profile/$imageRef.jpeg")
        val imageTask = productImageRef.putBytes(profileImage)
        imageTask.addOnSuccessListener {
            Log.d(
                "productImage",
                "Successful upload of product image"
            )
        }
            .addOnFailureListener { Log.d("productImage", "Failed to upload product image") }
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    saveChanges(updates)
                }else{
                    _errorUpdating.value = false
                }
            }
    }

    fun updateProfileDetails(deliveryCost: String, deliveryRange: Int, visibility: Boolean, deliveryOffered: Boolean,
                                     deliveryTime: Int, profileImage: ByteArray?){

        _loading.value = true
        val updates = mutableMapOf<String, Any>()
        val currentDeliveryCost = user.value?.deliveryCost.toString()
        val newImageRef = UUID.randomUUID().toString()


        if(deliveryCost != currentDeliveryCost){
            updates["deliveryCost"] = deliveryCost.toInt()
        }

        if(visibility != user.value?.sellerVisibility){
            updates["sellerVisibility"] = visibility
        }

        if(deliveryRange != user.value?.operatingRadius){
            updates["operatingRadius"] = deliveryRange
        }

        if(deliveryOffered != user.value?.deliveryOffered){
            updates["deliveryOffered"] = deliveryOffered
        }

        if(deliveryTime != user.value?.deliveryTime){
            updates["deliveryTime"] = deliveryTime
        }

        if(profileImage != null){
            updates["profileImageRef"] = newImageRef
            uploadNewProductImage(profileImage, newImageRef, updates)
        }else{
            saveChanges(updates)
        }





    }

    fun removeProfileImage(){
        val userRef = mFirebaseDatabase.collection("users").document(userId)
        userRef.update("profileImageRef", null)

    }


}