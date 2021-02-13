package saymobile.company.saytech.view.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_subcategory.view.*
import saymobile.company.saytech.R

class SubCategoryAdapter (private val subCategoryList: ArrayList<String>) : RecyclerView.Adapter<SubCategoryAdapter.SubCategoryListViewHolder>(){

    val selectedCategory = MutableLiveData<String>()

    fun updateSubCategories(newSubCategories : List<String>){
        subCategoryList.clear()
        subCategoryList.addAll(newSubCategories)
        notifyDataSetChanged()
    }

    class SubCategoryListViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoryListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_subcategory, parent, false)
        return SubCategoryListViewHolder(view)
    }

    override fun getItemCount() = subCategoryList.size

    override fun onBindViewHolder(holder: SubCategoryListViewHolder, position: Int) {
        val item = subCategoryList[position]
        holder.view.subCategory_label.text = item
        holder.view.setOnClickListener { selectedCategory.value = item }

    }
}

