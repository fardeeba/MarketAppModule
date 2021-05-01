@file:Suppress("DEPRECATION")

package com.skylarksit.module

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fenchtose.tooltip.Tooltip
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.skylarksit.module.analytics.Segment.locationReceived
import com.skylarksit.module.lib.ErrorResource
import com.skylarksit.module.pojos.Address
import com.skylarksit.module.pojos.AddressObject
import com.skylarksit.module.pojos.CollectionData
import com.skylarksit.module.pojos.DifferedLink
import com.skylarksit.module.pojos.services.MenuItemObject
import com.skylarksit.module.ui.components.NavigationBar
import com.skylarksit.module.ui.lists.hyperlocal.adapters.HomePageAdapter
import com.skylarksit.module.ui.model.TooltipsManager
import com.skylarksit.module.ui.model.ViewRouter
import com.skylarksit.module.ui.utils.IClickListener
import com.skylarksit.module.ui.utils.LightBox
import com.skylarksit.module.ui.utils.LocalStorage
import com.skylarksit.module.ui.utils.OnOneOffClickListener
import com.skylarksit.module.utils.MLinearLayoutManger
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Services
import com.skylarksit.module.utils.Services.ServiceResult
import com.skylarksit.module.utils.Utilities
import java.io.IOException
import java.util.*

