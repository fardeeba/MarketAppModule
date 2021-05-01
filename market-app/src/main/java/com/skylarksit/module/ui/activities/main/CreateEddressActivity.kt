@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.activities.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.fenchtose.tooltip.Tooltip
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.*
import com.skylarksit.module.R
import com.skylarksit.module.analytics.Segment.addressCreated
import com.skylarksit.module.lib.Rest
import com.skylarksit.module.libs.Json
import com.skylarksit.module.libs.alertdialog.AddressDetailsPopup
import com.skylarksit.module.libs.alertdialog.NokAlertDialog
import com.skylarksit.module.pojos.Address
import com.skylarksit.module.pojos.AddressObject
import com.skylarksit.module.pojos.AddressResponse
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.lists.items.SearchResultItem
import com.skylarksit.module.ui.lists.main.adapters.SearchResultAdapter
import com.skylarksit.module.ui.model.IListItem
import com.skylarksit.module.ui.utils.IClickListener
import com.skylarksit.module.ui.utils.ItemClickListener
import com.skylarksit.module.ui.utils.LocalStorage
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities
import java.io.IOException
import java.util.*
import kotlin.math.cos

class CreateEddressActivity : MyAppCompatActivity(), OnMapReadyCallback, OnCameraIdleListener {
    private var eddressMap: GoogleMap? = null
    private lateinit var nextButton: Button
    private lateinit var searchField: EditText
    private lateinit var resultsLayout: RelativeLayout
    private lateinit var clearSearch: RelativeLayout
    private var searchResultAdapter: SearchResultAdapter? = null
    private lateinit var myLocation: View
    private lateinit var searchAnimationLayout: RelativeLayout
    private val searchHint = "Search or move the map"
    private val searchResultItems = ArrayList<IListItem>()
    private var returnToMainView = false
    private var isPickup = false
    private var isHLS = false
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var isEditOrder = false
    private lateinit var switchMap: View
    private lateinit var pin: ImageView
    private var setAsCurrent = false
    var activeService: ServiceObject? = null
    private var token = AutocompleteSessionToken.newInstance()
    private var placesClient: PlacesClient? = null
    private var geocoder: Geocoder? = null
    private var mBounds: RectangularBounds? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_eddress)
        geocoder = Geocoder(this@CreateEddressActivity, Locale.getDefault())
        placesClient = Places.createClient(this)
        val slug = intent.getStringExtra("activeService")
        activeService = model.findProviderByName(slug)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = fragmentManager.findFragmentById(R.id.newMap) as MapFragment
        mapFragment.getMapAsync(this)
        myLocation = findViewById(R.id.mylocation)
        myLocation.setOnClickListener { requestLocationUpdates() }
        switchMap = findViewById(R.id.switchmap)
        switchMap.setOnClickListener(View.OnClickListener setOnClickListener@{
            if (eddressMap == null) {
                return@setOnClickListener
            }
            if (eddressMap!!.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                eddressMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
                pin.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pin_light))
                pin.colorFilter = null
            } else {
                eddressMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
                pin.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pin_dark))
                pin.setColorFilter(
                    ContextCompat.getColor(this@CreateEddressActivity, R.color.secondaryColor),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        })
        pin = findViewById(R.id.pin)
        pin.setColorFilter(ContextCompat.getColor(this, R.color.secondaryColor), PorterDuff.Mode.SRC_ATOP)
        val extras = intent.extras
        if (extras != null) {
            isHLS = extras.getBoolean("isHLS")
            isEditOrder = extras.getBoolean("editOrder")
        }
        setAsCurrent = intent.getBooleanExtra("setAsCurrent", false)
        returnToMainView = intent.getBooleanExtra("returnToMainView", false)
        isPickup = intent.getBooleanExtra("isPickup", false)
        searchField = findViewById(R.id.searchLocation)
        addTooltip(searchField, R.string.tip_search_location, Tooltip.BOTTOM)

