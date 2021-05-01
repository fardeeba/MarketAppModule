@file:Suppress("UNUSED_PARAMETER")

package com.skylarksit.module.ui.activities.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.skylarksit.module.MainActivity
import com.skylarksit.module.R
import com.skylarksit.module.ui.activities.hyperlocal.PurchaseOrdersActivity
import com.skylarksit.module.ui.activities.settings.SettingsActivity
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities

class ProfileActivity : MyAppCompatActivity() {
    lateinit var walletAmountTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_layout)
        walletAmountTextView = findViewById(R.id.wallet_amount)
        if (model.amountInWallet != null) {
            walletAmountTextView.text = Utilities.formatCurrency(
                model.amountInWallet,
                model.tenantCurrency,
                model.tenantCurrencyHideDecimals
            )
        }
    }

    override fun onStart() {
        if (!Utilities.isLoggedIn()) {
            this@ProfileActivity.startActivity(Intent(context, MainActivity::class.java))
        }
        super.onStart()
    }


    fun openSettings(view: View?) {
        val settingsActivity = Intent(context, SettingsActivity::class.java)
        this@ProfileActivity.startActivity(settingsActivity)
    }

    fun openMyAddresses(view: View?) {
        val myAddressesActivity = Intent(context, MyAddressesActivity::class.java)
        this@ProfileActivity.startActivity(myAddressesActivity)
    }

    fun logout(view: View?) {
        Utilities.logout()
    }

    fun openPreviousOrders(view: View?) {
        val orderHistoryActivity = Intent(context, PurchaseOrdersActivity::class.java)
        this@ProfileActivity.startActivity(orderHistoryActivity)
    }

    fun onCallSupportClick(v: View?) {
        val number = Uri.parse("tel:" + model.supportPhoneNumber)
        try {
            val callIntent = Intent(Intent.ACTION_CALL, number)
            startActivity(callIntent)
        } catch (e: SecurityException) {
            val callIntent = Intent(Intent.ACTION_DIAL, number)
            startActivity(callIntent)
        }
    }
}
