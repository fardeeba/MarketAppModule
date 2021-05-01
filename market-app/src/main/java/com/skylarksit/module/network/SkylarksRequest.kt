@file:Suppress("UNUSED_PARAMETER")

package com.skylarksit.module.network

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.skylarksit.module.lib.RestClient
import com.skylarksit.module.utils.Utilities
import org.json.JSONObject
import java.util.*

class SkylarksRequest(
    context: Context?,
    methodName: String,
    apiPath: String,
    params: JSONObject?,
    listener: Response.Listener<JSONObject?>?,
    errorListener: Response.ErrorListener?
) : JsonObjectRequest(
    Method.POST, "$apiPath$methodName/", params ?: JSONObject(), listener, errorListener
) {
    constructor(
        context: Context?,
        methodName: String,
        params: JSONObject?,
        listener: Response.Listener<JSONObject?>?,
        errorListener: Response.ErrorListener?
    ) : this(context, methodName, Utilities.currentUrl + Utilities.apiAppPath, params, listener, errorListener) {
        this.context = context
    }

    var context: Context? = null
    private var token: String? = null
    override fun getHeaders(): Map<String, String> {
        val params = HashMap<String, String>()
        if (token == null) {
            token = Utilities.jwtToken
            if (token == null || token!!.isEmpty()) {
                token = Utilities.jwtToken
            }
        }
        params["Authorization"] = "Bearer $token"
        return params
    }

    override fun getBodyContentType(): String {
        return "application/json"
    }

    fun post() {
        RestClient.getInstance(context).addToRequestQueue(this)
    }

    init {
        retryPolicy = DefaultRetryPolicy(180000, 0, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS.toFloat())
    }
}
