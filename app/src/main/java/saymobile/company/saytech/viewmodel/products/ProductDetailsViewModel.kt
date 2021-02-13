package saymobile.company.saytech.viewmodel.products

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_product.*
import saymobile.company.saytech.model.products.Product
import saymobile.company.saytech.util.getProgressDrawable
import saymobile.company.saytech.util.loadImage
import saymobile.company.saytech.view.product.ProductFragment
import java.util.*


class ProductDetailsViewModel : ViewModel() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()

    //user id will be placed instead at production level
    private val inventoryRef = mFirebaseDatabase.collection("inventory")
        .document(userId).collection("inventory")

    private val _focusedProduct = MutableLiveData<Product>()
    val focusedProduct: LiveData<Product>
        get() = _focusedProduct

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _uploadingChanges = MutableLiveData<Boolean>()
    val uploadingChanges: LiveData<Boolean>
        get() = _uploadingChanges

    private val _uploadSuccessful = MutableLiveData<Boolean>()
    val uploadSuccessful: LiveData<Boolean>
        get() = _uploadSuccessful

    private val _uploadingChangesWithImage = MutableLiveData<Boolean>()
    val uploadingImage: LiveData<Boolean>
        get() = _uploadingChangesWithImage


    fun refresh(productId: String) {
        _loading.value = true
        inventoryRef.document(productId).addSnapshotListener { value, error ->
            if (error != null) {
                Log.d("Product Details", "Listen failed")
                return@addSnapshotListener
            }

            if (value != null) {
                val snapshot = value.toObject(Product::class.java)
                _focusedProduct.value = snapshot
                _loading.value = false
            }

        }
    }

    fun updateProductDetails(
        newProductName: String,
        newProductPrice: Double,
        newProductStock: Int,
        newImageUri: Uri?
    ) {

        val newImageRef = UUID.randomUUID().toString()
        var updates = hashMapOf<String, Any>()
        val currentImageRef = focusedProduct.value?.imageRef

        if(newImageUri == null){
            _uploadingChanges.value = true
            updates = hashMapOf<String, Any>(
                "productName" to newProductName,
                "pricePerUnit" to newProductPrice,
                "itemsAvailable" to newProductStock
            )
        }else{
            _uploadingChangesWithImage
            updates = hashMapOf<String, Any>(
                "productName" to newProductName,
                "pricePerUnit" to newProductPrice,
                "itemsAvailable" to newProductStock,
                "imageRef" to newImageRef
            )
        }


        inventoryRef.document(focusedProduct.value!!.skuNumber).update(updates)
            .addOnSuccessListener {
                _uploadSuccessful.value = true
            }.addOnCompleteListener {
                if(newImageUri != null){
                    uploadImage(newImageUri, newImageRef, currentImageRef!!)
                }else{
                    _uploadingChanges.value = false
                }

            }
    }

    //think about whether both uploads can run at the same time
    fun uploadImage(newImageUri: Uri, newImageRef: String, currentImageRef: String) {
        val currentRef = FirebaseStorage.getInstance().reference.child("/$userId/inventory/${focusedProduct.value?.skuNumber}/$currentImageRef.jpeg")
        val deleteTask = currentRef.delete()

        deleteTask.addOnSuccessListener {
            Log.d(
                "productImage",
                "Successful deletion of product image"
            )
        }.addOnFailureListener{
            Log.d(
                "productImage",
                "Failed to delete product image"
            )
        }

        val productImageRef =
            FirebaseStorage.getInstance().reference.child("/$userId/inventory/${focusedProduct.value?.skuNumber}/$newImageRef.jpeg")
        val imageTask = productImageRef.putFile(newImageUri)
        imageTask.addOnSuccessListener {
            Log.d(
                "productImage",
                "Successful upload of product image"
            )
        }
            .addOnFailureListener { Log.d("productImage", "Failed to upload product image") }
            .addOnCompleteListener { _uploadingChangesWithImage.value = false }
    }


}