class MainActivity : MyAppCompatActivity(), INavBarActivity {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var searchLayout: View
    private lateinit var nonCurvedView: View
    lateinit var homePageRecycleView: RecyclerView
    private lateinit var changeLocationText: TextView
    lateinit var headerView: View
    lateinit var navigationBar: NavigationBar
    lateinit var changeLocationLayout: View
    private var backCounter = 0
    private var currentArea: String? = null
    private var geocoder: Geocoder? = null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_UPDATE_ORDER == intent.action) {
                navigationBar.displayHistoryBubble()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val listStatusBar = context.resources.getBoolean(R.bool.homePageStatusBarLight)
        if (listStatusBar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setLightStatusBar(this@MainActivity)
        }
        changeLocationLayout = findViewById(R.id.changeLocationLayout)
        navigationBar = findViewById(R.id.navigationBar)
        headerView = findViewById(R.id.headerView)
        changeLocationText = findViewById(R.id.changeLocationText)
        homePageRecycleView = findViewById(R.id.recyclerView)
        nonCurvedView = findViewById(R.id.nonCurvedView)
        searchLayout = findViewById(R.id.searchButton)
        geocoder = Geocoder(context, Locale.getDefault())
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateLocation(LatLng(location.latitude, location.longitude))
                    stopLocationUpdates()
                    break
                }
            }
        }
        initHomePageItems()
        Utilities.hideKeyboard(this)
        LocalStorage.instance().save(context, "tutorialComplete", true)
        changeLocationLayout.setOnClickListener(object : OnOneOffClickListener() {
            override fun onSingleClick(v: View) {
                changeYourLocation()
            }
        })
        searchLayout.setOnClickListener(object : OnOneOffClickListener() {
            override fun onSingleClick(v: View) {
                onSearchClick()
            }
        })
        navigationBar.init(this, null)
    }

    public override fun onResume() {
        super.onResume()
        backCounter = 0
        val filter = IntentFilter()
        filter.addAction(ACTION_UPDATE_ORDER)
        registerReceiver(receiver, filter)
        if (Utilities.searchResults != null) {
            Utilities.searchResults.clear()
        }
        if (model.setAsCurrentAddress != null) {
            updateCurrentAddress(model.setAsCurrentAddress, true)
            model.setAsCurrentAddress = null
        } else {
            updateCart()
            if (model.services.size > 0) {
                Utilities.hideSpinner()
            }
        }
        if ("current location".equals(model.changeLocationText, ignoreCase = true) && model.currentAddress != null) {
            updateCurrentAddress(model.currentAddress, true)
        }
        if (!model.servicesLoaded()) {
            showSpinner()
            val permission = getStringValue("permission")
            val permissionDenied = ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
            if ((!permissionDenied || Utilities.isEmpty(permission)) && ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Utilities.FINE_LOCATION_PERMISSION
                )
                return
            }
            when {
                Utilities.isLoggedIn() -> {
                    checkCredibility()
                }
                permissionDenied -> {
                    loadMarket(true)
                }
                else -> {
                    initLocationProcess()
                }
            }
        } else if (Utilities.isLoggedIn() && !model.credibilityChecked) {
            checkCredibility()
        }
        if (model.favoritesHomeModified) {
            model.favoritesHomeModified = false
            homePageAdapter!!.notifyDataSetChanged()
        }

        Services.call(this@MainActivity).getInventory(null)
        if (getBooleanValue("reloadServices")) {
            saveValue("reloadServices", false)
            loadMarket(true)
        }
    }

    private fun checkCredibility() {
        Services.call(this@MainActivity).result(object : ServiceResult<Any?> {
            override fun onResult(result: Any?) {
                initLocationProcess()
            }

            override fun onError(errorResource: ErrorResource) {}
        }).checkCredibility(this@MainActivity)
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    var homePageAdapter: HomePageAdapter? = null
    private fun initHomePageItems() {
        homePageAdapter = HomePageAdapter(this@MainActivity, model.homePageItems,
            withHeader = false,
            withFooter = false
        )
        homePageRecycleView.setHasFixedSize(true)
        homePageRecycleView.setItemViewCacheSize(100)
        homePageRecycleView.isDrawingCacheEnabled = true
        homePageRecycleView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        homePageRecycleView.adapter = homePageAdapter
        homePageAdapter!!.setUpdateCartListener(object : IClickListener<MenuItemObject> {
            override fun click(`object`: MenuItemObject?) {
                updateCart()
            }
        })
        val layoutManager = MLinearLayoutManger(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        homePageRecycleView.layoutManager = layoutManager
        updateCart()
    }

    fun initLocationProcess() {
        if (model.currentAddress != null || model.servicesLoaded()) return
        loadCurrentAddressFromLocalStorage()
        if (model.currentAddress != null) {
            updateLocationField(model.currentAddress.label)
            if (!model.servicesLoaded()) {
//                loadMarket(!model.servicesLoaded());
                checkIfGpsIsCloseToCurrentLocation()
            } else {
                Utilities.hideSpinner()
            }
        } else {
            updateLocationField(getString(R.string.fetching_location))
            if (Utilities.checkGps(this@MainActivity, object : IClickListener<Any> {
                    override fun click(`object`: Any?) {
                        loadMarket(false)
                    }
                })) {
                startLocationUpdates()
            } else {
                Utilities.hideSpinner()
            }
        }
    }

    private fun loadCurrentAddressFromLocalStorage() {
        if (model.currentAddress != null || model.servicesLoaded()) {
            return
        }
        val currentEddress = LocalStorage.instance().getString("currentAddress")
        model.currentAddress = model.findAddress(currentEddress)
    }

    private fun updateLocationField(text: String?) {
        model.changeLocationText = text
        changeLocationText.text = model.changeLocationText
    }

    private fun checkIfGpsIsCloseToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mFusedLocationClient!!.lastLocation.addOnSuccessListener(this) { location: Location? ->
                if (location != null && model.currentAddress != null) {
                    //check if you have another close address... and cart is not used
                    val addressObject = model.findClosestLocation(location)
                    if (addressObject != null && addressObject !== model.currentAddress) {
                        if (!model.hasSavedCart()) {
                            updateCurrentAddress(addressObject, false)
                            model.changeLocationTooltip = R.string.tip_location_modified
                            TooltipsManager.instance().showTooltip(
                                this@MainActivity,
                                changeLocationLayout,
                                model.changeLocationTooltip,
                                Tooltip.BOTTOM,
                                3
                            )
                        } else {
                            showAddressPopup(R.string.confirm_address)
                        }
                    } else if (location.distanceTo(model.currentAddress.location) > 100) {
                        showAddressPopup(R.string.we_could_not_find_close_address)
                    }
                } else {
                    model.changeLocationTooltip = R.string.tip_location_not_found
                    TooltipsManager.instance().showTooltip(
                        this@MainActivity,
                        changeLocationLayout,
                        model.changeLocationTooltip,
                        Tooltip.BOTTOM,
                        3
                    )
                }
                loadMarket(true)
            }.addOnFailureListener {
                model.changeLocationTooltip = R.string.tip_location_not_found
                TooltipsManager.instance().showTooltip(
                    this@MainActivity,
                    changeLocationLayout,
                    model.changeLocationTooltip,
                    Tooltip.BOTTOM,
                    3
                )
                loadMarket(true)
            }
        }
    }

    fun changeYourLocation() {
        if (Utilities.isLoggedIn()) {
            showAddressPopup(R.string.please_select_delivery)
        } else {
            Utilities.alertSignInFirst(this)
        }
    }

    private fun showAddressPopup(@StringRes text: Int) {
        ViewRouter.instance().showAddressPopup(
            this@MainActivity,
            text,
            null,
            false,
            true,
            object : IClickListener<AddressObject?> {
                override fun click(`object`: AddressObject?) {
                    updateCurrentAddress(`object`, true)
                }
            })
    }

    fun updateCart() {
        navigationBar.refreshView()
    }

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    fun updateDeliveryTimeText() {
        if (!model.singleStore || model.services == null || model.services.size == 0) {
            return
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Utilities.CAMERA_READ_EXTERNAL_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.CAMERA),
                            Utilities.CAMERA_PERMISSION
                        )
                    } else {
                        startActivityForResult(Utilities.getIntentForCam(this@MainActivity), 0)
                    }
                }
            }
            Utilities.CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(Utilities.getIntentForCam(this@MainActivity), 0)
                }
            }
            Utilities.FINE_LOCATION_PERMISSION -> {
                saveValue("permission", "permission")
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) {
            if (model.currentAddress != null) {
                updateLocationField(model.currentAddress.label)
            }
            return
        }
        when (requestCode) {
            LIGHT_BOX -> {
                val serviceName = data!!.getStringExtra("serviceName")
                var serviceUid: String? = null
                if (serviceName != null) {
                    val service = model.findProviderByName(serviceName)
                    if (service != null) {
                        serviceUid = service.serviceProviderUid
                    }
                }
                val promoCode = data.getStringExtra("promoCode")
                if (promoCode != null) {
                    if (serviceUid == null) model.applyPromo(this, promoCode, null, true) else {
                        ViewRouter.instance().goToServiceProvider(
                            this,
                            serviceName,
                            null,
                            promoCode,
                            CollectionData("banner_promo", promoCode, CollectionData.Type.DEEP_LINK)
                        )
                    }
                }
                if (serviceUid != null) {
                    ViewRouter.instance().goToServiceProvider(
                        this,
                        serviceName,
                        null,
                        null,
                        CollectionData("banner_service", serviceName!!, CollectionData.Type.DEEP_LINK)
                    )
                }
            }
            ViewRouter.IC_ADDRESS_PICKED -> {
                val addressCode = data!!.getStringExtra("addressCode")
                model.setAsCurrentAddress = model.findAddress(addressCode)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        if (navigationBar.onClearSearch()) {
            return
        }
        if (backCounter == 0) {
            Utilities.Toast("Press back one more time to exit!")
            backCounter++
            return
        }
        if (!isFinishing) super.onBackPressed()
    }

    private var statusTimer: CountDownTimer? = null

    private fun stopActiveOrdersTimer() {
        if (statusTimer != null) {
            statusTimer!!.cancel()
            statusTimer = null
        }
    }


    public override fun onPause() {
        unregisterReceiver(receiver)
        stopActiveOrdersTimer()
        super.onPause()
    }

    private fun stopLocationUpdates() {
        mFusedLocationClient!!.removeLocationUpdates(mLocationCallback!!)
    }

    private var mLocationCallback: LocationCallback? = null

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mFusedLocationClient!!.requestLocationUpdates(Utilities.REQUEST, mLocationCallback, null)
        //        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
//
//        }).addOnFailureListener(e -> {
//            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//            mFusedLocationClient.requestLocationUpdates(Utilities.REQUEST, mLocationCallback, null);
//        });
    }

    fun updateCurrentAddress(item: AddressObject?, loadMarket: Boolean) {
        if (item == null) return
        updateLocationField(item.label)
        if (model.currentAddress !== item) {
            model.currentAddress = item
            LocalStorage.instance().save(context, "currentAddress", item.code)
            if (loadMarket) loadMarket(true)
        }
    }

    fun updateLocation(latLng: LatLng?) {
        if (latLng != null && latLng.latitude != 0.0 && latLng.longitude != 0.0) {
            getAreaFromGoogle(latLng)
        } else {
            Utilities.hideSpinner()
            updateLocationField(getString(R.string.unknown_location))
            changeYourLocation()
        }
    }

    private fun getAreaFromGoogle(latLng: LatLng?) {
        if (latLng == null) return

//        String key = getString(R.string.geo_api_key);
//        String query = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" +latLng.latitude + "," + latLng.longitude + "&key="+ key +"&sensor=true";
        model.currentLocation = latLng
        try {
            val addresses = geocoder!!.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses.size > 0) {
                val address = Address(addresses[0], latLng.latitude, latLng.longitude)
                locationReceived(address)
                currentArea = address.locality
                if (!Utilities.isEmpty(currentArea)) updateLocationField(currentArea) else {
                    updateLocationField(getString(R.string.unknown_location))
                }
                loadMarket(true)
            }
        } catch (ignored: IOException) {
            updateLocationField(getString(R.string.unknown_location))
            loadMarket(true)
        }

