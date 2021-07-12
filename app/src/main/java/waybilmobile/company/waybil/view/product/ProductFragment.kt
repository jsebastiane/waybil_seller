package waybilmobile.company.waybil.view.product

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_add_product_inventory.*
import kotlinx.android.synthetic.main.fragment_product.*

import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.util.formatCurrency
import waybilmobile.company.waybil.util.getProgressDrawable
import waybilmobile.company.waybil.util.loadImage
import waybilmobile.company.waybil.viewmodel.products.ProductDetailsViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private lateinit var currentPhotoPath: String
private const val REQUEST_IMAGE_CAPTURE = 1
private const val PERMISSION_CODE = 1000
private const val IMAGE_PICK_CODE = 1001
private var productId = ""

class ProductFragment : Fragment() {

    private lateinit var viewModel: ProductDetailsViewModel
    private var newImageUri: Uri? = null
    private var productImage: ByteArray? = null
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
            if(image_product_page.visibility == View.VISIBLE){
                image_product_page.visibility = View.GONE
                editImage_layout_product.visibility = View.VISIBLE
            }else{
                image_product_page.visibility = View.VISIBLE
                editImage_layout_product.visibility = View.GONE
            }
        }

        image_product_page.apply {
            clipToOutline = true
        }

        delete_button_product.setOnClickListener {
            deleteProductPrompt()
        }

        captureImage_button_product.setOnClickListener {
            dispatchTakePictureIntent()
        }

        uploadImage_button_product.setOnClickListener {
            //Checking permissions to open device gallery
            checkImagePermissions() }



        observeViewModels()
        viewModel.refresh(productId)


    }

    fun observeViewModels(){
        viewModel.focusedProduct.observe(viewLifecycleOwner, Observer { focusedProduct ->
            focusedProduct?.let {
                price_product_page.text = formatCurrency(focusedProduct.pricePerUnit)
                name_product_page.text = focusedProduct.productName
                sku_number_product.text = focusedProduct.skuNumber
                detail_product_page.text = focusedProduct.details
                stock_product_page.text = focusedProduct.itemsAvailable.toString()
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

        viewModel.uploadSuccessful.observe(viewLifecycleOwner, Observer { uploadSuccessful->
            uploadSuccessful?.let {
                if (uploadSuccessful){
                    productImage = null
                    Toast.makeText(activity, R.string.update_success, Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()

                }
            }
        })

        viewModel.productDeleteComplete.observe(viewLifecycleOwner, Observer { productDeleteComplete ->
            productDeleteComplete?.let{
                if(productDeleteComplete){
                    findNavController().navigateUp()
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
        if(editPrice_product_page.text.toString() != focusedProduct.value?.pricePerUnit.toString() ||
                editStock_product_page.text.toString() != focusedProduct.value?.itemsAvailable.toString() ||
                newImageUri != null){
            saveChanges()
        }else{
            Toast.makeText(activity, R.string.no_changes_were_made, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveChanges(){
        val newProductName = name_product_page.text.toString()
        val newProductPrice = editPrice_product_page.text.toString().toDouble()
        val newProductStock = editStock_product_page.text.toString().toInt()
        viewModel.updateProductDetails(newProductName, newProductPrice, newProductStock, productImage)

    }



    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.error_image_capture, Toast.LENGTH_SHORT).show()
        }
    }



    private fun getImageFromGallery() {
        val getPictureIntent = Intent(Intent.ACTION_PICK)
        getPictureIntent.type = "image/*"
        startActivityForResult(getPictureIntent,
            IMAGE_PICK_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && data != null) {
            val bmp = data.extras?.get("data") as Bitmap
            image_product_page.setImageBitmap(bmp)
            image_product_page.visibility = View.VISIBLE
            editImage_layout_product.visibility = View.GONE
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()
            productImage = byteArray
        }

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            image_product_page.setImageURI(data.data)
            image_product_page.visibility = View.VISIBLE
            editImage_layout_product.visibility = View.GONE
            val bmp = (image_product_page.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()
            productImage = byteArray
        }

    }

    private fun checkImagePermissions() {

        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) ==
                    PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(activity, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                getImageFromGallery()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                showDialog()
            }

            else -> activity?.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_CODE
                )
            }
        }

    }

    private fun showDialog() {
        activity?.let { theActivity ->
            val builder = AlertDialog.Builder(theActivity)
            builder.apply {
                setMessage(getString(R.string.image_access_denied))
                setTitle(getString(R.string.permission_required))
                setPositiveButton("Ok") { dialog, which ->
                    activity?.let {
                        ActivityCompat.requestPermissions(
                            theActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            PERMISSION_CODE
                        )

                    }
                }
            }

            val dialog = builder.create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                ContextCompat.getColor(
                    theActivity,
                    R.color.colorAccent
                )
            )
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                ContextCompat.getColor(
                    theActivity,
                    R.color.colorAccent
                )
            )
        }

    }

//    @SuppressLint("SimpleDateFormat")
//    @Throws(IOException::class)
//    private fun createImageFile(): File {
//        // Create an image file name and add unique file name. Will change this to and ID associated
//        // with worker or product in the saytech app
//        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        return File.createTempFile(
//            "JPEG_${timeStamp}_", /* prefix */
//            ".jpg", /* suffix */
//            storageDir /* directory */
//        ).apply {
//            // Save a file: path for use with ACTION_VIEW intents
//            currentPhotoPath = absolutePath
//        }
//    }

    private fun deleteProductPrompt(){
        activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.confirm_delete_query).setNegativeButton(R.string.no){
                    dialog, _ -> dialog.dismiss()
            }.setPositiveButton(R.string.yes){_, _ ->
                viewModel.deleteProduct()
            }
            val dialog = builder.create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                ContextCompat.getColor(
                    it,
                    R.color.colorAccent
                )
            )
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                ContextCompat.getColor(
                    it,
                    R.color.colorAccent
                )
            )

        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        fun innerCheck() {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                getImageFromGallery()
            }
        }

        when (requestCode) {
            PERMISSION_CODE -> innerCheck()
        }

    }





    //Taking and saving picture onto external storage
//    private fun dispatchTakePictureIntent() {
//        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
//                val photoFile: File? = try {
//                    createImageFile()
//                } catch (ex: IOException) {
//                    // Error occurred while creating the File
//                    null
//                }
//                // Continue only if the File was successfully created
//                photoFile?.also {
//                    val photoURI: Uri = FileProvider.getUriForFile(
//                        requireActivity(),
//                        "waybilmobile.company.waybil.fileprovider",
//                        it
//                    )
//                    newImageUri = photoURI
////                    testingPath = photoFile
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
//                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
//                }
//            }
//        }
//    }

}
