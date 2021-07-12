package waybilmobile.company.waybil.view.inventory

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_add_product_inventory.*
import kotlinx.android.synthetic.main.fragment_add_product_inventory.pic_preview

import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.util.getSkuList
import waybilmobile.company.waybil.viewmodel.products.AddProductViewModel
import java.io.ByteArrayOutputStream

private lateinit var currentPhotoPath: String
private const val IMAGE_PICK_CODE = 1001
private const val REQUEST_IMAGE_CAPTURE = 1002
private const val PERMISSION_CODE = 1000

class AddProductInventory : Fragment() {

    private lateinit var viewModel: AddProductViewModel
    private var productImage: ByteArray? = null
    private var unitsText: String = ""
    private var sizeText: String = ""
    private var quantityText: String = ""

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

        units_add.afterTextChanged {
            unitsText = it
            updatePreview()
        }

        quantityPerUnit_add.afterTextChanged {
            quantityText = it
            updatePreview()
        }

        size_add.afterTextChanged {
            sizeText = it
            updatePreview()
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
                    Toast.makeText(activity, R.string.upload_successful, Toast.LENGTH_SHORT).show()
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
        val itemsAvailable = quantity_available.text.toString()
        val productType = type_add.text.toString()
        val skuList = getSkuList()

        skuList?.let {
            if (skuList.contains(skuNumber.toLowerCase())) {
                Toast.makeText(activity, R.string.sku_exists, Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (productName.isEmpty()) {
            Toast.makeText(activity, R.string.enter_product_name, Toast.LENGTH_SHORT).show()
            return
        }
        if (brandName.isEmpty()) {
            Toast.makeText(activity, R.string.enter_brand_name, Toast.LENGTH_SHORT).show()
            return
        }
        if (skuNumber.isEmpty()) {
            Toast.makeText(activity, R.string.enter_sku_number, Toast.LENGTH_SHORT).show()
            return
        }

        if (category.isEmpty()) {
            Toast.makeText(activity, R.string.enter_category, Toast.LENGTH_SHORT).show()
            return
        }
        if (unitType.isEmpty()) {
            Toast.makeText(activity, R.string.enter_unit_of_measurement, Toast.LENGTH_SHORT).show()
            return
        }
        if (sizePerItem.isEmpty()) {
            Toast.makeText(activity, R.string.enter_size_per_unit, Toast.LENGTH_SHORT).show()
            return
        }
        //CHANGE THIS LABELING LATER
        if (itemsPerSKU.isEmpty()) {
            Toast.makeText(activity, R.string.enter_quantity_per_unit, Toast.LENGTH_SHORT).show()
            return
        }
        if (priceOfSKU.isEmpty()) {
            Toast.makeText(activity, R.string.enter_price, Toast.LENGTH_SHORT).show()
            return
        }
        if (itemsAvailable.isEmpty()) {
            Toast.makeText(activity, R.string.enter_inventory_quantity, Toast.LENGTH_SHORT).show()
            return
        }

        if (productType.isEmpty()) {
            Toast.makeText(activity, R.string.enter_type, Toast.LENGTH_SHORT).show()
            return
        }

        if (productImage == null) {
            Toast.makeText(activity, R.string.upload_image, Toast.LENGTH_SHORT).show()
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
            Toast.makeText(activity, R.string.error_image_capture, Toast.LENGTH_SHORT).show()
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
                getColor(
                    theActivity,
                    R.color.colorAccent
                )
            )
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                getColor(
                    theActivity,
                    R.color.colorAccent
                )
            )
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            pic_preview.setImageURI(data.data)
            val bmp = (pic_preview.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()
            productImage = byteArray
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && data != null) {
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

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }
        })
    }

    private fun updatePreview() {
        val text = "$sizeText$unitsText x $quantityText"
        itemDescrip_preview.text = text
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
