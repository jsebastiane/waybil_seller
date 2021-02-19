package saymobile.company.saytech.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.StorageReference
import saymobile.company.saytech.R
import saymobile.company.saytech.model.GlideApp
import java.text.DecimalFormat

var temporaryGeoPoint: GeoPoint? = null

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun getProgressDrawable(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 10f
        centerRadius = 50f
        start()
    }
}

fun ImageView.loadImage(imageRef: StorageReference, progressDrawable: CircularProgressDrawable) {
    // place holder not working with circular imageView
    val options = RequestOptions()
        .placeholder(progressDrawable)
        .error(R.mipmap.ic_launcher)
        .centerCrop()
    GlideApp.with(this)
        .setDefaultRequestOptions(options)
        .load(imageRef)
        .into(this)
}

fun formatCurrency(price: Double) : String {
    /**
     * Use currency format later
     */
    val decForm = DecimalFormat("Q#,###.00")

    return decForm.format(price)
}

//Temporarily stores GeoPoint for SignUp
//Reset to null after SignUp
fun tempGeoPoint(geoPoint: GeoPoint){
    temporaryGeoPoint = geoPoint
}

