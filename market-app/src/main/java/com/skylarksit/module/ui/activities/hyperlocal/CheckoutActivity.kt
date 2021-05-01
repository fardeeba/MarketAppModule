@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.activities.hyperlocal

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.google.gson.JsonObject
import com.skylarksit.module.R
import com.skylarksit.module.analytics.Segment.couponApplied
import com.skylarksit.module.analytics.Segment.couponDenied
import com.skylarksit.module.analytics.Segment.couponEntered
import com.skylarksit.module.analytics.Segment.orderCompleted
import com.skylarksit.module.lib.ErrorResource
import com.skylarksit.module.lib.Rest
import com.skylarksit.module.libs.alertdialog.NokListAlertDialog
import com.skylarksit.module.libs.alertdialog.PromoCodeLayout
import com.skylarksit.module.pojos.AddressObject
import com.skylarksit.module.pojos.DeliveryChargeResponseBean
import com.skylarksit.module.pojos.PaymentOption
import com.skylarksit.module.pojos.PurchaseOrderResponseBean
import com.skylarksit.module.pojos.services.*
import com.skylarksit.module.ui.activities.TimePickerActivity
import com.skylarksit.module.ui.lists.items.TipOptionAdaptor
import com.skylarksit.module.ui.lists.main.adapters.CheckoutBasketAdapter
import com.skylarksit.module.ui.model.*
import com.skylarksit.module.ui.utils.IClickListener
import com.skylarksit.module.ui.utils.LocalStorage
import com.skylarksit.module.ui.utils.OnOneOffClickListener
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.NonScrollingManager
import com.skylarksit.module.utils.Services
import com.skylarksit.module.utils.Services.ServiceResult
import com.skylarksit.module.utils.Utilities
import java.util.*

class CheckoutActivity : MyAppCompatActivity() {

    lateinit var confirmButton: View
    private lateinit var tipsContainer: RelativeLayout
    private lateinit var paymentBar: RelativeLayout
    private var tipOptionsRecyclerView: RecyclerView? = null
    private lateinit var deliveryAddressIcon: View
    private val tipOptions: MutableList<TipOption> = ArrayList()
    private var paymentOptionsList: MutableList<PaymentOption>? = ArrayList()
    private var activeService: ServiceObject? = null
    private var basketAdapter: CheckoutBasketAdapter? = null
    private var tipOptionAdaptor: TipOptionAdaptor? = null
    private lateinit var deliveryScheduleLayout: View
    private lateinit var checkoutNotesLayout: View
    private lateinit var summaryLayout: View
    private lateinit var addressBarText: TextView
    private lateinit var noteText: EditText
    private lateinit var checkoutNotesDetail: View
    private lateinit var paymentText: TextView
    private lateinit var paymentImage: ImageView
    private lateinit var totalPriceText: TextView
    private lateinit var totalItemsText: TextView
    private lateinit var addressBar: View
    private lateinit var deliveryTimeText: TextView
    private lateinit var basketView: RecyclerView
    private lateinit var timeImage: ImageView
    private lateinit var promoCodeButton: TextView

