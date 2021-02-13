package saymobile.company.saytech.view.inventory

import android.Manifest
import android.app.Activity
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.fragment_add_product_inventory.*
import kotlinx.android.synthetic.main.fragment_add_product_inventory.pic_preview

import saymobile.company.saytech.R
import saymobile.company.saytech.viewmodel.products.AddProductViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private lateinit var currentPhotoPath: String
private const val IMAGE_PICK_CODE = 1001
private const val REQUEST_IMAGE_CAPTURE = 1002
private const val PERMISSION_CODE = 1000

class AddProductInventory : Fragment() {

    private lateinit var viewModel: AddProductViewModel
    private var productImage: ByteArray? = null
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_product_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(AddProductViewModel::class.java)

        close_fragment_addProduct.setOnClickListener {
            findNavController().navigateUp()
        }

        cancel_newProduct.setOnClickListener {
            findNavController().navigateUp()
        }

        save_newProduct.setOnClickListener {
            submitNewProductClicked()
        }

        capture_image.setOnClickListener {
            dispatchTakePictureIntent()
        }

        browse_images.setOnClickListener {
            checkImagePermissions()
        }

        observeViewModels()
    }

    private fun observeViewModels() {

        viewModel.uploading.observe(viewLifecycleOwner, androidx.lifecycle.Observer { uploading ->
            uploading?.let {
                if (uploading) {
                    progressBar_add_product.visibility = View.VISIBLE
                } else {
                    progressBar_add_product.visibility = View.GONE
                    Toast.makeText(activity, "Upload successful", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        })
    }

    private fun submitNewProductClicked() {
        val productName = product_name_add.text.toString()
        val brandName = brand_name_add.text.toString()
        val skuNumber = sku_id_add.text.toString()
        val category = category_add.text.toString()
        val unitType = units_add.text.toString()
        val sizePerItem = size_add.text.toString()
        val itemsPerSKU = quantityPerUnit_add.text.toString()
        val priceOfSKU = String.format("%.2f", salePrice_add.text.toString().toDouble())
        val itemsAvailable = quantity_forSale.text.toString()
        val productType = type_add.text.toString()

        if (productName.isEmpty()) {
            Toast.makeText(activity, "Enter product name", Toast.LENGTH_SHORT).show()
            return
        }
        if (brandName.isEmpty()) {
            Toast.makeText(activity, "Enter brand name", Toast.LENGTH_SHORT).show()
            return
        }
        if (skuNumber.isEmpty()) {
            Toast.makeText(activity, "Enter sku number", Toast.LENGTH_SHORT).show()
            return
        }
        if (category.isEmpty()) {
            Toast.makeText(activity, "Enter category", Toast.LENGTH_SHORT).show()
            return
        }
        if (unitType.isEmpty()) {
            Toast.makeText(activity, "Enter units", Toast.LENGTH_SHORT).show()
            return
        }
        if (sizePerItem.isEmpty()) {
            Toast.makeText(activity, "Enter size per unit", Toast.LENGTH_SHORT).show()
            return
        }
        //CHANGE THIS LABELING LATER
        if (itemsPerSKU.isEmpty()) {
            Toast.makeText(activity, "Enter quantity per unit", Toast.LENGTH_SHORT).show()
            return
        }
        if (priceOfSKU.isEmpty()) {
            Toast.makeText(activity, "Enter price", Toast.LENGTH_SHORT).show()
            return
        }
        if (itemsAvailable.isEmpty()) {
            Toast.makeText(activity, "Enter inventory availability", Toast.LENGTH_SHORT).show()
            return
        }

        if (productType.isEmpty()) {
            Toast.makeText(activity, "Enter type", Toast.LENGTH_SHORT).show()
            return
        }

        if (productImage == null) {
            Toast.makeText(activity, "Upload image", Toast.LENGTH_SHORT).show()
            return
        }



        viewModel.makeProductObject(
            productName,
            unitType,
            itemsPerSKU.toInt(),
            sizePerItem.toDouble(),
            skuNumber,
            priceOfSKU.toDouble(),
            brandName,
            productType,
            category,
            itemsAvailable.toInt(),
            productImage!!
        )


    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, "Error capturing image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageFromGallery() {
        val getPictureIntent = Intent(Intent.ACTION_PICK)
        getPictureIntent.type = "image/*"
        startActivityForResult(getPictureIntent, IMAGE_PICK_CODE)
    }



//        private fun dispatchTakePictureIntent() {
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
//                        "saymobile.company.saytech.fileprovider",
//                        it
//                    )
//                    imageUri = photoURI
////                    testingPath = photoFile
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
//                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//                }
//            }
//        }
//    }
    private fun checkImagePermissions() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            getImageFromGallery()
        } else {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            requestPermissions(permissions, PERMISSION_CODE)
        }
    }

    /**
     * Add option for camera or gallery
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            pic_preview.setImageURI(data.data)
            val bmp = (pic_preview.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()
            productImage = byteArray
        }
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && data != null){
           val bmp = data.extras?.get("data") as Bitmap
            pic_preview.setImageBitmap(bmp)
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()
            productImage = byteArray
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImageFromGallery()
                } else {
                    Toast.makeText(activity, "Permission acccess photos denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    }

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
}
