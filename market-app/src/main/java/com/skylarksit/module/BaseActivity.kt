package com.skylarksit.module

import android.os.Bundle
import android.util.Log
import com.skylarksit.module.gcm.MyFcmListenerService
import com.skylarksit.module.pojos.DifferedLink
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.BranchError
import io.branch.referral.util.LinkProperties

class BaseActivity : MyAppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)
        model.homePageAlignCenter = resources.getBoolean(R.bool.homePageAlignCenter)
    }

    override fun onStart() {
        super.onStart()
        val branch = Branch.getInstance()
        branch.initSession({ branchUniversalObject: BranchUniversalObject?, _: LinkProperties?, error: BranchError? ->
            if (error == null && branchUniversalObject != null) {
                Log.i("BranchTestBed", "referring Branch Universal Object: $branchUniversalObject")
                val obj = DifferedLink()
                val customMetadata = branchUniversalObject.contentMetadata.customMetadata
                val campaignIdentifier = customMetadata["compaign_identifier"]
                if ("INVITE".equals(campaignIdentifier, ignoreCase = true)) {
                    saveValue("referringUserUid", customMetadata["referringUserUid"])
                    saveValue("referringBonus", customMetadata["amount"])
                    saveValue("bannerUid", customMetadata["bannerUid"])
                } else {
                    obj.serviceName = customMetadata["serviceName"]
                    obj.serviceType = customMetadata["serviceType"]
                    obj.description = customMetadata["description"]
                    obj.promoCode = customMetadata["promoCode"]
                    obj.productSku = customMetadata["productSku"]
                    obj.imageUrl = customMetadata["imageUrl"]
                    obj.actionLabel = customMetadata["actionLabel"]
                    obj.type = DifferedLink.DifferedLinkType.PROMO
                    obj.branchIdentifier = customMetadata["~id"]
                }
                model.differedLink = obj
            } else {
                createDeferredLinks()
            }
            Utilities.startMainActivity(this@BaseActivity)
        }, this.intent.data, this)
    }

    private fun createDeferredLinks() {
        val intent = this@BaseActivity.intent
        val type = intent.getStringExtra("type")
        val data = intent.getStringExtra("data")
        val uri = intent.data
        var uriPath: String? = null
        if (uri != null) uriPath = uri.encodedPath
        var link: DifferedLink? = null
        if (type != null) {
            if (type == MyFcmListenerService.ORDERS) {
                link = DifferedLink()
                link.data = data
                link.type = DifferedLink.DifferedLinkType.ORDER
            } else if (type == MyFcmListenerService.SKYLARKS) {
                link = DifferedLink()
                link.data = data
                link.type = DifferedLink.DifferedLinkType.EDDRESS
            }
        } else if (uriPath != null) {
            if (uriPath.contains("code")) {
                val lastSegment = uri!!.lastPathSegment
                if (lastSegment != null) {
                    link = DifferedLink()
                    link.type = DifferedLink.DifferedLinkType.EDDRESS
                    link.data = lastSegment
                }
            }
        }
        model.differedLink = link
    }
}