    private var isPricingValid = true
    var orderUid: String? = null
    private var selectedDate: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.checkout_activity)
        confirmButton = findViewById(R.id.confirmButton)
        tipsContainer = findViewById(R.id.tipsLayout)
        tipOptionsRecyclerView = findViewById(R.id.tipOptionsList)
        deliveryAddressIcon = findViewById(R.id.deliveryAddressIcon)
        deliveryScheduleLayout = findViewById(R.id.deliveryScheduleLayout)
        checkoutNotesLayout = findViewById(R.id.checkoutNotesLayout)
        summaryLayout = findViewById(R.id.summaryLayout)
        addressBarText = findViewById(R.id.addressBarText)
        noteText = findViewById(R.id.checkoutNotes)
        checkoutNotesDetail = findViewById(R.id.checkoutNotesDetail)
        paymentText = findViewById(R.id.paymentText)
        paymentImage = findViewById(R.id.paymentImage)
        totalPriceText = findViewById(R.id.totalPrice)
        totalItemsText = findViewById(R.id.totalItems)
        addressBar = findViewById(R.id.addressBar)
        deliveryTimeText = findViewById(R.id.deliveryTimeText)
        basketView = findViewById(R.id.recyclerViewBasket)
        timeImage = findViewById(R.id.timeImage)
        promoCodeButton = findViewById(R.id.promoCodeButton)
        paymentBar = findViewById(R.id.paymentBar)

        paymentBar.setOnClickListener { displayPaymentOptions() }
        promoCodeButton.setOnClickListener { displayPromoBox() }


        val slug = intent.getStringExtra("activeService")
        orderUid = intent.getStringExtra("orderUid")
        activeService = model.findProviderByName(slug)
        if (activeService == null) return
        activeService!!.setContext(this@CheckoutActivity)
        val hasDelivery = activeService!!.getHasDelivery()
        if (activeService!!.isCourier) {
            deliveryAddressIcon.visibility = View.GONE
        } else {
            deliveryAddressIcon.visibility = View.VISIBLE
            addressBar.setOnClickListener(object : OnOneOffClickListener() {
                override fun onSingleClick(v: View) {
                    updateDeliveryAddress()
                }
            })
        }
        if (hasDelivery && !activeService!!.hasPickup) {
            model.deliveryEddress = model.currentAddress
        } else if (activeService!!.hasPickup && !hasDelivery) {
            model.pickupEddress = model.currentAddress
        }
        if (activeService!!.hasTimeSchedule) {
            if (Utilities.isEmpty(model.deliveryDate) && !activeService!!.isTimeRequired && !activeService!!.isClosed()) {
                deliveryTimeText.text = activeService!!.etaText
            } else if (Utilities.notEmpty(model.deliveryDate)) {
                deliveryTimeText.text = model.deliveryDate
            } else {
                Utilities.setFontStyle(deliveryTimeText, R.style.LightLarge)
            }
        }
        if (activeService!!.getHasNotes()) {
            checkoutNotesLayout.visibility = View.VISIBLE
            if (Utilities.notEmpty(model.notes)) {
                noteText.setText(model.notes)
            } else if (Utilities.notEmpty(activeService!!.getNotesPromptText())) {
                noteText.hint = activeService!!.getNotesPromptText()
            }
        } else {
            checkoutNotesLayout.visibility = View.GONE
        }
        tipOptionsRecyclerView!!.layoutManager =
            LinearLayoutManager(this@CheckoutActivity, LinearLayoutManager.HORIZONTAL, false)
        if (activeService!!.tipOptions != null && activeService!!.tipOptions.isNotEmpty()) {
            tipOptions.clear()
            val tipsValues = activeService!!.tipOptions.split(",".toRegex()).toTypedArray()
            for (tip in tipsValues) {
                val tipOption = TipOption()
                tipOption.tipLabel = activeService!!.currencySymbol + Utilities.getCurrencyFormatter(
                    activeService!!.currency, true
                ).format(tip.toDouble())
                tipOption.tipValue = tip.toDouble()
                if (model.tip == tipOption.tipValue) {
                    tipOption.isSelected = true
                }
                tipOptions.add(tipOption)
            }
            tipOptionAdaptor = TipOptionAdaptor(this, tipOptions, object : IClickListener<TipOption> {
                override fun click(`object`: TipOption?) {
                    initializeBasket()
                }
            })
            tipOptionsRecyclerView!!.adapter = tipOptionAdaptor
            tipOptionAdaptor!!.notifyDataSetChanged()
            if (model.paymentOption == null || model.paymentOption.isCash) {
                tipsContainer.visibility = View.GONE
            } else {
                tipsContainer.visibility = View.VISIBLE
            }
        } else {
            tipsContainer.visibility = View.GONE
        }
        confirmButton.setOnClickListener(object : OnOneOffClickListener() {
            override fun onSingleClick(v: View) {
                checkoutButtonTapped()
            }
        })