//        skipButton =  findViewById(R.id.skip);
        nextButton = findViewById(R.id.nextButton)
        searchField.setSingleLine()
        searchField.setOnClickListener { showResults() }
        searchField.onFocusChangeListener = OnFocusChangeListener { _: View?, b: Boolean ->
            if (b) {
                showResults()
            }
        }
        searchField.addTextChangedListener(object : TextWatcher {
            private var timer = Timer()
            override fun beforeTextChanged(charSequernce: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                timer.cancel()
                timer = Timer()
                // milliseconds
                val delay: Long = 500
                timer.schedule(
                    object : TimerTask() {
                        override fun run() {
                            startSearchAnimation()
                            if (searchField.text.length >= 2) {
                                searchForAddresses(searchField.text.toString())
                            } else if (searchField.text.isEmpty()) {
                                searchResultItems.clear()
                                runOnUiThread {
                                    searchResultAdapter!!.notifyDataSetChanged()
                                    stopSearchAnimation()
                                }
                            }
                        }
                    },
                    delay
                )
            }
        })
        searchField.setOnEditorActionListener(OnEditorActionListener setOnEditorActionListener@{ _: TextView?, i: Int, _: KeyEvent? ->
            if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NEXT || i == EditorInfo.IME_ACTION_UNSPECIFIED || i == EditorInfo.IME_ACTION_SEARCH) {
                Utilities.hideKeyboard(this@CreateEddressActivity)
                return@setOnEditorActionListener true
            }
            false
        })
        searchAnimationLayout = findViewById(R.id.animationLayout)
        searchAnimationLayout.visibility = View.GONE
        resultsLayout = findViewById(R.id.resultsLayout)
        val searchListView = findViewById<RecyclerView>(R.id.searchResultList)
        searchListView.setOnTouchListener { _: View?, _: MotionEvent? ->
            Utilities.hideKeyboard(this@CreateEddressActivity)
            false
        }
        searchListView.layoutManager = LinearLayoutManager(this)
        searchResultAdapter = SearchResultAdapter(context, searchResultItems, withHeader = false, withFooter = false)
        searchListView.adapter = searchResultAdapter
        searchResultAdapter!!.setClickListener(object : ItemClickListener<SearchResultItem?>(searchResultAdapter!!) {
            override fun onClick(item: SearchResultItem?, position: Int) {
                if (item == null) return
                if (item.type == "GOOGLE") locationPicked((item.data as AutocompletePrediction).placeId)
            }
        })
        clearSearch = findViewById(R.id.clearSearch)
        clearSearch.setOnClickListener {
            searchField.setText("")
            hideResults()
        }
        searchField.hint = searchHint
        searchField.isEnabled = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 4423) {
            selectMyEddress(Utilities.activeAddressObject)
            saveValue("continueFinish", true)
            finish()
        }
    }

    private fun startSearchAnimation() {
        runOnUiThread {
            clearSearch.visibility = View.GONE
            searchAnimationLayout.visibility = View.VISIBLE
        }
    }

    private fun stopSearchAnimation() {
        runOnUiThread { searchAnimationLayout.visibility = View.GONE }
    }

    private fun selectMyEddress(item: AddressObject) {
        if (isPickup) model.pickupEddress = item else {
            model.deliveryEddress = item
        }
    }

    private var showingResults = false
    private fun showResults() {
        showingResults = true
        clearSearch.visibility = View.VISIBLE
        searchField.hint = ""
        resultsLayout.visibility = View.VISIBLE
        nextButton.visibility = View.GONE
        myLocation.visibility = View.GONE
        switchMap.visibility = View.GONE
    }

    private fun hideResults() {
        showingResults = false
        myLocation.visibility = View.VISIBLE
        switchMap.visibility = View.VISIBLE
        resultsLayout.visibility = View.GONE
        clearSearch.visibility = View.GONE
        searchField.hint = searchHint
        Utilities.hideKeyboard(this)
        searchField.clearFocus()
        nextButton.visibility = View.VISIBLE
        searchResultItems.clear()
        searchResultAdapter!!.notifyDataSetChanged()
        stopSearchAnimation()
    }

    override fun onBackPressed() {
        if (showingResults) {
            hideResults()
            return
        }
        super.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == Utilities.FINE_LOCATION_PERMISSION) {
            if (isEditOrder) return
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates()
            }
        }
    }

    private fun computeBounds() {
        mBounds = null
        if (model.currentAddress != null) {
            mBounds = getBounds(model.currentAddress.location)
        } else if (model.currentLocation != null) {
            val loc1 = Location("")
            loc1.latitude = model.currentLocation.latitude
            loc1.longitude = model.currentLocation.longitude
            mBounds = getBounds(loc1)
        }
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mFusedLocationClient!!.lastLocation
            .addOnSuccessListener(this) { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    if (eddressMap != null) {
                        val loc = LatLng(location.latitude, location.longitude)
                        model.lastKnownLocation = loc
                        eddressMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, Utilities.closeZoom))
                    }
                }
            }
    }

    override fun onMapReady(map: GoogleMap) {
        eddressMap = map
        searchField.isEnabled = true
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Utilities.FINE_LOCATION_PERMISSION
            )
        } else {
            eddressMap!!.isMyLocationEnabled = true
        }

