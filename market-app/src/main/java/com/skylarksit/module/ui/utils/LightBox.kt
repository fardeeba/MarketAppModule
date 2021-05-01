package com.skylarksit.module.ui.utils

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.google.gson.Gson
import com.skylarksit.module.R
import com.skylarksit.module.pojos.DifferedLink
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.U

class LightBox : MyAppCompatActivity() {
    lateinit var image: SimpleDraweeView
    lateinit var text: TextView
    lateinit var promoText: TextView
    lateinit var actionButton: Button
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lightbox)
        setFinishOnTouchOutside(false)
        image = findViewById(R.id.image)
        text = findViewById(R.id.text)
        promoText = findViewById(R.id.promoText)
        actionButton = findViewById(R.id.actionButton)
        val linkString = intent.getStringExtra("linkData")
        try {
            if (linkString != null) {
                val link = Gson().fromJson(linkString, DifferedLink::class.java)
                text.text = link.description
                if (link.imageUrl != null) {
                    image.setImageURI(U.parse(link.imageUrl))
                } else {
                    image.visibility = View.GONE
                }
                if (link.promoCode != null) {
                    promoText.text = link.promoCode
                    promoText.visibility = View.VISIBLE
                } else {
                    promoText.visibility = View.GONE
                }
                if (link.actionLabel != null) {
                    actionButton.text = link.actionLabel
                } else {
                    if (link.type == DifferedLink.DifferedLinkType.PROMO) actionButton.text = getString(R.string.redeem) else if (link.type == DifferedLink.DifferedLinkType.NOTIFICATION) {
                        actionButton.text = getString(R.string.got_it)
                    }
                }
                actionButton.setOnClickListener(object : OnOneOffClickListener() {
                    override fun onSingleClick(v: View) {
                        val data = Intent()
                        if (link.serviceName != null) {
                            data.putExtra("serviceName", link.serviceName)
                        } else if (link.serviceType != null) {
                            data.putExtra("serviceType", link.serviceType)
                        }
                        if (link.promoCode != null) {
                            data.putExtra("promoCode", link.promoCode)
                        }
                        setResult(RESULT_OK, data)
                        finish()
                    }
                })
            }
        } catch (ignored: Exception) {
        }
    }
}
