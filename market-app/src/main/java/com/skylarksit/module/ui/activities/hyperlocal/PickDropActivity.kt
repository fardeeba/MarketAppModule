@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.activities.hyperlocal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.gson.JsonObject
import com.skylarksit.module.R
import com.skylarksit.module.lib.Rest
import com.skylarksit.module.libs.alertdialog.NokAlertDialog
import com.skylarksit.module.pojos.AddressObject
import com.skylarksit.module.pojos.CollectionData
import com.skylarksit.module.pojos.DeliveryChargeResponseBean
import com.skylarksit.module.pojos.services.MenuItemObject
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.model.ViewRouter
import com.skylarksit.module.ui.utils.IClickListener
import com.skylarksit.module.utils.GoogleMapsPath
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities
import java.util.*

class PickDropActivity : MyAppCompatActivity(), OnMapReadyCallback {

    lateinit var confirmButton: View
    private lateinit var dynamicDeliveryChargeLayout: RelativeLayout
    private lateinit var deliveryChargeTextView: TextView
    private lateinit var etaLabel: TextView
    private lateinit var etaTime: TextView
    private lateinit var surgePrice: TextView
    private lateinit var pricingButton: View
    private lateinit var tripTypeLayout: RelativeLayout
    private lateinit var tripType: Switch
    private lateinit var deliveryDetails: RelativeLayout
    private lateinit var pickupDetails: RelativeLayout
    lateinit var scrollView: ScrollView
    private lateinit var mapView: View
    private lateinit var mapLayout: RelativeLayout
    private lateinit var dropOffPin: ImageView
    private lateinit var pickupPin: ImageView
    private lateinit var closeMapButton: ImageButton
    lateinit var headerView: View
    private lateinit var deliveryLocationTextView: TextView
    private lateinit var pickupLocationTextView: TextView
    private lateinit var mapParent: RelativeLayout
    private lateinit var etaLayout: RelativeLayout
    private var eddressMap: GoogleMap? = null
    private var mapFragment: MapFragment? = null
    private var isExpanded = false
    private var selectedPickupEddress: AddressObject? = null
    private var selectedDeliveryEddress: AddressObject? = null
    private var activeService: ServiceObject? = null
    private var bounds: LatLngBounds? = null
    private var boundsSize = 0
    private var initialMapHeight = 0
    private var pricingDescription: String? = null
    private var selectedProduct: MenuItemObject? = null
    private var isValidPrice = false
    private var deliveryPrice = 0.0
    val collectionData: CollectionData
        get() = CollectionData("pick_drop", "Pick &amp; Drop", CollectionData.Type.SERVICE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pick_drop_activity)
        confirmButton = findViewById(R.id.confirmButton)
        dynamicDeliveryChargeLayout = findViewById(R.id.dynamicDeliveryChargeLayout)
        deliveryChargeTextView = findViewById(R.id.deliveryChargePrice)
        etaLabel = findViewById(R.id.etaLabel)
        etaTime = findViewById(R.id.etaTime)
        surgePrice = findViewById(R.id.surgePrice)
        pricingButton = findViewById(R.id.priceText)
        tripTypeLayout = findViewById(R.id.tripTypeLayout)
        tripType = findViewById(R.id.tripType)
        deliveryDetails = findViewById(R.id.deliveryDetails)
        pickupDetails = findViewById(R.id.pickupDetails)
        mapParent = findViewById(R.id.mapParent)
        scrollView = findViewById(R.id.scrollView)
        etaLayout = findViewById(R.id.etaLayout)
        mapView = findViewById(R.id.mapView)
        mapLayout = findViewById(R.id.mapLayout)
        dropOffPin = findViewById(R.id.dropOffPin)
        pickupPin = findViewById(R.id.pickupPin)
        closeMapButton = findViewById(R.id.closeButton)
        headerView = findViewById(R.id.headerView)
        pricingButton.setOnClickListener { pricingButton() }
        confirmButton.setOnClickListener { confirm() }
        deliveryDetails.setOnClickListener { deliverTo() }
        pickupDetails.setOnClickListener { pickupFrom() }
        headerView.setOnClickListener { expandMap() }
        closeMapButton.setOnClickListener { shrinkMap() }
        val slug = intent.getStringExtra("activeService")
        activeService = model.findProviderByName(slug)
        if (activeService == null) return
        deliveryLocationTextView = findViewById(R.id.deliveryLocation)
        pickupLocationTextView = findViewById(R.id.pickupLocation)
        initialMapHeight = Utilities.dpToPx(190)
        mapFragment = fragmentManager.findFragmentById(R.id.eddressMap) as MapFragment
        mapFragment!!.getMapAsync(this)
        if (mapFragment != null && mapFragment!!.view != null) {
            mapFragment!!.view!!.layoutParams.height = initialMapHeight
        }
        headerView.layoutParams.height = initialMapHeight
        selectedProduct = getItem("singletrip")
        model.clearCart(false)
        model.addToCart(this@PickDropActivity, selectedProduct, 1, collectionData, null)
        tripType.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            model.clearCart(false)
            selectedProduct = if (isChecked) {
                getItem("roundtrip")
            } else {
                getItem("singletrip")
            }
            dynamicDeliveryCharge
        }
    }

    private fun pricingButton() {
        val alert = NokAlertDialog(this@PickDropActivity).setTitle(context.getString(R.string.pricing_details))
            .showCancelButton(false).setMessage(pricingDescription).setConfirmText(
                context.getString(
                    R.string.dismiss
                )
            )
        alert.setConfirmClickListener { alert.dismiss() }
        alert.show()
    }

    private fun confirm() {
        if (!model.hasUserDetails()) {
            val intent = Intent(context, UserDetailsActivity::class.java)
            startActivityForResult(intent, 1239)
            return
        }
        if (validatePage()) {
            ViewRouter.instance().startCheckout(this@PickDropActivity, activeService)
        }
    }

    // delivery price is only for distance based delivery.
    // serviceModel.deliveryPrice will be shown separately as delivery price on checkout
    private val dynamicDeliveryCharge: Unit
        get() {
            if (selectedPickupEddress == null || selectedDeliveryEddress == null) return
            val params = JsonObject()
            params.addProperty("serviceUid", activeService!!.serviceProviderUid)
            params.addProperty("productUid", selectedProduct!!.sku)
            if (model.pickupEddress != null) {
                params.addProperty("pickupLat", model.pickupEddress.lat)
                params.addProperty("pickupLon", model.pickupEddress.lon)
            }
            if (model.deliveryEddress != null) {
                params.addProperty("deliveryLat", model.deliveryEddress.lat)
                params.addProperty("deliveryLon", model.deliveryEddress.lon)
            }
            if ("roundtrip" == selectedProduct!!.sku) {
                params.addProperty("isRoundTrip", true)
            }
            isValidPrice = false
            Rest.request().uri("getDynamicDeliveryCharge").params(params).showLoader(getString(R.string.loader_text))
                .response(
                    DeliveryChargeResponseBean::class.java, Response.Listener { response: DeliveryChargeResponseBean ->
                        if (response.deliveryPrice != null) {
                            model.deliveryDistance = response.distance
                            model.deliveryDistanceLabel = response.distanceLabel
                            isValidPrice = true
                            etaLayout.visibility = View.VISIBLE
                            etaLabel.text = response.distanceLabel
                            etaTime.text = response.durationLabel
                            if (response.surge != null && response.surge.isNotEmpty()) {
                                surgePrice.text = response.surge
                                surgePrice.visibility = View.VISIBLE
                            } else {
                                surgePrice.visibility = View.GONE
                            }
                            pricingDescription = response.pricingDescription
                            if (pricingDescription == null) {
                                pricingButton.visibility = View.GONE
                            } else {
                                pricingButton.visibility = View.VISIBLE
                            }
                            model.clearCart(false)
                            model.addToCart(this@PickDropActivity, selectedProduct, 1, collectionData, null)
                            deliveryPrice = response.deliveryPrice
                            // delivery price is only for distance based delivery.
                            // serviceModel.deliveryPrice will be shown separately as delivery price on checkout
                            if (model.hasDynamicPricing(activeService)) {
                                model.deliveryPrice = deliveryPrice
                            }
                            if (deliveryPrice > 0) {
                                dynamicDeliveryChargeLayout.visibility = View.VISIBLE
                            } else {
                                dynamicDeliveryChargeLayout.visibility = View.GONE
                            }
                            confirmButton.visibility = View.VISIBLE
                            deliveryChargeTextView.text = Utilities.formatCurrency(
                                deliveryPrice,
                                activeService!!.currency.iso,
                                activeService!!.currency.hideDecimals
                            )
                        }
                    } as Response.Listener<DeliveryChargeResponseBean>).post()
        }

    private fun validatePage(): Boolean {
        if (model.pickupEddress == null) {
            Utilities.Toast(getString(R.string.please_select_pickup))
            return false
        }
        if (model.deliveryEddress == null) {
            Utilities.Toast(getString(R.string.please_select_delivery))
            return false
        }
        if (model.pickupEddress.code == model.deliveryEddress.code && model.deliveryEddress.code != "PIN_LOCATION") {
            Utilities.Toast(getString(R.string.same_pickup_and_delivery))
            return false
        }
        if (!isValidPrice) {
            Utilities.Toast(getString(R.string.invalid_delivery_price))
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 1239) {
                if (model.hasUserDetails()) {
                    confirmButton.callOnClick()
                }
            }
        }
    }

    private fun refreshView() {
        if (selectedPickupEddress == null) {
            deliveryDetails.visibility = View.GONE
            pickupDetails.background = getDrawable(R.drawable.rounded_corners_white_10)
            (pickupDetails.layoutParams as RelativeLayout.LayoutParams).setMargins(
                Utilities.dpToPx(16),
                Utilities.dpToPx(4),
                Utilities.dpToPx(16),
                Utilities.dpToPx(4)
            )

//            noteDetails.setVisibility(View.GONE);
            tripTypeLayout.visibility = View.GONE
        } else {
            pickupDetails.background = getDrawable(R.drawable.rounded_corners_white_10_top)
            (pickupDetails.layoutParams as RelativeLayout.LayoutParams).setMargins(
                Utilities.dpToPx(16),
                Utilities.dpToPx(4),
                Utilities.dpToPx(16),
                Utilities.dpToPx(0)
            )
            (deliveryDetails.layoutParams as RelativeLayout.LayoutParams).setMargins(
                Utilities.dpToPx(16),
                Utilities.dpToPx(1),
                Utilities.dpToPx(16),
                Utilities.dpToPx(4)
            )
            deliveryDetails.visibility = View.VISIBLE
        }
        if (selectedPickupEddress != null && selectedDeliveryEddress != null) {
//            noteDetails.setVisibility(View.VISIBLE);
            tripTypeLayout.visibility = View.VISIBLE
        }
        createBounds()
        animateMap()
    }

    override fun onResume() {
        super.onResume()
        if (activeService == null || !model.isCartUsed) {
            model.clearCart(false)
            finish()
            return
        }


//        if (addressChanged || !initialized){
//            getDynamicDeliveryCharge();
//            refreshView();
//            initialized = true;
//        }
    }

    private fun getPickedAddressString(address: AddressObject?): String {
        return if (address == null) {
            ""
        } else address.label
    }

    private fun deliverTo() {
        showAddress(false)
    }

    private fun pickupFrom() {
        showAddress(true)
    }

    private fun showAddress(isPickup: Boolean) {
        val text = if (isPickup) R.string.select_pick_up_location else R.string.select_drop_off_location
        ViewRouter.instance().showAddressPopup(
            this@PickDropActivity,
            text,
            activeService!!.slug,
            isPickup,
            false,
            object : IClickListener<AddressObject> {
                override fun click(`object`: AddressObject?) {
                    if (isPickup && `object` !== selectedPickupEddress) {
                        val pickupAddress = getPickedAddressString(`object`)
                        if (pickupAddress.isNotEmpty()) {
                            pickupLocationTextView.text = pickupAddress
                            model.pickupEddress = `object`
                            selectedPickupEddress = model.pickupEddress
                            Utilities.setFontStyle(pickupLocationTextView, R.style.Large)
                        }
                    } else if (!isPickup && `object` !== selectedDeliveryEddress) {
                        val deliveryAddress = getPickedAddressString(`object`)
                        if (deliveryAddress.isNotEmpty()) {
                            deliveryLocationTextView.text = deliveryAddress
                            model.deliveryEddress = `object`
                            selectedDeliveryEddress = model.deliveryEddress
                            Utilities.setFontStyle(deliveryLocationTextView, R.style.Large)
                        }
                    }
                    dynamicDeliveryCharge
                    refreshView()
                }
            } as IClickListener<AddressObject>)
    }

    private fun expandMap() {
        isExpanded = true
        eddressMap!!.uiSettings.isMapToolbarEnabled = true
        eddressMap!!.uiSettings.isScrollGesturesEnabled = true
        eddressMap!!.uiSettings.isZoomGesturesEnabled = true
        scrollView.visibility = View.GONE
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            eddressMap!!.isMyLocationEnabled = true
        }
        if (mapFragment != null && mapFragment!!.view != null) {
            mapFragment!!.view!!.layoutParams.height = Utilities.screenHeight
        }
        closeMapButton.visibility = View.VISIBLE
        mapParent.removeView(mapView)
        mapLayout.addView(mapView, 0)
    }

    private fun shrinkMap() {
        isExpanded = false
        eddressMap!!.uiSettings.isMapToolbarEnabled = false
        eddressMap!!.uiSettings.isScrollGesturesEnabled = false
        eddressMap!!.uiSettings.isZoomGesturesEnabled = false
        scrollView.visibility = View.VISIBLE
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            eddressMap!!.isMyLocationEnabled = false
        }
        if (mapFragment != null && mapFragment!!.view != null) {
            mapFragment!!.view!!.layoutParams.height = initialMapHeight
        }
        closeMapButton.visibility = View.GONE
        mapLayout.removeView(mapView)
        mapParent.addView(mapView, 0)
    }

    private fun animateMap() {
        if (eddressMap != null && bounds != null) {
            if (boundsSize == 0) {
                eddressMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds!!.center, Utilities.zoomFar))
            }
            if (boundsSize > 1) {
                eddressMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            } else if (boundsSize == 1) {
                eddressMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds!!.center, Utilities.closeZoom))
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        eddressMap = map
        if (map.uiSettings == null) return
        map.uiSettings.isZoomGesturesEnabled = false
        map.uiSettings.setAllGesturesEnabled(false)
        map.uiSettings.isScrollGesturesEnabled = false
        map.uiSettings.isRotateGesturesEnabled = false
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isCompassEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMyLocationButtonEnabled = false
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@PickDropActivity, R.raw.style_json))
        refreshView()
    }

    private fun createBounds() {
        if (eddressMap == null) return
        eddressMap!!.clear()
        val x = Utilities.dpToPx(24).toDouble() //original width 60pixels
        var pickupLocation: LatLng? = null
        if (selectedPickupEddress != null && selectedPickupEddress!!.lat != null && selectedPickupEddress!!.lat != 0.0) {
            pickupLocation = LatLng(selectedPickupEddress!!.lat, selectedPickupEddress!!.lon)
            val marker = eddressMap!!.addMarker(MarkerOptions().position(pickupLocation))
            val bmp = Utilities.getMarkerPin(context, R.color.primaryColor, x.toInt())
            if (bmp != null) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp))
            }
        }
        var deliveryLocation: LatLng? = null
        if (selectedDeliveryEddress != null && selectedDeliveryEddress!!.lat != null && selectedDeliveryEddress!!.lat != 0.0) {
            deliveryLocation = LatLng(selectedDeliveryEddress!!.lat, selectedDeliveryEddress!!.lon)
            val marker = eddressMap!!.addMarker(MarkerOptions().position(deliveryLocation))
            val bmp = Utilities.getMarkerPin(context, R.color.secondaryColor, x.toInt())
            if (bmp != null) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp))
            }
        }
        boundsSize = 0
        val builder = LatLngBounds.Builder()
        if (deliveryLocation != null && deliveryLocation.latitude != 0.0) {
            builder.include(deliveryLocation)
            boundsSize++
        }
        if (pickupLocation != null && pickupLocation.latitude != 0.0) {
            builder.include(pickupLocation)
            boundsSize++
        }
        if (boundsSize == 0 && model.validLocation != null) {
            builder.include(model.validLocation)
        }
        bounds = builder.build()
        if (activeService!!.turfs != null) {
            val options = PolygonOptions().strokeWidth(2f).strokeColor(0x999999).fillColor(0x66999999).geodesic(true)
            options.addAll(Utilities.createOuterBounds())
            val holes: MutableList<List<LatLng>> = ArrayList()
            for (turf in activeService!!.turfs) {
                val poly = turf.getTurfPoly()
                if (poly != null && poly.isNotEmpty()) {
                    holes.add(poly)
                }
            }
            eddressMap!!.addPolygon(options).holes = holes

//            for (MarketStoreTurf turf:activeService.turfs){
//                List<LatLng> poly = turf.getTurfPoly();
//                if (poly!= null && poly.size()>0){
//                    PolylineOptions options = new PolylineOptions().width(5).color(0x999999).geodesic(true).addAll(poly);
//                    eddressMap.addPolyline(options);
//                }
//            }
        }
        if (pickupLocation != null && deliveryLocation != null) {
            GoogleMapsPath(this@PickDropActivity, eddressMap!!, pickupLocation, deliveryLocation)
        }
    }

    override fun onBackPressed() {
        if (isExpanded) {
            shrinkMap()
            return
        }
        model.pickupEddress = null
        model.deliveryEddress = null
        model.clearCart(false)
        super.onBackPressed()
    }

    fun getItem(itemType: String): MenuItemObject? {
        if (itemType == "singletrip" || itemType == "roundtrip") {
            for (cat in activeService!!.categories) {
                for (sub in cat.subcategories) {
                    for (itemObject in sub.items) {
                        if (itemObject.productSku != null && itemObject.productSku == itemType ||
                            itemObject.sku != null && itemObject.sku == itemType
                        ) return itemObject
                    }
                }
            }
        }
        return null
    }
}
