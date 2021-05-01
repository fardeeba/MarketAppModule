package com.skylarksit.module.ui.activities.hyperlocal

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.skylarksit.module.R
import com.skylarksit.module.analytics.Segment.cartViewed
import com.skylarksit.module.libs.alertdialog.NokAlertDialog
import com.skylarksit.module.pojos.CollectionData
import com.skylarksit.module.pojos.services.MenuItemObject
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.components.ProductListView
import com.skylarksit.module.ui.components.ProductListView.ListType
import com.skylarksit.module.ui.model.ServicesModel
import com.skylarksit.module.ui.model.ViewRouter
import com.skylarksit.module.ui.utils.IClickListener
import com.skylarksit.module.ui.utils.OnOneOffClickListener
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities
import java.text.NumberFormat
import java.util.*

class BasketActivity : MyAppCompatActivity() {
    private var formatter: NumberFormat? = null
    private lateinit var nextButton: View
    var activeService: ServiceObject? = null
    private lateinit var specialNotes: TextView
    private lateinit var specialNotesText: TextView
    private lateinit var basketProducts: ProductListView
    lateinit var basket: ProductListView
    private lateinit var totalPriceText: TextView
    private lateinit var totalItemsText: TextView

    private var displayCart: NokAlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.basket_activity)

        totalItemsText = findViewById(R.id.totalItems)
        totalPriceText = findViewById(R.id.totalPrice)
        basket = findViewById(R.id.basket)
        basketProducts = findViewById(R.id.basketProducts)
        specialNotesText = findViewById(R.id.specialNotesText)
        specialNotes = findViewById(R.id.specialNotes)
        nextButton = findViewById(R.id.bottomBar)



        activeService = model.cartService
        if (activeService != null) {
            cartViewed(activeService!!)
            model.cart.sortWith(Comparator sort@{ t0: MenuItemObject, t1: MenuItemObject ->
                val res = t0.subcategory.compareTo(t1.subcategory)
                if (res == 0) {
                    return@sort t0.label.compareTo(t1.label)
                }
                res
            })
            var viewType = ListType.GRID
            if ("list".equals(activeService!!.viewType, ignoreCase = true)) {
                viewType = ListType.VERTICAL
            }
            basket.initialize(
                getString(R.string.my_basket),
                model.cart,
                CollectionData("my_basket", "My basket", CollectionData.Type.BASKET),
                true,
                viewType,
                activeService!!.hideImages
            )
            basket.setUpdateListener(object : IClickListener<MenuItemObject> {
                override fun click(`object`: MenuItemObject?) {
                    initializeBasket()
                }
            })
            basket.setHideImages()
            basket.recyclerView?.isScrollbarFadingEnabled = false
            formatter = Utilities.getCurrencyFormatter(activeService!!.currency)
            nextButton.setOnClickListener(object : OnOneOffClickListener() {
                override fun onSingleClick(v: View) {
                    onNextButtonClick()
                }
            })
            val basketLabel = context.getString(R.string.bakset_items)
            if (activeService!!.showRecommendations()) {
                val basketItems = model.findItemsWithTag(activeService!!, "counter_item", false)
                basketItems.sortWith { t0: MenuItemObject, t1: MenuItemObject ->
                    t1.recommendationLevel.compareTo(
                        t0.recommendationLevel
                    )
                }
                val collectionData = CollectionData("basket_items", basketLabel, CollectionData.Type.RECOMMENDATION)
                basketProducts.initialize(
                    basketLabel, basketItems, collectionData,
                    fromProvider = true,
                    hideImages = false
                )
                basketProducts.setUpdateListener(object : IClickListener<MenuItemObject> {
                    override fun click(`object`: MenuItemObject?) {
                        initializeBasket()
                    }
                })
                basketProducts.visibility = if (basketItems.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun initializeBasket() {
        val model = ServicesModel.instance()
        model.calculateBasket(activeService)
        var note: String? = null
        if (model.hasVariablePrice()) {
            note = getString(R.string.items_price_may_vary)
        }
        if (activeService!!.hasMinimumCharge(model.subtotalPrice)) {
            note = if (Utilities.isMarketplace()) {
                getString(R.string.minum_charge_for) + activeService!!.name + ": " + formatter!!.format(
                    activeService!!.minimumCharge
                )
            } else {
                getString(R.string.minum_charge) + formatter!!.format(activeService!!.minimumCharge)
            }
        }
        if (Utilities.isEmpty(note)) {
            specialNotes.visibility = View.GONE
        } else {
            specialNotes.visibility = View.VISIBLE
            specialNotes.text = note
        }
        if (Utilities.isEmpty(activeService!!.specialNote)) {
            specialNotesText.visibility = View.GONE
        } else {
            specialNotesText.visibility = View.VISIBLE
            specialNotesText.text = activeService!!.specialNote
        }
        totalPriceText.text = model.subTotal
        totalItemsText.text = model.getItemCount()
        basket.actionClick = object : IClickListener<Any> {
            override fun click(`object`: Any?) {
                clearCart()
            }
        }
        basket.setSubtitle(context.getString(R.string.clear))
        basket.refresh()
        basket.visibility = if (model.isCartUsed) View.VISIBLE else View.GONE
        //        basketBottomButton.setVisibility(model.isCartUsed() && model.getCartService().showItemSuggestion ? View.VISIBLE : View.GONE);
    }

    private fun clearCart() {
        if (displayCart != null) displayCart!!.hide()
        displayCart = NokAlertDialog(this@BasketActivity, NokAlertDialog.WARNING)
            .setTitle(context.getString(R.string.title_clear_cart))
            .setConfirmText(context.getString(R.string.yes_clear_cart))
            .showCancelButton(true)
            .setCancelText(context.getString(R.string.dont_clear_cart))
            .setConfirmClickListener {
                model.clearCart(true)
                initializeBasket()
            }
        displayCart?.show()
    }

    fun onNextButtonClick() {
        if (model.closeMerchants) {
            Utilities.Error(getString(R.string.service_closed), "")
            return
        }
        if (model.cart.size == 0) {
            Utilities.Error(getString(R.string.empty_basket), "")
            return
        }
        if (!activeService!!.hasTimeSchedule && activeService!!.isClosed()) {
            Utilities.Error(getString(R.string.store_is_closed), activeService!!.openingTime)
            return
        }
        val minCharge = activeService!!.minimumCharge
        val minChargeFee = activeService!!.minimumChargeFee
        if (minCharge != null && minCharge >= 0) {
            if (model.subtotalPrice < minCharge && (minChargeFee == null || minChargeFee == 0.0)) {
                Utilities.Toast(getString(R.string.need_to_exceed_minimum_charge) + formatter!!.format(minCharge))
                return
            }
        }
        ViewRouter.instance().startCheckout(this@BasketActivity, activeService)
    }

    override fun onResume() {
        super.onResume()
        if (activeService == null || !model.isCartUsed) {
            model.clearCart(false)
            finish()
            return
        }
        initializeBasket()
    }
}
