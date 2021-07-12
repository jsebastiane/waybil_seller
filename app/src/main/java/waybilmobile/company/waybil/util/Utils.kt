package waybilmobile.company.waybil.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.StorageReference
import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.model.GlideApp
import waybilmobile.company.waybil.model.orders.Order
import java.text.DecimalFormat

private var temporaryGeoPoint: GeoPoint? = null
private var focusedOrder: Order? = null
private var profileIssue: Boolean = false
private var skuList: ArrayList<String>? = null

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

fun setSkuList(list: ArrayList<String>){
    skuList = list
}

fun getSkuList():ArrayList<String>?{
    return skuList
}

fun resetSkuList(){
    skuList = null
}

fun formatCurrency(price: Double) : String {
    /**
     * Use currency format later
     */
    val decForm = DecimalFormat("Q#,###.00")

    return decForm.format(price)
}

fun getProfileIssue(): Boolean{
    return profileIssue
}

fun setProfileIssue(issue : Boolean){
    profileIssue = issue
}

//Temporarily stores GeoPoint for SignUp
//Reset to null after SignUp
fun tempGeoPoint(geoPoint: GeoPoint){
    temporaryGeoPoint = geoPoint
}

fun getCurrentLocation(): GeoPoint?{
    return temporaryGeoPoint
}

fun resetTempGeoPoint(){
    temporaryGeoPoint = null
}

fun setFocusedOrder(order: Order){
    focusedOrder = order
}

fun getFocusedOrder(): Order?{
    return focusedOrder
}

fun resetFocusedOrder(){
    focusedOrder = null
}

