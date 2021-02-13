package saymobile.company.saytech.viewmodel.products

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import saymobile.company.saytech.model.products.Product
import java.util.*

class AddProductViewModel : ViewModel() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private val inventoryReference = mFirebaseDatabase.collection("inventory")

    private val _uploading = MutableLiveData<Boolean>()
    val uploading: LiveData<Boolean>
        get() = _uploading

    private val _failedUpload = MutableLiveData<Boolean>()
    val failedUpload: LiveData<Boolean>
        get() = _failedUpload


    private fun uploadProduct(newProduct: Product, imageUri: ByteArray) {
        _uploading.value = true
//        val inventoryRef = inventoryReference.document(mAuth.currentUser!!.uid).collection("inventory")
        val inventoryRef =
            inventoryReference.document(userId).collection("inventory")

        inventoryRef.document(newProduct.skuNumber).set(newProduct)
            .addOnSuccessListener { Log.d("newProduct", "Successfully uploaded new product") }
            .addOnFailureListener { Log.d("newProduct", "Failed to upload new product") }
            .addOnCompleteListener {
                Log.d("productUpload", "Successfully completed new product upload")
                uploadNewProductImage(imageUri, newProduct.skuNumber, newProduct.imageRef)
            }
    }

    private fun uploadNewProductImage(imageUri: ByteArray, skuNumber: String, imageRef: String) {
//        val userId = mAuth.currentUser!!.uid
//        val productImageRef = FirebaseStorage.getInstance().reference.child("/$userId/inventory/$skuNumber.jpeg")
        val productImageRef =
            FirebaseStorage.getInstance().reference.child("/$userId/inventory/$skuNumber/$imageRef.jpeg")
        val imageTask = productImageRef.putBytes(imageUri)
        imageTask.addOnSuccessListener {
            Log.d(
                "productImage",
                "Successful upload of product image"
            )
        }
            .addOnFailureListener { Log.d("productImage", "Failed to upload product image") }
            .addOnCompleteListener { _uploading.value = false }
    }

    fun makeProductObject(
        productName: String,
        unitType: String,
        itemsPerSKU: Int,
        sizePerItem: Double,
        skuNumber: String,
        priceOfSKU: Double,
        brandName: String,
        productType: String,
        category: String,
        itemsAvailable: Int,
        imageUri: ByteArray
    ) {

        val newImageRef = UUID.randomUUID().toString()
        val newProduct = Product(
            productName,
            unitType,
            itemsPerSKU,
            sizePerItem,
            skuNumber,
            priceOfSKU,
            brandName,
            productType,
            category,
            itemsAvailable,
            newImageRef
        )
        uploadProduct(newProduct, imageUri)
    }


}