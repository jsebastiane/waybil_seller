package saymobile.company.saytech.view.inventory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_inventory.*

import saymobile.company.saytech.R
import saymobile.company.saytech.util.hideKeyboard
import saymobile.company.saytech.viewmodel.Inventory.InventoryViewModel


class InventoryFragment : Fragment() {

    private lateinit var viewModel: InventoryViewModel
    //    ArrayList<Product>() test this to see if it accomplishes the same thing
    private var inventoryListAdapter = InventoryListAdapter(arrayListOf())
    private var subCategoryAdapter = SubCategoryAdapter(arrayListOf())
    private var selectedFilterButton: ToggleButton? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        close_fragment_inventory.setOnClickListener {
            findNavController().navigateUp()
        }

        add_product_button.setOnClickListener {
            val action = InventoryFragmentDirections.actionInventoryFragmentToAddProductInventory()
            Navigation.findNavController(it).navigate(action)
        }

        viewModel = ViewModelProviders.of(this).get(InventoryViewModel::class.java)

        product_list_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = inventoryListAdapter
        }

        recycler_sub_categories.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = subCategoryAdapter
        }

        observeInventoryViewModel()

        //Creating live search ability for user inventory
        search_products.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchQuery = query
                viewModel.filterProducts()
                hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchQuery = newText
                viewModel.filterProducts()
                return true
            }
        })

        subCategoryAdapter.selectedCategory.observe(viewLifecycleOwner, Observer { selectedCategory ->
            selectedCategory?.let {
                updateListForSelectedCategory(selectedCategory)

            }
        })

        //Observing for changes in subCategory data and notifying recyclerview for update
        viewModel.subCategories.observe(viewLifecycleOwner, Observer { subcategories ->
            subcategories?.let {
                recycler_sub_categories.visibility = if(subcategories.isEmpty()) View.GONE else View.VISIBLE
                subCategoryAdapter.updateSubCategories(subcategories)
            }
        })

        viewModel.refresh()

        //Setting listeners for filter toggles
        toggle_brand.setOnCheckedAction()
        toggle_category.setOnCheckedAction()
        toggle_type.setOnCheckedAction()






    }

    private fun observeInventoryViewModel(){
        viewModel.filteredInventory.observe(viewLifecycleOwner, Observer { inventory ->
            inventory?.let {
                product_list_recycler.visibility = View.VISIBLE
                inventoryListAdapter.updateProductList(inventory)
            }
        })
    }

    private fun ToggleButton.setOnCheckedAction(){
        this.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                selectedFilterButton = this
                //update viewmodel data with list of all brands
                viewModel.updateSubCategories(this.id)
            }
            else{
                selectedFilterButton = null
                viewModel.clearSubCategories()
            }

            when(this){
                toggle_brand -> viewModel.selectedBrand.value = null
                toggle_type -> viewModel.selectedType.value = null
                toggle_category -> viewModel.selectedCategory.value = null
            }

            viewModel.filterProducts()
        }
    }

    private fun updateListForSelectedCategory(category: String){
        when(selectedFilterButton){
            toggle_brand -> viewModel.selectedBrand.value = category
            toggle_type -> viewModel.selectedType.value = category
            toggle_category -> viewModel.selectedCategory.value = category
        }
        when(selectedFilterButton){
            toggle_brand -> toggle_brand.text = viewModel.selectedBrand.value
            toggle_type -> toggle_type.text = viewModel.selectedType.value
            toggle_category -> toggle_category.text = viewModel.selectedCategory.value
        }

//        viewModel.clearSubCategories()
        viewModel.filterProducts()
    }

}
