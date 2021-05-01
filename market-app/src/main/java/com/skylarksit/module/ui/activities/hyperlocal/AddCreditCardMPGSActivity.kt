package com.skylarksit.module.ui.activities.hyperlocal

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.android.volley.Response
import com.skylarksit.module.R
import com.skylarksit.module.analytics.Segment.creditCardAdded
import com.skylarksit.module.lib.Rest
import com.skylarksit.module.libs.Json
import com.skylarksit.module.pojos.ResponseBean
import com.skylarksit.module.pojos.services.CreditCardBean
import com.skylarksit.module.pojos.services.CreditCardParam
import com.skylarksit.module.ui.model.ServicesModel
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities

class AddCreditCardMPGSActivity : MyAppCompatActivity() {

    lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card_bob)
        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.domStorageEnabled = true
        webView.addJavascriptInterface(WebAppInterface(context), "Android")
        Utilities.showSpinner(getString(R.string.access_payment_gateway))
        checkoutSession()
    }

    fun checkoutSession() {
        Rest.request().uri("addCreditCardSession").params(Json.param().toJson())
            .response(ResponseBean::class.java, Response.Listener { response: ResponseBean ->
                if (response.data != null) {
                    loadCheckoutFile(response.data.toString())
                }
            } as Response.Listener<ResponseBean>).post()
    }

    private fun loadCheckoutFile(idSession: String?) {
        val buttonColor = Utilities.getColor(context, R.color.buttonColor)
        val buttonColorFinal = "#" + buttonColor.substring(3)
        var pgUrl = ""
        when (ServicesModel.instance().paymentGateway.toUpperCase()) {
            "BOB" -> pgUrl = "checkout_bob.html"
            "AREEBA" -> pgUrl = "checkout_areeba.html"
        }
        webView.loadUrl("file:///android_asset/$pgUrl")
        val finalPgUrl = pgUrl
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                when {
                    url.contains(finalPgUrl) -> {
                        webView.loadUrl(String.format("javascript:configure('%s');", idSession))
                    }
                    url.contains("/checkout/pay/") -> {
                        webView.loadUrl(
                            "javascript:document.getElementsByClassName('submitButton')[2].innerText = 'Add Card';" +
                                    "document.getElementsByClassName('submitButton')[2].style.borderStyle = 'none';" +
                                    "document.getElementsByClassName('checkoutFooter')[0].style.display = 'none';" +
                                    "document.getElementsByClassName('cancelButton')[1].style.display = 'none';" +
                                    "document.getElementsByClassName('submitButton')[2].style.background = '" + buttonColorFinal + "';null"
                        )
                        Utilities.hideSpinner()
                    }
                    url.contains("/checkout/receipt/") -> {
                        saveCreditCardToEddressServer(idSession)
                    }
                }
            }
        }
    }

    fun saveCreditCardToEddressServer(idSession: String?) {
        val param = CreditCardParam()
        param.name = Utilities.getMyName()
        param.token = idSession
        param.email = getStringValue("email")
        Rest.request().uri("addCreditCard").params(param).showLoader(getString(R.string.saving_credit_card)).response(
            CreditCardBean::class.java, Response.Listener { response: CreditCardBean ->
                if (Utilities.notEmpty(response.uid)) {
                    model.creditCards.add(response)
                    finish()
                    creditCardAdded()
                } else {
                    Utilities.Error("Error", getString(R.string.unable_to_save_card))
                }
            } as Response.Listener<CreditCardBean>).post()
    }

    inner class WebAppInterface internal constructor(var mContext: Context) {
        @JavascriptInterface
        fun cancel() {
            finish()
        }

        @JavascriptInterface
        fun error(error: Any) {
            Utilities.hideSpinner()
            Utilities.Error(getString(R.string.add_credit_card_error), error.toString())
        }
    }
}