//        RelativeLayout pickupDetails = findViewById(R.id.pickupDetails);
//        if (activeService.getHasPickup() && !activeService.isCourier()) {
//            if (hasDelivery)
//                deliveryDetails.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corners_white_10_bottom));
//            else {
//                pickupDetails.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corners_white_10));
//            }
//        } else {
//            pickupDetails.setVisibility(View.GONE);
//            deliveryDetails.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_corners_white_10));
//        }


//        deliveryLocationTextView = findViewById(R.id.deliveryLocation);

//        deliveryDetails.setVisibility(hasDelivery && !activeService.isCourier() ? View.VISIBLE : View.GONE);

//        pickupLocationTextView = findViewById(R.id.pickupLocation);
        if (!activeService!!.hasTimeSchedule) {
            deliveryScheduleLayout.visibility = View.GONE
        } else {
            deliveryScheduleLayout.visibility = View.VISIBLE
            //            addTooltip(deliveryScheduleLayout, R.string.tip_delivery_time);
            deliveryTimeText.setOnClickListener(object : OnOneOffClickListener() {
                override fun onSingleClick(v: View) {
                    showTimeSelectionDialog()
                }
            })
        }
        basketAdapter = CheckoutBasketAdapter(context, list)
        basketView.layoutManager = NonScrollingManager(context)
        basketView.adapter = basketAdapter
        if (model.promoPending != null && model.promoPending.promoCode != null) {
            applyPromo(model.promoPending.promoCode)
        } else {
            initializeBasket()
        }
        if (model.hasDynamicPricing(activeService)) {
            calcDynamicDeliveryCharge()
        }
        createPaymentList()
        updateAddress()
    }

    private fun updateAddress() {
        var address = ""
        if (model.pickupEddress != null && activeService!!.hasPickup) {
            address += model.pickupEddress.label
        }
        if (model.deliveryEddress != null && activeService!!.hasDelivery) {
            if (address.isNotEmpty()) address += " - "
            address += model.deliveryEddress.label
        }
        addressBarText.text = address
    }

    private fun checkoutButtonTapped() {
        if (activeService!!.hasTimeSchedule
            && Utilities.isEmpty(selectedDate)
            && (activeService!!.isTimeRequired
                    || activeService!!.isClosed()
                    || activeService!!.hasBookNow())
        ) {
            showTimeSelectionDialog()
            return
        }
        if (!model.hasUserDetails()) {
            val intent = Intent(context, UserDetailsActivity::class.java)
            startActivityForResult(intent, 1239)
            return
        }
        if (model.paymentOption == null) {
            displayPaymentOptions()
            return
        }
        var tp: TipOption? = null
        for (p in tipOptions) {
            if (p.isSelected) {
                tp = p
                break
            }
        }
        if (validatePage()) {
            placeOrder(model.paymentOption, tp)
        }
    }

    private fun calcDynamicDeliveryCharge() {
        if (!model.hasDynamicPricing(activeService) || activeService!!.isCourier && model.hasDynamicPricing(
                activeService
            )
        ) return
        if (model.deliveryEddress == null) return
        val params = JsonObject()
        params.addProperty("serviceUid", activeService!!.serviceProviderUid)
        if (model.pickupEddress != null) {
            params.addProperty("pickupLat", model.pickupEddress.lat)
            params.addProperty("pickupLon", model.pickupEddress.lon)
        }
        if (model.deliveryEddress != null) {
            params.addProperty("deliveryLat", model.deliveryEddress.lat)
            params.addProperty("deliveryLon", model.deliveryEddress.lon)
        }
        isPricingValid = false
        Rest.request().uri("getDynamicDeliveryCharge").params(params).showLoader(getString(R.string.loader_text))
            .response(
                DeliveryChargeResponseBean::class.java, Response.Listener { response: DeliveryChargeResponseBean ->
                    model.deliveryPrice = response.deliveryPrice
                    model.deliveryDistance = response.distance
                    model.deliveryDistanceLabel = response.distanceLabel
                    isPricingValid = model.deliveryPrice > 0
                    initializeBasket()
                } as Response.Listener<DeliveryChargeResponseBean>).post()
    }

    private fun validatePage(): Boolean {
        if (!activeService!!.hasTimeSchedule && activeService!!.isClosed()) {
            Utilities.Error(getString(R.string.store_is_currently), activeService!!.etaText)
            return false
        }
        if (activeService!!.hasTimeSchedule && Utilities.isEmpty(model.deliveryDate)
            && (activeService!!.isTimeRequired || activeService!!.isClosed())
        ) {
            Utilities.Toast(getString(R.string.please_select_desired_time))
            return false
        }
        if (activeService!!.getHasPickup() &&
            (model.pickupEddress == null || !activeService!!.deliversTo(
                model.pickupEddress.lat,
                model.pickupEddress.lon
            ))
        ) {
            Utilities.Toast(getString(R.string.please_select_pickup))
            model.pickupEddress = null
            return false
        }
        if (activeService!!.getHasDelivery() &&
            (model.deliveryEddress == null || !activeService!!.deliversTo(
                model.deliveryEddress.lat,
                model.deliveryEddress.lon
            ))
        ) {
            Utilities.Toast(getString(R.string.please_select_delivery))
            model.deliveryEddress = null
            return false
        }
        if (activeService!!.getHasDelivery() && activeService!!.getHasPickup()) {
            if (model.pickupEddress.code == model.deliveryEddress.code && model.deliveryEddress.code != "PIN_LOCATION") {
                Utilities.Toast(getString(R.string.same_pickup_and_delivery))
                return false
            }
        }
        if (!isPricingValid) {
            Utilities.Toast(getString(R.string.invalid_delivery_price))
            return false
        }
        return true
    }

    @Synchronized
    fun showTimeSelectionDialog() {
        val intent = Intent(context, TimePickerActivity::class.java)
        intent.putExtra("activeService", activeService!!.slug)
        startActivityForResult(intent, 4422)
    }

    private fun addCreditCard() {
        model.addCreditCardActivity(this@CheckoutActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 4422) {
                selectedDate = data!!.getStringExtra("selectedDate")
                model.deliveryDate = selectedDate
                deliveryTimeText.text = selectedDate
            } else if (requestCode == 1239) {
                if (model.hasUserDetails()) {
                    confirmButton.callOnClick()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        createPaymentList()
        refreshPromo()
        totalPriceText.text = model.total
        totalItemsText.text = model.getItemCount()
    }

    private fun refreshPromo() {
        if (model.promo != null) {
            promoCodeButton.text = getString(R.string.remove_promo_code)
        } else {
            promoCodeButton.text = getString(R.string.add_promode_code)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.notes = noteText.text.toString()
    }

    private fun placeOrder(po: PaymentOption, tip: TipOption?) {
        Utilities.showSpinner()
        val params = PurchaseOrderParam()
        params.storeId = activeService!!.id
        params.creditCardParam = po.creditCardParam
        params.paymentOptionUid = po.uid
        params.serviceUid = activeService!!.serviceProviderUid
        params.serviceName = activeService!!.serviceTitle
        params.orderUid = orderUid
        params.total = Utilities.formatCurrencyToDouble(model.totalPrice, activeService!!.currency)
        params.cashCollection = Utilities.formatCurrencyToDouble(model.amountDue, activeService!!.currency)
        params.subTotal = Utilities.formatCurrencyToDouble(model.subtotalPrice, activeService!!.currency)
        params.deductionFromWallet = model.deductionFromWalletInServiceCurrency
        if (tip != null) params.tips = tip.tipValue
        if (model.deliveryVatAmount > 0) {
            params.deliveryVat = Utilities.formatCurrencyToDouble(model.deliveryVatAmount, activeService!!.currency)
        }
        if (model.deliveryVatAmount > 0) {
            params.deliveryVat = Utilities.formatCurrencyToDouble(model.deliveryVatAmount, activeService!!.currency)
        }
        params.currencySymbol = activeService!!.currencySymbol
        params.discountType = activeService!!.discountType
        params.discountValue = activeService!!.discountValue
        if (model.promo != null) {
            params.discountType = model.promo.discountType
            params.discountValue = model.promo.discountValue
        }
        params.minimumChargeFee = model.surcharge
        params.hasPricing = activeService!!.getHasPricing()
        params.deliveryCharge = model.deliveryPrice
        params.deliveryDistanceKm = model.deliveryDistance
        if (activeService!!.getHasPickup() && model.pickupEddress != null) {
            if (model.pickupEddress.code == "PIN_LOCATION") {
                params.pickupTempName = model.pickupEddress.label
                params.pickupTempNumber = model.pickupEddress.phoneNumber.replace("\\s".toRegex(), "")
            }
            params.pickupEddress = model.pickupEddress.code
            params.pickupLat = model.pickupEddress.lat
            params.pickupLon = model.pickupEddress.lon
        }
        if (activeService!!.getHasDelivery() && model.deliveryEddress != null) {
            if (model.deliveryEddress.code == "PIN_LOCATION") {
                params.deliveryTempName = model.deliveryEddress.label
                //                params.deliveryTempNumber = model.deliveryEddress.phoneNumber.replaceAll("\\s","");
            }
            params.deliveryEddress = model.deliveryEddress.code
            params.deliveryLat = model.deliveryEddress.lat
            params.deliveryLon = model.deliveryEddress.lon
        }
        params.deliveryDate = model.deliveryDate
        params.promiseTime = model.promiseTime

//        promoCodeText = findViewById(R.id.promoCodeText);
        if (model.promoPending != null && model.promoPending.promoCode != null) {
            params.promoCode = model.promoPending.promoCode
        }

//        EditText recipientText = findViewById(R.id.recipientText);
//        if (recipientText.getText().toString().length() > 0) {
//            params.deliveryRecipient =recipientText.getText().toString();
//        }
        if (activeService!!.getHasNotes()) {
            val note = findViewById<EditText>(R.id.checkoutNotes)
            if (note.text.toString().isNotEmpty()) {
                params.notes = note.text.toString()
            }
        }
        val items: MutableList<PurchaseOrderItemObject> = ArrayList()
        for (menuItem in model.cart) {
            val purchaseOrderItem = PurchaseOrderItemObject()
            purchaseOrderItem.itemPrice = menuItem.totalPrice
            purchaseOrderItem.uid = menuItem.uid
            purchaseOrderItem.uidProduct = menuItem.id
            purchaseOrderItem.sku = menuItem.sku
            purchaseOrderItem.label = menuItem.label
            purchaseOrderItem.imageUrl = menuItem.imageUrl
            purchaseOrderItem.description = menuItem.description
            purchaseOrderItem.category = menuItem.category
            purchaseOrderItem.subCategory = menuItem.subcategory
            purchaseOrderItem.marketProductItemId = menuItem.id
            purchaseOrderItem.isVariablePrice = menuItem.isVariablePrice
            purchaseOrderItem.customizationItems = menuItem.customizationItems
            if (menuItem.isSingleSelection) {
                if (menuItem.isItemSelected) {
                    purchaseOrderItem.quantity = 1.0
                }
            } else {
                if (menuItem.itemsOrdered > 0) {
                    purchaseOrderItem.quantity = java.lang.Double.valueOf(menuItem.itemsOrdered.toDouble())
                }
            }
            items.add(purchaseOrderItem)
        }
        params.items = items
        model.saveCartToStorage(context)
        Rest.request().uri("placeOrderNew").timeOut(-1).params(params).response(
            PurchaseOrderResponseBean::class.java,
            Response.Listener label@{ responseBean: PurchaseOrderResponseBean ->
                if (responseBean.missingItems != null) {
                    Utilities.hideSpinner()
                    model.displayMissingItems(this@CheckoutActivity, responseBean.missingItems)
                    return@label
                } else if (responseBean.data == null) {
                    Utilities.hideSpinner()
                    return@label
                }
                model.loadRecentlyOrdered = true
                model.activePurchaseOrder = responseBean.data
                if (model.deductionFromWalletInServiceCurrency != null && model.deductionFromWalletInServiceCurrency > 0) {
                    if (model.amountInWallet != null && model.amountInWallet > 0) {
                        val deductionInTenantCurrency: Double
                        if (model.tenantCurrency.equals(activeService!!.currency.iso, ignoreCase = true)) {
                            deductionInTenantCurrency = model.deductionFromWalletInServiceCurrency
                        } else {
                            if (model.exchangeRate == null || model.exchangeRate == 0) model.exchangeRate = 1
                            val amountInDollar =
                                model.deductionFromWalletInServiceCurrency / activeService!!.currency.exchangeRate
                            deductionInTenantCurrency = amountInDollar * model.exchangeRate
                        }
                        if (model.amountInWallet >= deductionInTenantCurrency) {
                            model.amountInWallet -= deductionInTenantCurrency
                        } else {
                            model.amountInWallet = 0.0
                        }
                    }
                    model.credibilityChecked = false
                }
                val activity = Intent(this@CheckoutActivity, OrderCompletedActivity::class.java)
                activity.putExtra("successMessage", true)
                model.promoPending = null
                model.clearCart(true)
                saveValue("continueFinish", false)
                Utilities.hideSpinner()
                orderCompleted(orderUid!!, activeService!!)
                if (model.promo != null) {
                    couponApplied(orderUid!!, model.promo.uid, model.promo.promoCode, model.discount)
                }
                this@CheckoutActivity.startActivity(activity)
                finish()
            } as Response.Listener<PurchaseOrderResponseBean>).error { errorResource: ErrorResource ->
            Utilities.hideSpinner()
            val title =
                if (errorResource.title != null) errorResource.title else getString(R.string.something_went_wrong)
            val description =
                if (errorResource.description != null) errorResource.description else getString(R.string.pls_try_again)
            Utilities.Error(title, description)
        }.post()
    }

    fun updateDeliveryAddress() {
        if (activeService == null) return
        if (activeService!!.isCourier) return
        deliverTo(activeService!!.hasPickup)
    }

    private fun deliverTo(isPickup: Boolean) {
        ViewRouter.instance().showAddressPopup(
            this@CheckoutActivity,
            R.string.change_location,
            activeService!!.slug,
            isPickup,
            true,
            object : IClickListener<AddressObject> {
                override fun click(`object`: AddressObject?) {
                    if (model.recalculateDeliveryPrice && model.hasDynamicPricing(activeService)) {
                        calcDynamicDeliveryCharge()
                    }
                    if (isPickup) {
                        model.pickupEddress = `object`
                    } else {
                        model.deliveryEddress = `object`
                    }
                    updateAddress()
                    if (`object`?.returnToMainView == true) {
                        `object`.returnToMainView = false
                        model.setAsCurrentAddress = `object`
                        saveValue("returnToMainView", true)
                        finish()
                    }
                }
            })
    }

    private fun hideTipOptions() {
        tipsContainer.visibility = View.GONE
        for (tipOption in tipOptions) {
            tipOption.isSelected = false
        }
        if (tipOptions.size > 0) {
//            tipsContainer.setVisibility(View.VISIBLE);
            if (tipOptionAdaptor != null) tipOptionAdaptor!!.notifyDataSetChanged()
        }
        model.tip = 0.0
        initializeBasket()
    }

    private fun showTipOptions() {
        if (tipOptions.size > 0) {
            tipsContainer.visibility = View.VISIBLE
        } else {
            tipsContainer.visibility = View.GONE
        }
    }

    var list: MutableList<IProductItem> = ArrayList()
    fun initializeBasket() {
        if (activeService!!.getDeliveryCharge() != null && activeService!!.getDeliveryCharge() > 0) {
            model.deliveryPrice = activeService!!.getDeliveryCharge()
        }
        model.calculateBasket(activeService)
        //        totalText.setText(Utilities.getCurrencyFormatter(activeService.currency).format(model.totalPrice));
        val model = ServicesModel.instance()
        val formatterShort = Utilities.getCurrencyFormatter(
            activeService!!.currency, true
        )
        val formatter = Utilities.getCurrencyFormatter(
            activeService!!.currency
        )
        list.clear()
        if (model.subtotalPrice != model.amountDue || model.promo != null) {
            list.add(
                BasketItem(
                    getString(R.string.sub_total),
                    formatterShort.format(model.subtotalPrice),
                    BasketItem.TOTAL
                )
            )
        }
        if (model.hasDiscountOnSubtotal(activeService)) {
            list.add(
                BasketItem(
                    getString(R.string.discount) + model.discountDecription,
                    "-" + formatterShort.format(model.discount),
                    BasketItem.SPECIAL
                )
            )
        }
        if (model.deliveryPrice > 0) {
            val item = BasketItem(
                activeService!!.getDeliveryFeeLabel() + " " + if (model.deliveryDistanceLabel != null) model.deliveryDistanceLabel else "",
                formatterShort.format(model.deliveryPrice),
                BasketItem.CHARGES
            )
            list.add(item)
        }
        if (model.surcharge > 0) {
            val item = BasketItem(
                getString(R.string.surcharge_for_orders) + formatterShort.format(
                    activeService!!.minimumCharge
                ), formatterShort.format(model.surcharge), BasketItem.SPECIAL
            )
            list.add(item)
        }
        if (model.deliveryVatAmount > 0) {
            val item = BasketItem(
                getString(R.string.delivery_vat),
                formatterShort.format(model.deliveryVatAmount),
                BasketItem.CHARGES
            )
            list.add(item)
        }
        if (model.hasDiscountOnDelivery(activeService)) {
            list.add(
                BasketItem(
                    getString(R.string.discount) + model.discountDecription,
                    "-" + formatterShort.format(model.discount),
                    BasketItem.SPECIAL
                )
            )
        }
        if (model.tip > 0) {
            list.add(
                BasketItem(
                    getString(R.string.tips_for_driver),
                    formatterShort.format(model.tip),
                    BasketItem.CHARGES
                )
            )
        }
        list.add(BasketItem(getString(R.string.total), formatter.format(model.totalPrice), BasketItem.TOTAL))
        if (model.hasDiscountOnTotal(activeService)) {
            list.add(
                BasketItem(
                    getString(R.string.discount) + model.discountDecription,
                    "-" + formatterShort.format(model.discount),
                    BasketItem.SPECIAL
                )
            )
        }
        if (model.deductionFromWalletInServiceCurrency > 0) {
            list.add(
                BasketItem(
                    getString(R.string.wallet),
                    "-" + formatter.format(model.deductionFromWalletInServiceCurrency),
                    BasketItem.SPECIAL
                )
            )
        }
        if (model.hasDiscountOnTotal(activeService) || model.deductionFromWalletInServiceCurrency > 0) {
            list.add(BasketItem(getString(R.string.amount_due), formatter.format(model.amountDue), BasketItem.TOTAL))
        }
        basketAdapter!!.notifyDataSetChanged()
        refreshPromo()
    }

    private fun displayPaymentOptions() {
        val alertDialog = NokListAlertDialog<PaymentOption>(this@CheckoutActivity)
        alertDialog.setCancelable(true)
        alertDialog.setItems(ArrayList<IListItem>(paymentOptionsList))
        alertDialog.setTitle(getString(R.string.select_payment_method))
        alertDialog.setItemClickListener { item: PaymentOption ->
            if (item.paymentMethod == "ADDNEW") {
                addCreditCard()
            } else {
                model.paymentOption = item
                LocalStorage.instance().save(context, "paymentOption", item.name)
                updatePaymentView()
            }
        }
        alertDialog.show()
    }

    private fun createPaymentList() {
        val payment = LocalStorage.instance().getString("paymentOption")
        if (paymentOptionsList == null) paymentOptionsList = ArrayList()
        paymentOptionsList!!.clear()
        if (activeService != null && activeService!!.paymentOptions == null) activeService!!.paymentOptions =
            ArrayList()
        var onlinePaymentOptionEnabled = false
        if (activeService == null) return
        for (paymentOption in activeService!!.paymentOptions) {
            if (paymentOption.paymentMethod == "CREDIT" && model.creditCardsEnabled) {
                if (model.creditCards != null) {
                    for (bean in model.creditCards) {
                        val item =
                            PaymentOption("CREDIT", bean.cardNumber, CreditCardParam(bean.uid, getStringValue("email")))
                        item.setContext(this)
                        paymentOptionsList!!.add(item)
                        if (model.paymentOption == null && item.name == payment) {
                            model.paymentOption = item
                        }
                    }
                }
                onlinePaymentOptionEnabled = true
                break
            }
        }
        for (paymentOption in activeService!!.paymentOptions) {
            if (paymentOption.paymentMethod != "CREDIT") {
                paymentOption.setContext(this)
                paymentOptionsList!!.add(paymentOption)
                if (model.paymentOption == null && paymentOption.name == payment) {
                    model.paymentOption = paymentOption
                }
            }
        }
        if (onlinePaymentOptionEnabled) {
            val paymentOption = PaymentOption("ADDNEW", getString(R.string.add_online_payment), null)
            paymentOption.setContext(this)
            paymentOptionsList!!.add(paymentOption)
        }
        updatePaymentView()
    }

    private fun updatePaymentView() {
        showTipOptions()
        if (model.paymentOption == null) {
            hideTipOptions()
            paymentText.text = getString(R.string.select_payment)
        } else {
            paymentText.text = model.paymentOption.getName()
            paymentImage.setImageResource(model.paymentOption.icon)
            if (model.paymentOption.isCash) {
                hideTipOptions()
            }
        }
    }

    var promoCodeLayout: PromoCodeLayout<*>? = null


    private fun displayPromoBox() {
        if (model.promo != null) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.remove_promo_code_confirmation))
            builder.setPositiveButton(getString(R.string.option_yes)) { _: DialogInterface?, _: Int ->
                model.promo = null
                initializeBasket()
            }
            builder.setNegativeButton(getString(R.string.option_no)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            builder.show()
            return
        }
        promoCodeLayout = PromoCodeLayout<Any?>(this)
        promoCodeLayout?.setConfirmClickListener { promo: String -> applyPromo(promo) }
        promoCodeLayout?.show()
    }

    private fun applyPromo(promo: String) {
        Services.call(this@CheckoutActivity).result(object : ServiceResult<PromoResultBean?> {
            override fun onResult(response: PromoResultBean?) {
                if (response != null) {
                    if (response.uid == null) response.uid = response.promoCode
                    couponEntered(orderUid!!, response.uid, response.promoCode)
                    model.promo = response
                }
                initializeBasket()
                if (promoCodeLayout != null) promoCodeLayout!!.hide()
            }

            override fun onError(errorResource: ErrorResource) {
                if (errorResource.data != null) {
                    val resultBean = errorResource.data as PromoResultBean
                    if (resultBean.uid != null) couponDenied(
                        orderUid!!,
                        resultBean.uid,
                        resultBean.promoCode,
                        errorResource.description
                    )
                }
                Utilities.Error(getString(R.string.invalid_promo), errorResource.description)
            }
        }).applyPromo(promo, activeService!!.serviceProviderUid, true)
    }
}
