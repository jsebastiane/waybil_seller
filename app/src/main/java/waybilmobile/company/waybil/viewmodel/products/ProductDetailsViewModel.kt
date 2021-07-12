package waybilmobile.company.waybil.viewmodel.products

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_product.*
import waybilmobile.company.waybil.model.products.Product
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

    private val _productDeleteComplete = MutableLiveData<Boolean>()
    val productDeleteComplete: LiveData<Boolean>
        get() = _productDeleteComplete





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

    fun saveChanges(updates: Map<String, Any>){
        _loading.value = true
        val productRef = inventoryRef.document(focusedProduct.value!!.skuNumber)

        productRef.update(updates).addOnSuccessListener {
            Log.d("updateProfile", "Update successful")
        }.addOnFailureListener{
            Log.d("updateProfile", "Update unsuccessful")
        }.addOnCompleteListener {task ->
            Log.d("updateProfile", "Update completed")
            _loading.value = false
            _uploadSuccessful.value = task.isSuccessful
            _uploadingChanges.value = false

        }
    }

    fun updateProductDetails(
        newProductName: String,
        newProductPrice: Double,
        newProductStock: Int,
        productImage: ByteArray?
    ) {

        _uploadingChanges.value = true

        val newImageRef = UUID.randomUUID().toString()
        var updates = hashMapOf<String, Any>()
        val currentImageRef = focusedProduct.value?.imageRef

        if(focusedProduct.value?.productName !=  newProductName){
            updates["productName"] = newProductName
        }

        if(focusedProduct.value?.pricePerUnit != newProductPrice){
            updates["pricePerUnit"] = newProductPrice
        }

        if(focusedProduct.value?.itemsAvailable != newProductStock){
            updates["itemsAvailable"] = newProductStock
        }

        if(productImage == null){
            saveChanges(updates)
        }else{
            updates["imageRef"] = newImageRef
            uploadImage(productImage, newImageRef, currentImageRef!!, updates)
        }


    }

    //think about whether both uploads can run at the same time
    fun uploadImage(productImage: ByteArray, newImageRef: String, currentImageRef: String, updates: Map<String, Any>) {
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
        val imageTask = productImageRef.putBytes(productImage)
        imageTask.addOnSuccessListener {
            Log.d(
                "productImage",
                "Successful upload of product image"
            )
        }
            .addOnFailureListener { Log.d("productImage", "Failed to upload product image") }
            .addOnCompleteListener {
                saveChanges(updates)
                }
    }

    private fun deleteProductImage(){
        val productRef = focusedProduct.value?.imageRef
        val imageRef = FirebaseStorage.getInstance().reference.child("/$userId/inventory/${focusedProduct.value?.skuNumber}/$productRef.jpeg")
        val deleteTask = imageRef.delete()

        deleteTask.addOnSuccessListener {
            Log.d("Delete Image:", "Success")
        }
            .addOnCompleteListener { _productDeleteComplete.value = true }
    }

    fun deleteProduct(){
        //focusedProduct shouldn't be null if we are in the product details page
        inventoryRef.document(focusedProduct.value!!.skuNumber).delete().addOnSuccessListener {
            Log.d("Delete Product:", "Product delete success")
        }.addOnCompleteListener {
            deleteProductImage()
        }
    }


}

