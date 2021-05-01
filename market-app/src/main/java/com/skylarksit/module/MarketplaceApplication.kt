package com.skylarksit.module

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.onesignal.OneSignal
import com.skylarksit.module.analytics.Segment.identify
import com.skylarksit.module.analytics.Segment.initialize
import com.skylarksit.module.ui.model.FeedReaderDbHelper
import com.skylarksit.module.ui.model.ServicesModel
import com.skylarksit.module.ui.utils.LocalStorage
import com.skylarksit.module.utils.FontsOverride
import com.skylarksit.module.utils.Utilities
import io.branch.referral.Branch
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTimeZone
import org.joda.time.tz.UTCProvider
import java.util.*

@Suppress("UNUSED")
class MarketplaceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServicesModel.instance(applicationContext)
        DateTimeZone.setProvider(UTCProvider())
        JodaTimeAndroid.init(this) //keep here for it to work
        FontsOverride.setDefaultFont(this, "DEFAULT", "OpenSans-Regular.ttf")
        FontsOverride.setDefaultFont(this, "MONOSPACE", "OpenSans-Regular.ttf")
        FontsOverride.setDefaultFont(this, "SERIF", "OpenSans-Regular.ttf")
        FontsOverride.setDefaultFont(this, "SANS_SERIF", "OpenSans-Semibold.ttf")
        Utilities.production = !BuildConfig.DEBUG
        Utilities.jwtToken = LocalStorage.instance().getString("jwtToken")
        var url = getString(R.string.serverUrl)
        if (Utilities.isDebug()) {
            url = getString(R.string.serverUrlDebug)
        }
        Utilities.appType = getString(R.string.appType)

        // Initialize Branch automatic session tracking
        Branch.getAutoInstance(this)
        Utilities.baseUrl = url + Utilities.apiAppPath
        Utilities.baseUserUrl = url + Utilities.apiUserPath
        Utilities.currentUrl = url
        FirebaseApp.initializeApp(applicationContext)
        val config = ImagePipelineConfig.newBuilder(applicationContext)
            .setDownsampleEnabled(true)
            .build()
        Fresco.initialize(this, config)

        // Initialize Places.
        Places.initialize(applicationContext, getString(R.string.geo_api_key))
        ServicesModel.instance().singleStore = resources.getBoolean(R.bool.singleStore)
        initialize(applicationContext)
        if (Utilities.isLoggedIn()) {
            identify(LocalStorage.instance().getString("uid"))
        }
        ServicesModel.instance().dbHelper = FeedReaderDbHelper(
            Objects.requireNonNull(
                applicationContext
            )
        )
        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()
    }
}