//        StringRequest stringRequest = new StringRequest(Request.Method.GET, query,
//                response -> {
//                    try {
//                        JSONObject obj = new JSONObject(response);
//                        Address location = Utilities.getAddress(context, obj, latLng.latitude, latLng.longitude);
//                        Segment.INSTANCE.locationReceived(location);
//
//                        currentArea = Utilities.findLocality(obj);
//                        if (!Utilities.isEmpty(currentArea))
//                            updateLocationField(currentArea);
//                        else{
//                            updateLocationField(getString(R.string.unknown_location));
//                        }
//                        loadMarket(true);
//                    } catch (JSONException ignored) {
//                        updateLocationField(getString(R.string.unknown_location));
//                    }
//                }, error -> {
//                     updateLocationField(getString(R.string.unknown_location));
//                    loadMarket(true);
//                });
//        RestClient.getInstance(context).addToRequestQueue(stringRequest);
    }

    private fun calculatePromiseTime() {
        if (model.calculatePromiseTime && model.singleStore && model.currentAddress != null) {
            val serviceObject = model.singleStoreService
            if (serviceObject != null) {
                Services.call(this@MainActivity).result(object : ServiceResult<Any?> {
                    override fun onResult(result: Any?) {
                        updateDeliveryTimeText()
                    }

                    override fun onError(errorResource: ErrorResource) {
                        updateDeliveryTimeText()
                    }
                }).calculatePromiseTime(serviceObject.id, model.currentAddress.lat, model.currentAddress.lon)
            }
        } else {
            updateDeliveryTimeText()
        }
    }

    private var loadingMarket = false
    private fun loadMarket(showLoader: Boolean) {
        if (loadingMarket) return
        if (showLoader) {
            Utilities.showSpinner()
        }
        loadingMarket = true
        appServices
    }

    //                swipeRefresh.setRefreshing(false);
    private val appServices: Unit
        get() {
            Services.call(this@MainActivity).result(object : ServiceResult<Boolean?> {
                override fun onError(errorResource: ErrorResource) {
//                swipeRefresh.setRefreshing(false);
                    Utilities.hideSpinner()
                    loadingMarket = false
                }

                override fun onResult(result: Boolean?) {
                    loadingMarket = false
                    homePageAdapter!!.notifyDataSetChanged()
                    if (model.services.isNotEmpty()) {
                        consumeDifferedLink()
                    }

                    Services.call(this@MainActivity).result(object : ServiceResult<Any?> {
                        override fun onResult(result: Any?) {
                            updateCart()
                        }

                        override fun onError(errorResource: ErrorResource) {}
                    }).getInventory(null)
                    updateCart()
                    updateDeliveryTimeText()
                    if (model.currentAddress == null) {
                        if (model.myLocations.isEmpty()) {
                            model.changeLocationTooltip = R.string.tip_select_location
                            TooltipsManager.instance().showTooltip(
                                this@MainActivity,
                                changeLocationLayout,
                                model.changeLocationTooltip,
                                Tooltip.BOTTOM,
                                3
                            )
                        } else {
                            showAddressPopup(R.string.please_select_delivery)
                        }
                    }
                    calculatePromiseTime()
                    Utilities.hideSpinner()
                }
            }).loadMarketPlace()
        }

    private fun consumeDifferedLink() {
        val link = model.differedLink
        if (link != null) {
            if (link.type == DifferedLink.DifferedLinkType.PROMO) {
                if (link.description != null && link.description.isNotEmpty()) {
                    val intent = Intent(this, LightBox::class.java)
                    intent.putExtra("linkData", Gson().toJson(link))
                    startActivityForResult(intent, LIGHT_BOX)
                } else if (link.serviceName != null && link.serviceName.isNotEmpty()) {
                    ViewRouter.instance().goToServiceProvider(
                        this,
                        link.serviceName,
                        null,
                        null,
                        CollectionData("deep_link", link.serviceName, CollectionData.Type.DEEP_LINK)
                    )
                }
            } else if (link.type == DifferedLink.DifferedLinkType.ORDER) {
                ViewRouter.instance().openOrderDetails(this@MainActivity, link.data)
            } else if (link.type == DifferedLink.DifferedLinkType.NOTIFICATION) {
                val intent = Intent(this, LightBox::class.java)
                intent.putExtra("linkData", Gson().toJson(link))
                startActivityForResult(intent, LIGHT_BOX)
            }
            model.differedLink = null
        }
    }

    override fun getSearchView(): View {
        return searchLayout
    }

    fun onSearchClick() {
        navigationBar.onSearchClick()
    }

    companion object {
        const val ACTION_UPDATE_ORDER = "UPDATE_ORDER"

        //    public void profileButtonClicked() {
        //        if (Utilities.isLoggedIn()) {
        //            startActivity(new Intent(context, ProfileActivity.class));
        //        } else {
        //            Utilities.alertSignInFirst(this, true);
        //        }
        //    }
        //    @OnClick(R2.id.favoritesButton)
        //    public void favoritesButtonClicked() {
        //        startActivity(new Intent(context, FavoritesActivity.class));
        //    }
        const val LIGHT_BOX = 654
    }
}