//        eddressMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(CreateEddressActivity.this, R.raw.style_json));
        eddressMap!!.uiSettings.isMapToolbarEnabled = false
        eddressMap!!.uiSettings.isMyLocationButtonEnabled = false
        eddressMap!!.uiSettings.isScrollGesturesEnabled = true
        eddressMap!!.uiSettings.isZoomGesturesEnabled = true
        eddressMap!!.uiSettings.isRotateGesturesEnabled = false
        eddressMap!!.uiSettings.isTiltGesturesEnabled = false
        eddressMap!!.setOnCameraIdleListener(this)
        if (isHLS && activeService != null && activeService!!.turfs != null) {
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
        }
        if (isEditOrder && Utilities.activeAddressObject != null && java.lang.Boolean.TRUE == Utilities.activeAddressObject.isPinned) {
            val latLng = LatLng(Utilities.activeAddressObject.lat, Utilities.activeAddressObject.lon)
            eddressMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, Utilities.closeZoom))
        } else {
            requestLocationUpdates()
        }
        hideResults()
    }

    override fun onResume() {
        super.onResume()
        if (getBooleanValue("continueFinish")) {
            finish()
        } else {
            nextButton.isEnabled = true
        }
    }

    private fun getBounds(location: Location): RectangularBounds {
        val latRadian = Math.toRadians(location.latitude)
        val mDistanceInMeters = 100000.0
        val degLatKm = 110.574235
        val degLongKm = 110.572833 * cos(latRadian)
        val deltaLat = mDistanceInMeters / 1000.0 / degLatKm
        val deltaLong = mDistanceInMeters / 1000.0 / degLongKm
        val minLat = location.latitude - deltaLat
        val minLong = location.longitude - deltaLong
        val maxLat = location.latitude + deltaLat
        val maxLong = location.longitude + deltaLong
        return RectangularBounds.newInstance(
            LatLng(minLat, minLong),
            LatLng(maxLat, maxLong)
        )
    }

    @Synchronized
    private fun searchForAddresses(search: String) {
        googleResults = null
        val requestBuilder = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(search)
        computeBounds()
        if (mBounds != null) {
            requestBuilder.locationRestriction = mBounds
        } else if (activeService != null) {
            requestBuilder.setCountry(activeService!!.countryIso)
        }
        val request = requestBuilder.build()
        placesClient!!.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                val items: MutableList<SearchResultItem> = ArrayList()
                for (prediction in response.autocompletePredictions) {
                    val i = SearchResultItem()
                    i.label = prediction.getPrimaryText(null).toString()
                    i.description = prediction.getSecondaryText(null).toString()
                    i.icon = R.drawable.google_icon
                    i.data = prediction
                    i.type = "GOOGLE"
                    items.add(i)
                }
                processResults(items)
            }.addOnFailureListener { }
    }

    private var googleResults: List<SearchResultItem>? = null
    private fun processResults(results: List<SearchResultItem>) {
        googleResults = results
        if (googleResults == null) {
            return
        }
        searchResultItems.clear()
        searchResultItems.addAll(googleResults!!)
        searchResultAdapter!!.notifyDataSetChanged()
        stopSearchAnimation()
    }

    fun locationPicked(placeId: String?) {
        clearSearch.callOnClick()

        // Specify the fields to return.
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        // Construct a request object, passing the place ID and fields array.
        val request = FetchPlaceRequest.builder(placeId!!, placeFields)
            .build()

        // Add a listener to handle the response.
        placesClient!!.fetchPlace(request).addOnSuccessListener { response: FetchPlaceResponse ->
            val place = response.place
            eddressMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, Utilities.closeZoom))
        }.addOnFailureListener { exception: Exception ->
            if (exception is ApiException) {
                // Handle error with given status code.
                Log.e("PLACE EXCEPTION", "Place not found: " + exception.message)
            }
        }
    }

    override fun onCameraIdle() {
        if (isFinishing) return
        val lat = eddressMap!!.cameraPosition.target.latitude
        val lon = eddressMap!!.cameraPosition.target.longitude
        if (isHLS && activeService != null) {
            if (!activeService!!.deliversTo(lat, lon)) {
                nextButton.text = getString(R.string.out_of_reach)
                nextButton.isEnabled = false
            } else {
                nextButton.text = getString(R.string.proceed)
                nextButton.isEnabled = true
            }
        } else {
            nextButton.text = getString(R.string.proceed)
            nextButton.isEnabled = true
        }
    }

    private fun saveAddress(address: Address) {
        val message = address.validate()
        if (message != null) {
            Utilities.Error("Validation Error", message)
            return
        }
        Rest.request().baseUrl("api/market/app/").uri("saveAddress").showLoader(getString(R.string.create_address))
            .params(Json.param().set("address", address)).response(
                AddressResponse::class.java, Response.Listener { response: AddressResponse ->
                    val addressObject = AddressObject(response.address)
                    Utilities.newAddressObject = addressObject
                    model.myLocations.add(addressObject)
                    if (isHLS) {
                        Utilities.activeAddressObject = model.findAddress(Utilities.newAddressObject.code)
                        if (isPickup) {
                            model.pickupEddress = Utilities.activeAddressObject
                        } else {
                            model.deliveryEddress = Utilities.activeAddressObject
                        }
                    }
                    Utilities.newAddressObject = null
                    LocalStorage.instance().save(context, "continueFinish", true)
                    if (setAsCurrent && !isPickup) {
                        model.setAsCurrentAddress = addressObject
                    }
                    if (returnToMainView) {
                        saveValue("returnToMainView", true)
                    }
                    addressCreated(addressObject)
                    addressDetailsPopup!!.hide()
                    finish()
                } as Response.Listener<AddressResponse>).post()
    }

    fun nextButtonClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        pinLocation()
    }

    private fun pinLocation() {
        if (eddressMap == null || eddressMap!!.cameraPosition == null) {
            return
        }
        val lat = eddressMap!!.cameraPosition.target.latitude
        val lon = eddressMap!!.cameraPosition.target.longitude
        if (lat == 0.0 || lon == 0.0) {
            Utilities.Error("Invalid Coordinates", "Please fix your pin and retry.")
            return
        }
        if (isHLS && !activeService!!.deliversTo(lat, lon)) {
            NokAlertDialog(this@CreateEddressActivity)
                .setTitle("Out of reach!")
                .setMessage("Sorry! we don't deliver there yet.")
                .show()
            return
        }
        getAreaNameOf(LatLng(lat, lon))
    }

    private fun addressDetailsPopup(address: Address) {
        if (addressDetailsPopup == null) {
            addressDetailsPopup = AddressDetailsPopup<Any?>(this)
            addressDetailsPopup!!.setConfirmClickListener(object : IClickListener<Address> {
                override fun click(`object`: Address?) {
                    if (`object` != null) {
                        saveAddress(`object`)
                    }
                }
            } as IClickListener<Address>)
        }
        addressDetailsPopup!!.address = address
        addressDetailsPopup!!.show()
    }

    private var addressDetailsPopup: AddressDetailsPopup<*>? = null
    override fun onGeocodeResult(location: LatLng, area: String) {
        super.onGeocodeResult(location, area)
        var address: Address? = null
        try {
            val location1 = geocoder!!.getFromLocation(location.latitude, location.longitude, 1)[0]
            address = Address(location1, location1.latitude, location1.longitude)
        } catch (ignored: IOException) {
        }
        if (address == null) {
            address = Address(location.latitude, location.longitude)
        }
        address.locality = area
        if (isEditOrder) {
            val returnIntent = Intent()
            returnIntent.putExtra("lat", location.latitude)
            returnIntent.putExtra("lon", location.longitude)
            returnIntent.putExtra("area", address.locality)
            returnIntent.putExtra("building", address.building)
            returnIntent.putExtra("unit", address.unit)
            returnIntent.putExtra("note", address.notes)
            setResult(RESULT_OK, returnIntent)
            finish()
            return
        }
        nextButton.isEnabled = true
        addressDetailsPopup(address)
    }
}
