@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.activities.hyperlocal

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.RatingBar.OnRatingBarChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.JsonObject
import com.skylarksit.module.MainActivity
import com.skylarksit.module.R
import com.skylarksit.module.analytics.Segment.couponRemoved
import com.skylarksit.module.analytics.Segment.driverContacted
import com.skylarksit.module.analytics.Segment.orderCanceled
import com.skylarksit.module.analytics.Segment.supportContacted
import com.skylarksit.module.lib.Rest
import com.skylarksit.module.libs.Json
import com.skylarksit.module.libs.alertdialog.NokAlertDialog
import com.skylarksit.module.pojos.AddressObject
import com.skylarksit.module.pojos.CollectionData
import com.skylarksit.module.pojos.ResponseBean
import com.skylarksit.module.pojos.services.*
import com.skylarksit.module.pojos.services.Currency
import com.skylarksit.module.ui.lists.main.adapters.BasketAdapter
import com.skylarksit.module.ui.model.BasketItem
import com.skylarksit.module.ui.model.IProductItem
import com.skylarksit.module.ui.model.ViewRouter
import com.skylarksit.module.ui.utils.FeedbackPopup
import com.skylarksit.module.ui.utils.FeedbackType
import com.skylarksit.module.ui.utils.ItemClickListener
import com.skylarksit.module.ui.utils.OnOneOffClickListener
import com.skylarksit.module.utils.*
import java.text.NumberFormat
import java.util.*

class OrderCompletedActivity : MyAppCompatActivity(), OnMapReadyCallback {
    private var bounds: LatLngBounds? = null
    private var boundsSize = 0
    private var eddressMap: GoogleMap? = null
    private var mapFragment: MapFragment? = null


