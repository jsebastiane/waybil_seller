package saymobile.company.saytech.view.product

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_add_product_inventory.*
import kotlinx.android.synthetic.main.fragment_inventory.*
import kotlinx.android.synthetic.main.fragment_product.*

import saymobile.company.saytech.R
import saymobile.company.saytech.util.formatCurrency
import saymobile.company.saytech.util.getProgressDrawable
import saymobile.company.saytech.util.loadImage
import saymobile.company.saytech.view.inventory.InventoryFragment
import saymobile.company.saytech.view.inventory.InventoryListAdapter
import saymobile.company.saytech.viewmodel.Inventory.InventoryViewModel
import saymobile.company.saytech.viewmodel.products.ProductDetailsViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private lateinit var currentPhotoPath: String
private const val REQUEST_TAKE_PHOTO = 1
private var productId = ""

class ProductFragment : Fragment() {

    private lateinit var viewModel: ProductDetailsViewModel
    private var newImageUri: Uri? = null
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ProductDetailsViewModel::class.java)

        arguments?.let {
            productId = ProductFragmentArgs.fromBundle(it).productId
        }

        close_fragment_product.setOnClickListener {
            findNavController().navigateUp()
        }

        save_changes_product.setOnClickListener {
            checkChanges()
        }

        cancel_changes_product.setOnClickListener {
            findNavController().navigateUp()
        }

        editImage_product_page.setOnClickListener {
            dispatchTakePictureIntent()
        }



        observeViewModels()
        viewModel.refresh(productId)


    }

    fun observeViewModels(){
        viewModel.focusedProduct.observe(viewLifecycleOwner, Observer { focusedProduct ->
            focusedProduct?.let {
                price_product_page.text = formatCurrency(focusedProduct.pricePerUnit)
                name_product_page.setText(focusedProduct.productName)
                sku_number_product.text = focusedProduct.skuNumber
                detail_product_page.text = focusedProduct.details
                stock_product_page.text = focusedProduct.itemsAvailable.toString()
                editUnit_product_page.setText(focusedProduct.itemsPerUnit.toString())
                editPrice_product_page.setText(String.format("%.2f", focusedProduct.pricePerUnit))
                editStock_product_page.setText(focusedProduct.itemsAvailable.toString())
                getProductImage()
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            loading?.let {
                if (loading){
                    progressBar_productDetails.visibility = View.VISIBLE
                }else{
                    progressBar_productDetails.visibility = View.GONE
                }
            }
        })

        viewModel.uploadingChanges.observe(viewLifecycleOwner, Observer { uploadingChanges->
            uploadingChanges?.let {
                if(uploadingChanges){
                    temp_detailsProgressBar.visibility = View.VISIBLE
                }else{
                    temp_detailsProgressBar.visibility = View.GONE
                }
            }
        })

        viewModel.uploadingImage.observe(viewLifecycleOwner, Observer { uploadingImage->
            uploadingImage?.let {
                if(uploadingImage){
                    temp_imageProgressBar.visibility = View.VISIBLE
                }else{
                    getProductImage()
                    temp_imageProgressBar.visibility = View.GONE

                }
            }
        })
    }

    private fun getProductImage(){
        val imageId = viewModel.focusedProduct.value?.imageRef
        val gsReference = FirebaseStorage.getInstance().reference
            .child("/$userId/inventory/$productId/$imageId.jpeg")

        image_product_page.loadImage(gsReference, getProgressDrawable(image_product_page.context))

    }

    private fun checkChanges(){
        val focusedProduct = viewModel.focusedProduct
        if(name_product_page.text.toString() != focusedProduct.value?.productName ||
            editPrice_product_page.text.toString() != focusedProduct.value?.pricePerUnit.toString() ||
                editStock_product_page.text.toString() != focusedProduct.value?.itemsAvailable.toString() ||
                newImageUri != null){
            saveChanges()
        }else{
            Toast.makeText(activity, "No changes were made", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveChanges(){
        val newProductName = name_product_page.text.toString()
        val newProductPrice = editPrice_product_page.text.toString().toDouble()
        val newProductStock = editStock_product_page.text.toString().toInt()
        viewModel.updateProductDetails(newProductName, newProductPrice, newProductStock, newImageUri)

    }




    //Taking and saving picture onto external storage
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireActivity(),
                        "saymobile.company.saytech.fileprovider",
                        it
                    )
                    newImageUri = photoURI
//                    testingPath = photoFile
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            val imageTaken = BitmapFactory.decodeFile(currentPhotoPath)
            image_product_page.setImageBitmap(imageTaken)
        }

    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name and add unique file name. Will change this to and ID associated
        // with worker or product in the saytech app
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

}
