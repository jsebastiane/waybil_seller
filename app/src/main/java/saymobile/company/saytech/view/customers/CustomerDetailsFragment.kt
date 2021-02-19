package saymobile.company.saytech.view.customers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_customer_details.*

import saymobile.company.saytech.R
import saymobile.company.saytech.util.CustomSupportMapFragment
import saymobile.company.saytech.viewmodel.customers.CustomerDetailsViewModel
import saymobile.company.saytech.viewmodel.products.ProductDetailsViewModel


class CustomerDetailsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var viewModel: CustomerDetailsViewModel
    private lateinit var mMap: GoogleMap
    private var customerId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customer_details, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel = ViewModelProviders.of(this).get(CustomerDetailsViewModel::class.java)
        //***************


//        val mapFragment = supportFragmentManager.findFragmentById(R.id.customerDetails_map) as CustomSupportMapFragment
//        mapFragment.getMapAsync(this)
//        mapFragment.setListener(object: CustomSupportMapFragment.OnTouchListener {
//            override fun onTouch() {
//                customerDetails_scrollView.requestDisallowInterceptTouchEvent(true)
//            }
//        })

        customerDetails_scrollView.requestDisallowInterceptTouchEvent(true)


        //************

        arguments?.let {
            customerId = CustomerDetailsFragmentArgs.fromBundle(it).customerId
        }

        //Child fragment manager is used as we are operating in a fragment activity with a map fragment
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.customerDetails_map) as CustomSupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.setListener(object: CustomSupportMapFragment.OnTouchListener {
            override fun onTouch() {
                customerDetails_scrollView.requestDisallowInterceptTouchEvent(true)
            }
        })

        close_fragment_customerDetails.setOnClickListener {
            findNavController().navigateUp()
        }

        observeViewModels()
        viewModel.refresh(customerId)

    }


    private fun observeViewModels(){
        viewModel.focusedCustomer.observe(viewLifecycleOwner, Observer { focusedCustomer ->
            focusedCustomer?.let {
                customerName_Detailsfragment.text = focusedCustomer.customer.businessName
                customerDetails_phoneNumber.text = focusedCustomer.customer.userMobileNumber
                customerDetails_address.text = focusedCustomer.customer.userAddress
                /**
                 * Remove this one later - just seeing how this will look with real data
                 */
                average_orderSize_figure.text = focusedCustomer.lifeTimeOrderQuantity.toString()
                val longitude = focusedCustomer.customer.geoLocation?.longitude
                val latitude = focusedCustomer.customer.geoLocation?.latitude
                val customerLocation = LatLng(latitude!!, longitude!!)
                mMap.addMarker(MarkerOptions().position(customerLocation).title(focusedCustomer.customer.businessName))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 15f))

            }
        })

    }


    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mMap = googleMap
        }

    }


}