    private lateinit var mapParent: RelativeLayout
    private lateinit var mapView: View
    private lateinit var voucherView: View
    private lateinit var barcodeText: TextView
    private lateinit var serialNumberText: TextView
    private lateinit var mapLayout: RelativeLayout
    lateinit var headerView: View
    lateinit var scrollView: ScrollView
    private lateinit var statusLayout: LinearLayout
    private lateinit var contactUs: Button
    private lateinit var phoneImage: ImageView
    private lateinit var closeMapButton: ImageButton
    private lateinit var newOrderImg: ImageView
    private lateinit var cancelImage: ImageView
    private lateinit var confirmedImage: ImageView
    private lateinit var pickupImage: ImageView
    private lateinit var deliverImage: ImageView
    private lateinit var newOrderText: TextView
    private lateinit var newOrderDate: TextView
    private lateinit var confirmedDate: TextView
    private lateinit var confirmedText: TextView
    private lateinit var pickupText: TextView
    private lateinit var pickupDate: TextView
    private lateinit var pickupElapsed: TextView
    private lateinit var confirmedElapsed: TextView
    private lateinit var deliverElapsed: TextView
    private lateinit var returnElapsed: TextView
    private lateinit var deliverDate: TextView
    private lateinit var etaLabel: TextView
    lateinit var eta: TextView
    private lateinit var etaLayout: View
    private lateinit var driverSection: RelativeLayout
    private lateinit var driverName: TextView
    private lateinit var driverImage: ImageView
    private lateinit var returnImage: ImageView
    private lateinit var pickupLayout: View
    private lateinit var deliverLayout: View
    private lateinit var confirmedLayout: View
    private lateinit var cancelOrderButton: View
    private lateinit var returnLayout: View
    private lateinit var deliverText: TextView
    private lateinit var returnText: TextView
    private lateinit var cancelText: TextView
    private lateinit var cancelDate: TextView
    private lateinit var returnDate: TextView
    private lateinit var cancelLayout: View
    private lateinit var notesText: TextView
    private lateinit var notesLayout: View
    private lateinit var restaurantFeedbackView: RelativeLayout
    private lateinit var restaurantRatingBar: RatingBar
    private lateinit var feedbackLayout: View
    lateinit var recyclerView: RecyclerView
    private var formatter: NumberFormat? = null
    private var pickupEddress: AddressObject? = null
    private var deliveryEddress: AddressObject? = null
    var orderUid: String? = null
    private var trackDriverLocation = true
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val order = intent.getStringExtra("orderUid")
            if (order != null && order == orderUid && !"vouchers".equals(
                    model.activePurchaseOrder.serviceSlug,
                    ignoreCase = true
                )
            ) {
                reloadPurchaseOrder(orderUid!!)
            }
        }
    }
    private var initialMapHeight = 0
    private var isInitialized = false
    private var isExpanded = false
    private var applyColorFilter = false
    private var discountValue: Double? = null
    private var discountDescription: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_completed)
        recyclerView = findViewById(R.id.recyclerView)
        mapParent = findViewById(R.id.mapParent)
        mapView = findViewById(R.id.mapView)
        voucherView = findViewById(R.id.voucherView)
        barcodeText = findViewById(R.id.barcodeText)
        serialNumberText = findViewById(R.id.serialNumberText)
        mapLayout = findViewById(R.id.mapLayout)
        headerView = findViewById(R.id.headerView)
        scrollView = findViewById(R.id.scrollView)
        statusLayout = findViewById(R.id.statusLayout)
        contactUs = findViewById(R.id.contactUs)
        phoneImage = findViewById(R.id.phoneImage)
        closeMapButton = findViewById(R.id.closeButton)
        newOrderImg = findViewById(R.id.newOrderImg)
        cancelImage = findViewById(R.id.cancelImage)
        confirmedImage = findViewById(R.id.confirmedImage)
        pickupImage = findViewById(R.id.pickupImage)
        deliverImage = findViewById(R.id.deliverImage)
        newOrderText = findViewById(R.id.newOrderText)
        newOrderDate = findViewById(R.id.newOrderDate)
        confirmedDate = findViewById(R.id.confirmedDate)
        pickupText = findViewById(R.id.pickupText)
        confirmedText = findViewById(R.id.confirmedText)
        pickupDate = findViewById(R.id.pickupDate)
        feedbackLayout = findViewById(R.id.feedbackLayout)
        restaurantRatingBar = findViewById(R.id.restaurantRatingBar)
        restaurantFeedbackView = findViewById(R.id.restaurantFeedbackLayout)
        notesLayout = findViewById(R.id.notesLayout)
        notesText = findViewById(R.id.notesText)
        cancelLayout = findViewById(R.id.cancelLayout)
        returnDate = findViewById(R.id.returnDate)
        cancelDate = findViewById(R.id.cancelDate)
        cancelText = findViewById(R.id.cancelText)
        returnText = findViewById(R.id.returnText)
        deliverText = findViewById(R.id.deliverText)
        returnLayout = findViewById(R.id.returnLayout)
        cancelOrderButton = findViewById(R.id.cancelOrderButton)
        confirmedLayout = findViewById(R.id.confirmedLayout)
        deliverLayout = findViewById(R.id.deliverLayout)
        pickupLayout = findViewById(R.id.pickupLayout)
        returnImage = findViewById(R.id.returnImage)
        driverImage = findViewById(R.id.driverImage)
        driverName = findViewById(R.id.driverName)
        driverSection = findViewById(R.id.driverSection)
        etaLayout = findViewById(R.id.etaLayout)
        eta = findViewById(R.id.eta)
        etaLabel = findViewById(R.id.etaLabel)
        deliverDate = findViewById(R.id.deliverDate)
        returnElapsed = findViewById(R.id.returnElapsed)
        deliverElapsed = findViewById(R.id.deliverElapsed)
        confirmedElapsed = findViewById(R.id.confirmedElapsed)
        pickupElapsed = findViewById(R.id.pickupElapsed)

        phoneImage.setOnClickListener { callDriver() }
        cancelOrderButton.setOnClickListener { cancelOrder() }
        closeMapButton.setOnClickListener { shrinkMap() }
        etaLayout.setOnClickListener { onETALayout() }
        contactUs.setOnClickListener { onCall() }

        saveValue("continueFinish", true)
        applyColorFilter = resources.getBoolean(R.bool.applyColorFilter)
        if ("vouchers".equals(model.activePurchaseOrder.serviceSlug, ignoreCase = true)) {
            showVoucher()
        } else {
            hideVoucher()
            initialMapHeight = Utilities.dpToPx(200)
            mapFragment = fragmentManager.findFragmentById(R.id.eddressMap) as MapFragment
            mapFragment!!.getMapAsync(this)
            if (mapFragment != null && mapFragment!!.view != null) {
                mapFragment!!.view!!.layoutParams.height = initialMapHeight
            }
            headerView.layoutParams.height = initialMapHeight
            headerView.setOnClickListener(object : OnOneOffClickListener() {
                override fun onSingleClick(v: View) {
                    expandMap()
                }
            })
        }
        orderUid = intent.getStringExtra("orderUid")
        if (orderUid != null) {
            reloadPurchaseOrder(orderUid!!)
        } else {
            createActivity()
        }
        restaurantRatingBar.onRatingBarChangeListener = OnRatingBarChangeListener { ratingBar, rating, _ ->
            if (rating != 0f) {
                val intent = Intent(this@OrderCompletedActivity, FeedbackPopup::class.java)
                intent.putExtra("rating", restaurantRatingBar.rating)
                intent.putExtra("feedbackType", FeedbackType.FEEDBACK_TYPE_THIRD_PARTY)
                startActivityForResult(intent, 1)
                ratingBar.rating = 0f
            }
        }
    }

    private fun showVoucher() {
        reloadPurchaseOrder(model.activePurchaseOrder.uid)
        if (model.activePurchaseOrder.items[0].legacyDescription != null
            && model.activePurchaseOrder.items[0].legacyId != null
        ) {
            barcodeText.text = "Barcode: " + model.activePurchaseOrder.items[0].legacyDescription
            serialNumberText.text = "SN: " + model.activePurchaseOrder.items[0].legacyId
        }
        mapView.visibility = View.GONE
        headerView.visibility = View.GONE
        voucherView.visibility = View.VISIBLE
    }

    private fun hideVoucher() {
        mapView.visibility = View.VISIBLE
        headerView.visibility = View.VISIBLE
        voucherView.visibility = View.GONE
    }

    fun initializeBasket() {
        val po = model.activePurchaseOrder ?: return
        val formatterShort = Utilities.getCurrencyFormatter(po.currency, po.hideDecimals, true)
        val list: MutableList<IProductItem> = ArrayList(po.items)
        initPromo(po)
        if (hasDiscountOnSubtotal(po)) {
            list.add(
                BasketItem(
                    getString(R.string.discount) + " " + discountDescription,
                    "-" + formatterShort.format(discountValue),
                    BasketItem.SPECIAL
                )
            )
        }
        if (Utilities.notEmpty(po.deliveryChargeLabel)) {
            list.add(BasketItem(po.getDeliveryFeeLabel(), formatterShort.format(po.deliveryCharge), BasketItem.CHARGES))
        }
        if (Utilities.notEmpty(po.minimumChargeLabel)) {
            list.add(
                BasketItem(
                    getString(R.string.surcharge),
                    formatterShort.format(po.minimumChargeFee),
                    BasketItem.SPECIAL
                )
            )
        }
        if (po.deliveryVat != null && po.deliveryVat > 0) {
            list.add(
                BasketItem(
                    getString(R.string.delivery_vat),
                    formatterShort.format(po.deliveryVat),
                    BasketItem.CHARGES
                )
            )
        }
        if (hasDiscountOnDelivery(po)) {
            list.add(
                BasketItem(
                    getString(R.string.discount) + " " + discountDescription,
                    "-" + formatterShort.format(discountValue),
                    BasketItem.SPECIAL
                )
            )
        }
        if (po.tips != null && po.tips > 0) {
            list.add(BasketItem(getString(R.string.tip_for_driver), formatterShort.format(po.tips), BasketItem.CHARGES))
        }
        var totalPriceLabel = po.totalPriceLabel
        if (Utilities.isEmpty(totalPriceLabel)) {
            totalPriceLabel = Utilities.formatCurrency(0.0, po.currency, po.hideDecimals)
        }
        list.add(BasketItem(getString(R.string.total), totalPriceLabel, BasketItem.TOTAL))
        if (hasDiscountOnTotal(po)) {
            list.add(
                BasketItem(
                    getString(R.string.discount) + " " + discountDescription,
                    "-" + formatterShort.format(discountValue),
                    BasketItem.SPECIAL
                )
            )
        }
        if (po.deductionFromWallet != null && po.deductionFromWallet > 0) {
            list.add(
                BasketItem(
                    getString(R.string.wallet),
                    "-" + formatterShort.format(po.deductionFromWallet),
                    BasketItem.SPECIAL
                )
            )
        }
        if (hasDiscountOnTotal(po) || po.deductionFromWallet != null && po.deductionFromWallet > 0) {
            list.add(
                BasketItem(
                    getString(R.string.amount_paid),
                    formatter!!.format(po.collectionAmount),
                    BasketItem.TOTAL
                )
            )
        }
        val adapter = BasketAdapter(context, list)
        adapter.setClickListener(object : ItemClickListener<IProductItem>(adapter) {
            override fun onClick(item: IProductItem, position: Int) {
                if (item is MenuItemObject) {
                    ViewRouter.instance().displayProduct(
                        this@OrderCompletedActivity,
                        item,
                        CollectionData("basket_completed_order", "My Basket", CollectionData.Type.BASKET),
                        false,
                        false,
                        false,
                        true
                    )
                }
            }
        })
        recyclerView.layoutManager = NonScrollingManager(context)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun initPromo(po: PurchaseOrderObject) {
        if (Utilities.notEmpty(po.discountType) && po.appliedOn != null) {
            if ("percentage".equals(po.discountType, ignoreCase = true)) {
                if (po.discountValue > 0) {
                    when (po.appliedOn) {
                        PromocodeApplication.TOTAL -> {
                            discountValue = discountWithValue(po.discountValue, po.total)
                            discountDescription = "(-" + po.discountValue + getString(R.string.total_off)
                        }
                        PromocodeApplication.DELIVERY -> {
                            var totalDeliveryFee = po.deliveryCharge
                            if (po.deliveryVat != null) {
                                totalDeliveryFee += po.deliveryVat
                            }
                            if (po.minimumChargeFee != null) {
                                totalDeliveryFee += po.minimumChargeFee
                            }
                            discountValue = discountWithValue(po.discountValue, totalDeliveryFee)
                            discountDescription = "(-" + po.discountValue + getString(R.string.delivery_off)
                        }
                        PromocodeApplication.SUBTOTAL -> {
                            discountValue = discountWithValue(po.discountValue, po.subTotal)
                            discountDescription = "(-" + po.discountValue + getString(R.string.sub_total_off)
                        }
                        else -> {
                        }
                    }
                }
            } else if ("value".equals(po.discountType, ignoreCase = true) || "amount".equals(
                    po.discountType,
                    ignoreCase = true
                )
            ) {
                if (po.discountValue > 0) {
                    discountValue = po.discountValue
                    when (po.appliedOn) {
                        PromocodeApplication.TOTAL -> discountDescription =
                            "(-" + formatter!!.format(discountValue) + " " + getString(
                                R.string.off_total
                            )
                        PromocodeApplication.DELIVERY -> discountDescription =
                            "(-" + formatter!!.format(discountValue) + " " + getString(
                                R.string.off_delivery
                            )
                        PromocodeApplication.SUBTOTAL -> discountDescription =
                            "(-" + formatter!!.format(discountValue) + " " + getString(
                                R.string.off_sub_total
                            )
                        else -> {
                        }
                    }
                }
            }
        }
    }

    private fun discountWithValue(discountValue: Double, onAmount: Double): Double {
        val discount = discountValue * onAmount / 100.0
        return if (discount < onAmount) discount else 0.0
    }

    private fun hasDiscountOnSubtotal(po: PurchaseOrderObject): Boolean {
        return Utilities.notEmpty(po.discountType) && po.appliedOn == PromocodeApplication.SUBTOTAL
    }

    private fun hasDiscountOnDelivery(po: PurchaseOrderObject): Boolean {
        return Utilities.notEmpty(po.discountType) && po.appliedOn == PromocodeApplication.DELIVERY
    }

    private fun hasDiscountOnTotal(po: PurchaseOrderObject): Boolean {
        return Utilities.notEmpty(po.discountType) && po.appliedOn == PromocodeApplication.TOTAL
    }

    private fun reloadPurchaseOrder(orderUid: String) {
        Rest.request().uri("getOrderDetails/$orderUid").showLoader(getString(R.string.loader_text)).response(
            PurchaseOrderObject::class.java
        ) { response ->
            model.activePurchaseOrder = response as PurchaseOrderObject?
            if (!isInitialized) createActivity() else {
                updateActivity()
            }
        }.post()
    }

    override fun onDestroy() {
        model.activePurchaseOrder = null
        super.onDestroy()
    }

    private fun createActivity() {
        isInitialized = true
        if (model.activePurchaseOrder == null) return
        orderUid = model.activePurchaseOrder.uid
        formatter =
            Utilities.getCurrencyFormatter(model.activePurchaseOrder.currency, model.activePurchaseOrder.hideDecimals)
        if (model.activePurchaseOrder.deliveryLat != null) {
            deliveryEddress = AddressObject()
            deliveryEddress!!.lat = model.activePurchaseOrder.deliveryLat
            deliveryEddress!!.lon = model.activePurchaseOrder.deliveryLon
        }
        if (model.activePurchaseOrder.pickupLat != null) {
            pickupEddress = AddressObject()
            pickupEddress!!.lat = model.activePurchaseOrder.pickupLat
            pickupEddress!!.lon = model.activePurchaseOrder.pickupLon
        }
        val title = findViewById<TextView>(R.id.title)
        title.text = model.activePurchaseOrder.providerName
        if (model.activePurchaseOrder.notes != null && model.activePurchaseOrder.notes.isNotEmpty()) {
            notesText.text = model.activePurchaseOrder.notes
        } else {
            notesLayout.visibility = View.GONE
        }
        updateActivity()
        initializeBasket()
    }

    private fun callDriver() {
        if (model.activePurchaseOrder == null) return
        driverContacted(orderUid!!, serviceObject)
        val number = Uri.parse("tel:" + model.activePurchaseOrder.workerPhoneNumber)
        try {
            val callIntent = Intent(Intent.ACTION_CALL, number)
            startActivity(callIntent)
        } catch (e: SecurityException) {
            val callIntent = Intent(Intent.ACTION_DIAL, number)
            startActivity(callIntent)
        }
    }

    private fun cancelOrder() {
        if (model.activePurchaseOrder == null) return
        val sDialog = NokAlertDialog(this@OrderCompletedActivity, NokAlertDialog.WARNING)
        sDialog.setMessage(getString(R.string.are_you_sure_to_cancel_order))
        sDialog.setConfirmText(getString(R.string.confirm_text_cance_order))
        sDialog.setCancelText(getString(R.string.nevermind))
        sDialog.setTitle(getString(R.string.confirm_text_cance_order))
            .setConfirmClickListener {
                if (model.activePurchaseOrder == null) return@setConfirmClickListener
                sDialog.hide()
                val param = JsonObject()
                param.addProperty("cancelationReason", "Canceled by customer")
                Rest.request().uri("cancelOrder/" + model.activePurchaseOrder.uid)
                    .showLoader(getString(R.string.cancelling_order)).params(param).response(
                        PurchaseOrderObject::class.java, Response.Listener { response: PurchaseOrderObject ->
                            orderCanceled(orderUid!!, serviceObject)
                            if (response.promoCodeUid != null) {
                                val promoCodeUid =
                                    if (model.activePurchaseOrder.promoCodeUid == null) model.activePurchaseOrder.promoCode else model.activePurchaseOrder.promoCodeUid
                                couponRemoved(
                                    model.activePurchaseOrder.uid,
                                    promoCodeUid,
                                    model.activePurchaseOrder.promoCode,
                                    discountValue!!
                                )
                            }
                            reloadPurchaseOrder(response.uid)
                        } as Response.Listener<PurchaseOrderObject>).post()
            }.show()
    }

    val serviceObject: ServiceObject
        get() {
            val service = ServiceObject()
            service.name = model.activePurchaseOrder.providerName
            service.id = model.activePurchaseOrder.serviceSlug
            service.serviceName = model.activePurchaseOrder.serviceType
            service.currency = Currency()
            service.currency.iso = model.activePurchaseOrder.countryIso
            return service
        }


    private fun onETALayout() {
    }

    private fun onCall() {
        if (model.activePurchaseOrder == null) return
        val items: Array<CharSequence> = if (Utilities.notEmpty(model.activePurchaseOrder.servicePhoneNumber)) {
            arrayOf(model.activePurchaseOrder.servicePhoneNumber)
        } else {
            arrayOf(model.supportPhoneNumber)
        }
        val builder = AlertDialog.Builder(this@OrderCompletedActivity)
        builder.setTitle(getString(R.string.title_contact_us))
        builder.setItems(items) { _: DialogInterface?, _: Int ->
            val number: Uri = if (Utilities.notEmpty(model.activePurchaseOrder.servicePhoneNumber)) {
                Uri.parse("tel:" + model.activePurchaseOrder.servicePhoneNumber)
            } else {
                Uri.parse("tel:" + model.supportPhoneNumber)
            }
            try {
                val callIntent = Intent(Intent.ACTION_CALL, number)
                startActivity(callIntent)
            } catch (e: SecurityException) {
                val callIntent = Intent(Intent.ACTION_DIAL, number)
                startActivity(callIntent)
            }
            supportContacted(orderUid!!, serviceObject)
        }
        builder.create().show()
    }

    private fun updateActivity() {
        updateStatus()
        updateTimers()
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
        closeMapButton.visibility = View.VISIBLE
        mapParent.removeView(mapView)
        mapLayout.addView(mapView, 0)
        etaLabel.visibility = View.GONE
        contactUs.visibility = View.GONE
        headerView.visibility = View.GONE
    }

    private fun shrinkMap() {
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
        closeMapButton.visibility = View.GONE
        mapLayout.removeView(mapView)
        mapParent.addView(mapView, 0)
        etaLabel.visibility = View.VISIBLE
        contactUs.visibility = View.VISIBLE
        headerView.visibility = View.VISIBLE
        trackDriverLocation = true
        createBounds(false)
    }

    override fun onPause() {
        if (driverLocationTimer != null) {
            Log.e("aa", "driverLocationTimer paused")
            driverLocationTimer!!.cancel()
            driverLocationTimer = null
        }
        if (statusTimer != null) {
            Log.e("aa", "statusTimer paused")
            statusTimer!!.cancel()
            statusTimer = null
        }
        unregisterReceiver(receiver)
        super.onPause()
    }

    var driverBean: WorkerLocationBean? = null
    private fun updateDriverLocationTimer() {
        if (driverLocationTimer != null) return
        if (model.activePurchaseOrder != null && model.activePurchaseOrder.fetchDriverLocation && model.activePurchaseOrder.workerUid != null && model.activePurchaseOrder.fetchDriverLocationTimerSecs > 0) {
            driverLocationTimer = object :
                CountDownTimer(216000000, (model.activePurchaseOrder.fetchDriverLocationTimerSecs * 1000).toLong()) {
                override fun onTick(millisUntilFinished: Long) {
                    if (model.activePurchaseOrder != null && model.activePurchaseOrder.workerUid != null) {
                        Rest.request()
                            .uri("workerLocation/" + model.activePurchaseOrder.uid + "/" + model.activePurchaseOrder.workerUid)
                            .response(
                                WorkerLocationBean::class.java, Response.Listener<WorkerLocationBean?> { response ->
                                    if (response == null) return@Listener
                                    driverBean = response
                                    createBounds(false)
                                }).post()
                    }
                }

                override fun onFinish() {}
            }.start()
        }
    }

    private fun updateStatusTimer() {
        if (model.activePurchaseOrder != null && model.activePurchaseOrder.statusTimerSecs > 0) {
            if (statusTimer == null) {
                statusTimer =
                    object : CountDownTimer(216000000, (model.activePurchaseOrder.statusTimerSecs * 1000).toLong()) {
                        override fun onTick(millisUntilFinished: Long) {
                            if (model.activePurchaseOrder == null) return
                            Rest.request().uri("getOrderDetails/" + model.activePurchaseOrder.uid).response(
                                PurchaseOrderObject::class.java
                            ) { response ->
                                if (response != null) {
                                    model.activePurchaseOrder = response as PurchaseOrderObject?
                                }
                                updateDriverLocationTimer()
                                updateStatus()
                                initializeBasket()
                            }.post()
                        }

                        override fun onFinish() {}
                    }.start()
            }
        }
    }

    private fun updateTimers() {
        updateDriverLocationTimer()
        updateStatusTimer()
    }

    private fun animateMap() {
        if (eddressMap != null && bounds != null) {
            if (boundsSize > 1) {
                eddressMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            } else if (boundsSize == 1) {
                eddressMap!!.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        bounds!!.center,
                        if (isExpanded) Utilities.zoom else Utilities.zoomFar
                    )
                )
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        eddressMap = map
        if (map.uiSettings == null) return
        map.uiSettings.isZoomGesturesEnabled = false
        map.uiSettings.setAllGesturesEnabled(false)
        map.uiSettings.isScrollGesturesEnabled = false
        map.uiSettings.isRotateGesturesEnabled = false
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isCompassEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMyLocationButtonEnabled = false
        //        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(OrderCompletedActivity.this, R.raw.style_json));
        eddressMap!!.setOnMapLoadedCallback { createBounds(true) }
        eddressMap!!.setOnCameraMoveStartedListener { reasonCode: Int ->
            if (reasonCode == OnCameraMoveStartedListener.REASON_GESTURE) {
                trackDriverLocation = false
            }
        }
    }

    private fun createBounds(showPath: Boolean) {
        if (eddressMap == null) return
        eddressMap!!.clear()
        val x = Utilities.dpToPx(24).toDouble() //original width 60pixels
        var pickupLocation: LatLng? = null
        if (pickupEddress != null && pickupEddress!!.lat != null && pickupEddress!!.lat != 0.0) {
            pickupLocation = LatLng(pickupEddress!!.lat, pickupEddress!!.lon)
            val marker = eddressMap!!.addMarker(MarkerOptions().position(pickupLocation))
            //            pickupEddress.setAddressType("98");
            val bmp = Utilities.getMarkerPin(
                context,
                if (applyColorFilter) R.color.secondaryColor else 0,
                R.drawable.pin_pickup,
                x.toInt()
            )
            if (bmp != null) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp))
            }
        }
        var deliveryLocation: LatLng? = null
        if (deliveryEddress != null && deliveryEddress!!.lat != null && deliveryEddress!!.lat != 0.0) {
            deliveryLocation = LatLng(deliveryEddress!!.lat, deliveryEddress!!.lon)
            val marker = eddressMap!!.addMarker(MarkerOptions().position(deliveryLocation))
            //            deliveryEddress.setAddressType("99");
            var bmp = Utilities.getMarkerPin(
                context,
                if (applyColorFilter) R.color.secondaryColor else 0,
                R.drawable.pin_dark,
                x.toInt()
            )
            if (bmp != null) {
                if (applyColorFilter) {
                    bmp = ImageHelper.changeBitmapColor(bmp, ContextCompat.getColor(context, R.color.buttonColor))
                }
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp))
            }
        }
        boundsSize = 0
        val builder = LatLngBounds.Builder()
        if (deliveryLocation != null && deliveryLocation.latitude != 0.0) {
            builder.include(deliveryLocation)
            boundsSize++
        }
        if (pickupLocation != null && pickupLocation.latitude != 0.0) {
            builder.include(pickupLocation)
            boundsSize++
        }
        if (driverBean != null && eddressMap != null) {
            val latLon = driverBean!!.latLon
            if (latLon != null) {
                val marker = eddressMap!!.addMarker(MarkerOptions().position(latLon))
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(driverBean!!.getVehicleIcon(context)))
                builder.include(latLon)
                boundsSize++
            }
        }
        if (boundsSize > 0) {
            bounds = builder.build()
        }
        if (showPath && pickupLocation != null && deliveryLocation != null) {
            GoogleMapsPath(this@OrderCompletedActivity, eddressMap!!, pickupLocation, deliveryLocation)
        }
        if (trackDriverLocation) {
            animateMap()
        }
    }

    private fun updateStatus() {
        if (model.activePurchaseOrder == null) return
        val po = model.activePurchaseOrder
        cancelOrderButton.visibility = if (po.canCancelOrder) View.VISIBLE else View.GONE
        if (formatter == null) {
            formatter = Utilities.getCurrencyFormatter(
                model.activePurchaseOrder.currency,
                model.activePurchaseOrder.hideDecimals
            )
        }
        if (po.completedOn != null) {
            eta.text = context.getString(R.string.delivered)
            etaLabel.text = TimestampUtils.getElapsedTime(po.createdOn, po.completedOn)
        } else {
            eta.text = context.getString(R.string.eta)
            if (model.activePurchaseOrder.eta != null) {
                etaLabel.text = model.activePurchaseOrder.eta.toUpperCase()
            } else {
                etaLabel.text = ""
            }
        }
        if (model.activePurchaseOrder.status == "Order Placed") {
            etaLabel.text = ""
            eta.text = ""
        }
        if (po.statusEnum == "CANCELED") {
            etaLabel.text = po.statusEnum
            eta.text = po.canceledSubtitle
        }
        driverSection.visibility = View.GONE
        cancelLayout.visibility = View.GONE
        if (!po.hasReturn) returnLayout.visibility = View.GONE else returnLayout.visibility = View.VISIBLE
        val primaryColor = ContextCompat.getColor(this, R.color.buttonColor)
        val inactiveColor = ContextCompat.getColor(this, R.color.darker_gray)
        phoneImage.setColorFilter(primaryColor, PorterDuff.Mode.SRC_ATOP)
        deliverImage.setColorFilter(inactiveColor, PorterDuff.Mode.SRC_ATOP)
        returnImage.setColorFilter(inactiveColor, PorterDuff.Mode.SRC_ATOP)
        pickupImage.setColorFilter(inactiveColor, PorterDuff.Mode.SRC_ATOP)
        if (applyColorFilter) {
            newOrderImg.setImageResource(R.drawable.order_state_customer)
            newOrderImg.setColorFilter(primaryColor, PorterDuff.Mode.SRC_ATOP)
        }
        newOrderDate.text = po.orderSubmittedSubtitle
        newOrderText.text = po.orderSubmittedTitle
        confirmedElapsed.text = ""
        pickupElapsed.text = ""
        deliverElapsed.text = ""
        returnElapsed.text = ""
        var lastDate = po.createdOn
        if (Utilities.notEmpty(po.confirmedTitle)) {
            confirmedText.text = po.confirmedTitle
            confirmedDate.text = po.confirmedSubtitle
            if (po.confirmComplete) confirmedImage.setColorFilter(
                if (applyColorFilter) primaryColor else 0,
                PorterDuff.Mode.SRC_ATOP
            ) else {
                confirmedImage.setColorFilter(inactiveColor, PorterDuff.Mode.SRC_ATOP)
            }
            if (po.confirmedOn != null) {
                confirmedElapsed.text = TimestampUtils.getElapsedTime(lastDate!!, po.confirmedOn)
                lastDate = po.confirmedOn
            }
        }
        if (!po.hasPickup) pickupLayout.visibility = View.GONE else {
            pickupLayout.visibility = View.VISIBLE
            if (Utilities.notEmpty(po.pickupTitle)) {
                pickupText.text = po.pickupTitle
                pickupDate.text = po.pickupSubtitle
                if (po.pickupFailed) {
                    pickupImage.setImageResource(R.drawable.error)
                }
                if (po.pickupComplete) {
                    pickupImage.setColorFilter(if (applyColorFilter) primaryColor else 0, PorterDuff.Mode.SRC_ATOP)
                    if (po.pickedUpOn != null) {
                        pickupElapsed.text = TimestampUtils.getElapsedTime(lastDate!!, po.pickedUpOn)
                        lastDate = po.pickedUpOn
                    }
                } else {
                    pickupImage.setColorFilter(inactiveColor, PorterDuff.Mode.SRC_ATOP)
                }
            }
        }
        if (!po.hasDelivery) {
            deliverLayout.visibility = View.GONE
        } else {
            deliverLayout.visibility = View.VISIBLE
            if (Utilities.notEmpty(po.deliverTitle)) {
                deliverText.text = po.deliverTitle
                deliverDate.text = po.deliverSubtitle
                if (po.deliveryFailed) {
                    deliverImage.setImageResource(R.drawable.error)
                }
                if (po.deliveryComplete) {
                    deliverImage.setColorFilter(if (applyColorFilter) primaryColor else 0, PorterDuff.Mode.SRC_ATOP)
                    if (po.deliveredOn != null) {
                        deliverElapsed.text = TimestampUtils.getElapsedTime(lastDate!!, po.deliveredOn)
                        lastDate = po.deliveredOn
                    }
                } else {
                    deliverImage.setColorFilter(inactiveColor, PorterDuff.Mode.SRC_ATOP)
                }
            }
        }
        if (Utilities.notEmpty(po.returnTitle)) {
            returnText.text = po.returnTitle
            returnDate.text = po.returnSubtitle
            if (po.returnFailed) {
                returnImage.setImageResource(R.drawable.error)
            }
            if (po.returnComplete) {
                returnImage.setColorFilter(if (applyColorFilter) primaryColor else 0, PorterDuff.Mode.SRC_ATOP)
                if (po.returnedOn != null) {
                    deliverElapsed.text = TimestampUtils.getElapsedTime(lastDate!!, po.returnedOn)
                }
            } else {
                returnImage.setColorFilter(inactiveColor, PorterDuff.Mode.SRC_ATOP)
            }
        }
        if (po.fetchDriverLocation) {
            driverName.text = model.activePurchaseOrder.workerName
            //            driverImage.setImageURI(U.parse(model.activePurchaseOrder.workerImageUrl));
            driverSection.visibility = View.VISIBLE
        }
        if (Utilities.notEmpty(po.canceledTitle)) {
            confirmedLayout.visibility = View.GONE
            pickupLayout.visibility = View.GONE
            deliverLayout.visibility = View.GONE
            returnLayout.visibility = View.GONE
            cancelLayout.visibility = View.VISIBLE
            cancelImage.setColorFilter(primaryColor, PorterDuff.Mode.SRC_ATOP)
            cancelText.text = po.canceledTitle
            cancelDate.text = po.canceledSubtitle
        }
        if (model.activePurchaseOrder.showFeedback) {
            feedbackLayout.visibility = View.VISIBLE
        } else {
            feedbackLayout.visibility = View.GONE
        }
        if (model.activePurchaseOrder.showThirdPartyFeedback) {
            restaurantFeedbackView.visibility = View.VISIBLE
        } else {
            restaurantFeedbackView.visibility = View.GONE
        }
        var lastChild: View? = null
        for (i in 0 until statusLayout.childCount) {
            val child = statusLayout.getChildAt(i)
            if (child.visibility == View.VISIBLE) lastChild = child
        }
        if (lastChild != null) {
            lastChild.background = ContextCompat.getDrawable(
                context,
                R.drawable.rounded_corners_white_10_bottom
            )
        }
    }

    fun feedbackClick(view: View) {
        val feedback = view.tag as String
        val intent = Intent(this, FeedbackPopup::class.java)
        intent.putExtra("rating", feedback)
        intent.putExtra("feedbackType", FeedbackType.FEEDBACK_TYPE_DELIVERY)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                if (isFinishing || model.activePurchaseOrder == null) return
                val feedbackMessage = data!!.getStringExtra("feedbackMessage")
                val ratingString = data.getStringExtra("rating")
                val rating = Integer.valueOf(ratingString!!)
                Rest.request().uri("submitRestaurantRating/" + model.activePurchaseOrder.uid)
                    .params(Json.param().p("feedbackMessage", feedbackMessage).p("rating", rating).toJson()).showLoader(
                        getString(
                            R.string.submitting_feedback
                        )
                    ).response(
                        ResponseBean::class.java, Response.Listener { response: ResponseBean ->
                            Utilities.Toast(response.description)
                            model.activePurchaseOrder.feedbackRating = rating
                            restaurantFeedbackView.visibility = View.GONE
                        } as Response.Listener<ResponseBean>).post()
            } else if (requestCode == 2) {
                if (isFinishing || model.activePurchaseOrder == null) return
                val feedbackMessage = data!!.getStringExtra("feedbackMessage")
                val ratingString = data.getStringExtra("rating")
                val rating = Integer.valueOf(ratingString!!)
                Rest.request().uri("submitFeedback/" + model.activePurchaseOrder.uid)
                    .params(Json.param().p("feedbackMessage", feedbackMessage).p("rating", rating).toJson()).showLoader(
                        getString(
                            R.string.submitting_feedback
                        )
                    ).response(
                        ResponseBean::class.java, Response.Listener { response: ResponseBean ->
                            Utilities.Toast(response.description)
                            if (model.activePurchaseOrder != null) model.activePurchaseOrder.feedbackRating = rating
                            if (!isFinishing) {
                                feedbackLayout.visibility = View.GONE
                            }
                        } as Response.Listener<ResponseBean>).post()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(MainActivity.ACTION_UPDATE_ORDER)
        registerReceiver(receiver, filter)
        updateTimers()
        trackDriverLocation = true
    }

    override fun onBackPressed() {
        if (isExpanded) {
            shrinkMap()
            return
        }
        saveValue("continueFinish", true)
        super.onBackPressed()
    }

    companion object {
        private var driverLocationTimer: CountDownTimer? = null
        private var statusTimer: CountDownTimer? = null
    }
}
