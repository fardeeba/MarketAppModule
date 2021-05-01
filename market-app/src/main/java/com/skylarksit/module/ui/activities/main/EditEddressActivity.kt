@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.activities.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.skylarksit.module.R
import com.skylarksit.module.lib.Rest
import com.skylarksit.module.libs.Json
import com.skylarksit.module.libs.alertdialog.NokAlertDialog
import com.skylarksit.module.pojos.Address
import com.skylarksit.module.pojos.AddressObject
import com.skylarksit.module.pojos.AddressResponse
import com.skylarksit.module.pojos.ResponseBean
import com.skylarksit.module.ui.utils.LocalStorage
import com.skylarksit.module.ui.utils.OnOneOffClickListener
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities

class EditEddressActivity : MyAppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, OnMyLocationButtonClickListener, OnMapReadyCallback {
    private var addressObject: AddressObject? = null
    private var eddressMap: GoogleMap? = null
    private var mapFragment: MapFragment? = null
    lateinit var mapParent: RelativeLayout
    lateinit var saveButton: Button
    lateinit var scrollView: View
    lateinit var mapView: View
    lateinit var mapLayout: RelativeLayout
    lateinit var headerView: View
    lateinit var updatePin: View
    private var initialMapHeight = 0
    private var isExpanded = false
    private var isHLS = false
    private var serviceSlug: String? = null
    var closeMapButton: ImageButton? = null
    private var saveEnabled = false
    private var mLocationCallback: LocationCallback? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var areaField: EditText
    private lateinit var buildingField: EditText
    private lateinit var moreInfoField: EditText
    private lateinit var aptField: EditText

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_eddress)
        mapParent = findViewById(R.id.mapParent)
        saveButton = findViewById(R.id.saveButton)
        scrollView = findViewById(R.id.scrollView)
        mapView = findViewById(R.id.mapView)
        mapLayout = findViewById(R.id.mapLayout)
        headerView = findViewById(R.id.headerView)
        updatePin = findViewById(R.id.updatePin)
        closeMapButton = findViewById(R.id.closeButton)
        saveButton.setOnClickListener { updateAddress() }
        updatePin.setOnClickListener { updatePin() }
        closeMapButton?.setOnClickListener { shrinkMap() }
        addressObject = Utilities.activeAddressObject.clone()
        isHLS = intent.getBooleanExtra("isHLS", false)
        serviceSlug = intent.getStringExtra("activeService")
        if (addressObject != null) {
            updateView()
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            mLocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult.locations.size > 0) {
                        val location = locationResult.locations[0]
                        model.lastKnownLocation = LatLng(location.latitude, location.longitude)
                    }
                }
            }
            areaField = findViewById(R.id.areaField)
            buildingField = findViewById(R.id.buildingField)
            moreInfoField = findViewById(R.id.moreInfoField)
            aptField = findViewById(R.id.aptField)
            areaField.setSingleLine()
            buildingField.setSingleLine()
            moreInfoField.setSingleLine()
            aptField.setSingleLine()
            setAddressInfo()
            val scrollView = findViewById<ScrollView>(R.id.scrollView)
            scrollView.setOnTouchListener { _: View?, _: MotionEvent? ->
                hideKeyboard(this@EditEddressActivity)
                false
            }
            initialMapHeight = Utilities.dpToPx(200)
            mapFragment = fragmentManager.findFragmentById(R.id.eddressMap) as MapFragment
            mapFragment!!.getMapAsync(this)
            if (mapFragment != null && mapFragment!!.view != null) {
                mapFragment!!.view!!.layoutParams.height = initialMapHeight
            }
            val deleteEddressButton = findViewById<View>(R.id.deleteEddressButton)
            deleteEddressButton.setOnClickListener {
                hideKeyboard(this@EditEddressActivity)
                deleteEddress()
            }
            headerView.layoutParams.height = initialMapHeight
            headerView.setOnClickListener(object : OnOneOffClickListener() {
                override fun onSingleClick(v: View) {
                    expandMap()
                }
            })
            areaField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    saveEnabled = true
                    updateView()
                    addressObject!!.locality = editable.toString()
                }
            })
            buildingField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    saveEnabled = true
                    updateView()
                    addressObject!!.building = editable.toString()
                }
            })
            moreInfoField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    saveEnabled = true
                    updateView()
                    addressObject!!.notes = editable.toString()
                }
            })
            aptField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    saveEnabled = true
                    updateView()
                    addressObject!!.unit = editable.toString()
                }
            })
        }
    }

    private fun setAddressInfo() {
        areaField.setText(addressObject!!.locality)
        if (addressObject!!.building != null) buildingField.setText(addressObject!!.building)
        if (addressObject!!.notes != null) moreInfoField.setText(addressObject!!.notes)
        if (addressObject!!.unit != null) {
            aptField.setText(addressObject!!.unit)
        }
    }

    fun updateAddress() {
        val message = addressObject!!.validate()
        if (message != null) {
            Utilities.Error("Validation Error", message)
            return
        }
        val address = Address(addressObject!!)
        address.eddressCode = addressObject!!.code
        Rest.request().baseUrl("api/market/app/").uri("saveAddress").showLoader(getString(R.string.saving_address))
            .params(Json.param().set("address", address)).response(
                AddressResponse::class.java, Response.Listener { response: AddressResponse ->
                    val newAddress = response.address
                    val newAddressObject = AddressObject(newAddress)
                    val obj = model.findAddress(addressObject!!.code)
                    obj.update(newAddressObject)
                    finish()
                } as Response.Listener<AddressResponse>).post()
    }

    fun updatePin() {
        val intent = Intent(this, CreateEddressActivity::class.java)
        intent.putExtra("editOrder", true)
        intent.putExtra("isHLS", isHLS)
        intent.putExtra("activeService", serviceSlug)
        startActivityForResult(intent, 1010)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Utilities.CAMERA_READ_EXTERNAL_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this@EditEddressActivity,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@EditEddressActivity,
                            arrayOf(Manifest.permission.CAMERA),
                            Utilities.CAMERA_PERMISSION
                        )
                    } else {
                        startActivityForResult(Utilities.getIntentForCam(this@EditEddressActivity), 0)
                    }
                }
            }
            Utilities.CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(Utilities.getIntentForCam(this@EditEddressActivity), 0)
                }
            }
        }
    }

    private fun deleteEddress() {
        val params = Json.param()
        params.p("appName", getString(R.string.application))
        params.p("idUser", LocalStorage.instance().getString("idUser"))
        params.p("uid", LocalStorage.instance().getString("uid"))
        params.p("code", addressObject!!.code)
        params.p("os", "android")
        val sDialog = NokAlertDialog(this@EditEddressActivity, NokAlertDialog.WARNING)
        sDialog.setConfirmText(getString(R.string.yes))
        sDialog.setCancelText(getString(R.string.nevermind))
        sDialog.setTitle(getString(R.string.title_are_you_sure_to_delete_address))
            .setConfirmClickListener {
                sDialog.hide()
                Rest.request().uri("deleteAddress").showLoader(getString(R.string.deleting_address)).params(params)
                    .response(
                        ResponseBean::class.java
                    ) {
                        val obj = model.findAddress(addressObject!!.code)
                        model.myLocations.remove(obj)
                        if ((addressObject == model.currentAddress)) {
                            model.currentLocation = model.currentAddress.latLng
                            model.currentAddress = null
                        }
                        saveValue("continueFinish", true)
                        finish()
                    }.post()
            }.show()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 1010) {
                val lat = data!!.getDoubleExtra("lat", 0.0)
                val lon = data.getDoubleExtra("lon", 0.0)
                if (lat == addressObject!!.lat && lon == addressObject!!.lon) return
                addressObject!!.locality = data.getStringExtra("area")

                /* if (!data.getStringExtra("building").isEmpty())
                    addressObject.building = data.getStringExtra("building");
                if (data.getStringExtra("unit").isEmpty())
                    addressObject.unit = data.getStringExtra("unit");
                if (!data.getStringExtra("note").isEmpty())
                    addressObject.notes = data.getStringExtra("note");*/setAddressInfo()
                addressObject!!.lat = lat
                addressObject!!.lon = lon
                val location = LatLng(lat, lon)
                eddressMap!!.clear()
                val marker = eddressMap!!.addMarker(MarkerOptions().position(location))
                val bmp = Utilities.getMarkerPin(context, R.color.secondaryColor, 0)
                if (bmp != null) {
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp))
                }
                eddressMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(location, Utilities.zoom))
            }
        }
    }

    private fun updateView() {
        saveButton.visibility = if (saveEnabled) View.VISIBLE else View.GONE
    }

    private fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (activity.currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        }
    }

    override fun onResume() {
        if (getBooleanValue("continueFinish")) {
            finish()
        }
        super.onResume()
    }

    override fun onBackPressed() {
        if (isExpanded) {
            shrinkMap()
            return
        }
        if (saveEnabled) {
            val sDialog = NokAlertDialog(this@EditEddressActivity, NokAlertDialog.WARNING)
            sDialog.setMessage("")
            sDialog.setConfirmText(getString(R.string.save_address))
            sDialog.setCancelText(getString(R.string.discard))
            sDialog.setTitle(getString(R.string.are_you_sure_leave_no_save))
                .setConfirmClickListener {
                    sDialog.hide()
                    updateAddress()
                    super.onBackPressed()
                }.setCancelClickListener { super.onBackPressed() }.show()
            return
        }
        super.onBackPressed()
    }

    override fun onMapReady(map: GoogleMap) {
        eddressMap = map
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@EditEddressActivity, R.raw.style_json))
        map.uiSettings.isZoomGesturesEnabled = false
        map.uiSettings.setAllGesturesEnabled(false)
        map.uiSettings.isScrollGesturesEnabled = false
        map.uiSettings.isRotateGesturesEnabled = false
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isCompassEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMyLocationButtonEnabled = false
        val location = LatLng(addressObject!!.lat, addressObject!!.lon)
        eddressMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(location, Utilities.zoom))
        val marker = eddressMap!!.addMarker(MarkerOptions().position(location))
        val bmp = Utilities.getMarkerPin(context, R.color.secondaryColor, 0)
        if (bmp != null) {
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp))
        }
    }

    override fun onPause() {
        if (mFusedLocationClient != null && mLocationCallback != null) mFusedLocationClient!!.removeLocationUpdates(
            mLocationCallback!!
        )
        super.onPause()
    }

    override fun onConnectionSuspended(cause: Int) {
        // Do nothing
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Log.e("Google Maps", "connection failed")
        // Do nothing
    }

    override fun onConnected(connectionHint: Bundle?) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationCallback?.let {
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                mFusedLocationClient!!.requestLocationUpdates(Utilities.REQUEST, it, null)
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
//        Toast.makeText(getContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
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
        if (closeMapButton != null) {
            closeMapButton?.visibility = View.VISIBLE
        }
        mapParent.removeView(mapView)
        mapLayout.addView(mapView, 0)
        updatePin.visibility = View.GONE
        headerView.visibility = View.GONE
    }

    fun shrinkMap() {
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
        if (closeMapButton != null) {
            closeMapButton?.visibility = View.GONE
        }
        val location = LatLng(addressObject!!.lat, addressObject!!.lon)
        eddressMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(location, Utilities.zoom))
        mapLayout.removeView(mapView)
        mapParent.addView(mapView, 0)
        updatePin.visibility = View.VISIBLE
        headerView.visibility = View.VISIBLE
    }
}
