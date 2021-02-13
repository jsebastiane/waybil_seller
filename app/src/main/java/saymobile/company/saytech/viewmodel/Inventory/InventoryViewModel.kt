package saymobile.company.saytech.viewmodel.Inventory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import saymobile.company.saytech.R
import saymobile.company.saytech.model.products.Product
import java.util.*
import kotlin.collections.ArrayList

class InventoryViewModel: ViewModel() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()
    private val inventoryRef = mFirebaseDatabase.collection("inventory")
        .document(userId).collection("inventory")

    private var inventory = mutableListOf<Product>()
    private val _subCategories = MutableLiveData<List<String>>()
    val subCategories: LiveData<List<String>>
        get() = _subCategories

    val selectedBrand = MutableLiveData<String>()
    val selectedType = MutableLiveData<String>()
    val selectedCategory = MutableLiveData<String>()

    private val _filteredInventory = MutableLiveData<List<Product>>()
    val filteredInventory: LiveData<List<Product>>
        get() = _filteredInventory

    private val inventoryLoadError = MutableLiveData<Boolean>()
    var searchQuery: String? = null
    private val loading = MutableLiveData<Boolean>()

    fun refresh() {
        loading.value = true
        inventoryRef.addSnapshotListener { value, error ->
            if(error != null){
                Log.d("LiveDataInventory", "ListenFailed")
                return@addSnapshotListener
            }

            if (value != null){
                val snapShotList = ArrayList<Product>()
                val document = value.documents
                document.forEach {
                    val product = it.toObject(Product::class.java)
                    if(product != null){
                        snapShotList.add(product)
                    }
                }
                inventory = snapShotList
                loading.value = false
                filterProducts()
            }
        }

    }

    fun filterProducts(){
        _filteredInventory.value = inventory.filter {(
            selectedBrand.value?.equals(it.brand.toLowerCase())?: true &&
                    selectedCategory.value?.equals(it.category.toLowerCase())?: true &&
                    selectedType.value?.equals(it.type.toLowerCase())?: true &&
            //Needs to be ?: "" handling nullable object
            it.productName.toLowerCase(Locale.ROOT).contains(searchQuery?.toLowerCase(Locale.ROOT)?: ""))
        }
    }

    fun updateSubCategories(selectedButton: Int){
        val currentSubCategoriesList = arrayListOf<String>()
        _filteredInventory.value?.forEach { product ->
            when(selectedButton){
                R.id.toggle_brand -> if(!currentSubCategoriesList.contains(product.brand.toLowerCase())) currentSubCategoriesList.add(product.brand.toLowerCase())
                R.id.toggle_type -> if(!currentSubCategoriesList.contains(product.type.toLowerCase())) currentSubCategoriesList.add(product.type.toLowerCase())
                R.id.toggle_category -> if(!currentSubCategoriesList.contains(product.category.toLowerCase())) currentSubCategoriesList.add(product.category.toLowerCase())
            }
        }

        _subCategories.value = currentSubCategoriesList


    }

    fun clearSubCategories(){
        _subCategories.value = listOf()
    }
}