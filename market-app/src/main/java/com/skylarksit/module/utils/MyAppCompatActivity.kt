@file:Suppress("DEPRECATION")

package com.skylarksit.module.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fenchtose.tooltip.Tooltip
import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import com.skylarksit.module.MainActivity
import com.skylarksit.module.R
import com.skylarksit.module.pojos.geocode_response.GeoCodeResponse
import com.skylarksit.module.ui.model.ServicesModel
import com.skylarksit.module.ui.model.TooltipsManager
import com.skylarksit.module.ui.utils.LocalStorage.Companion.instance
import java.util.*

abstract class MyAppCompatActivity : AppCompatActivity() {
    protected open var model: ServicesModel = ServicesModel.instance()
    protected lateinit var context: Context
    private var showStatusBar = true
    override fun onResume() {
        model.currentActivity = this
        val hoursAgo = Calendar.getInstance()
        hoursAgo.add(Calendar.MINUTE, -90)
        if (model.lastSyncDate != null && model.lastSyncDate.before(hoursAgo.time)) {
            saveValue("returnToMainView", true)
            saveValue("reloadServices", true)
        }
        if (getBooleanValue("returnToMainView")) {
            if (this is MainActivity) {
                saveValue("returnToMainView", false)
            } else finish()
        }
        if (getBooleanValue("continueFinish")) {
            (this as? MainActivity)?.saveValue("continueFinish", false)
        }
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (showStatusBar) {
            setStatusBarGradiant(this)
        }
        super.onCreate(savedInstanceState)
        context = baseContext
        Utilities.init(model.appContext, windowManager.defaultDisplay)
        ServicesModel.instance().currentActivity = this
        TooltipsManager.instance().clear()
        Log.d("Activity", javaClass.simpleName)
    }

    protected fun addTooltip(anchor: View?, @StringRes resId: Int, position: Int) {
        TooltipsManager.instance().add(this, anchor!!, resId, position, 2, 0)
    }

    fun addTooltip(anchor: View?, @StringRes resId: Int) {
        TooltipsManager.instance().add(this, anchor!!, resId, Tooltip.BOTTOM, 2, 0)
    }

    private fun startTooltips() {
        TooltipsManager.instance().start(context)
    }

    protected fun showSpinner() {
        runOnUiThread { Utilities.showSpinner("Loading...") }
    }

    override fun onStart() {
        super.onStart()
        startTooltips()
    }

    fun getStringValue(key: String?): String {
        return instance().getString(key)
    }

    fun getBooleanValue(key: String?): Boolean {
        return instance().getBoolean(key)
    }

    fun saveValue(key: String?, value: Any?) {
        instance().save(context!!, key, value)
    }

    fun getAreaNameOf(location: LatLng) {
        var url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&sensor=true&key=%s"
        url = String.format(url, location.latitude, location.longitude, getString(R.string.geo_api_key))
        Utilities.showSpinner("")
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.GET, url, Response.Listener { response: String? ->
                val gson = GsonBuilder().create()
                val geoCodeResponse = gson.fromJson(response, GeoCodeResponse::class.java)
                for (result in geoCodeResponse.results) {
                    for (addressComponent in result.addressComponents) {
                        for (type in addressComponent.types) {
                            if (type == "neighborhood" || type == "sublocality" || type == "administrative_area_level_3") {
                                val area = result.formattedAddress.split(",".toRegex()).toTypedArray()
                                if (area.isNotEmpty()) {
                                    Utilities.hideSpinner()
                                    onGeocodeResult(location, area[0])
                                    return@Listener
                                }
                            }
                        }
                    }
                }
                Utilities.hideSpinner()
                onGeocodeResult(location, "")
            }, { error: VolleyError ->
                Utilities.hideSpinner()
                error.printStackTrace()
            })
        queue.add(stringRequest)
    }

    open fun onGeocodeResult(location: LatLng, area: String) {}

    companion object {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun setStatusBarGradiant(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = activity.window
                val background = activity.resources.getDrawable(R.drawable.gradient_background)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = activity.resources.getColor(R.color.statusBarTransparent)
                window.setBackgroundDrawable(background)
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        fun setLightStatusBar(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = activity.window
                val background = activity.resources.getDrawable(R.color.background)
                var flags = activity.window.decorView.systemUiVisibility // get current flag
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // add LIGHT_STATUS_BAR to flag
                activity.window.decorView.systemUiVisibility = flags
                window.statusBarColor = activity.resources.getColor(R.color.background)
                window.setBackgroundDrawable(background)
            }
        }
    }
}
