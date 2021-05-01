package com.skylarksit.module.ui.activities.hyperlocal

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skylarksit.module.R
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.lists.main.adapters.BasketAdapter
import com.skylarksit.module.ui.model.BasketItem
import com.skylarksit.module.ui.model.IProductItem
import com.skylarksit.module.ui.model.ViewRouter
import com.skylarksit.module.ui.utils.ItemClickListener
import com.skylarksit.module.ui.utils.OnOneOffClickListener
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.NonScrollingManager
import com.skylarksit.module.utils.Utilities
import java.text.NumberFormat
import java.util.*

class MissingItemsActivity : MyAppCompatActivity() {
    private var formatter: NumberFormat? = null
    lateinit var title: TextView
    lateinit var nextButton: View
    lateinit var recyclerView: RecyclerView
    var activeService: ServiceObject? = null
    lateinit var specialNotes: TextView
    lateinit var adapter: BasketAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.basket_activity)
        title = findViewById(R.id.title)
        nextButton = findViewById(R.id.bottomBar)
        recyclerView = findViewById(R.id.recyclerView)
        specialNotes = findViewById(R.id.specialNotes)
        activeService = model.cartService
        if (activeService != null) {
            formatter = Utilities.getCurrencyFormatter(activeService!!.currency)
            nextButton.setOnClickListener(object : OnOneOffClickListener() {
                override fun onSingleClick(v: View) {
                    onNextButtonClick()
                }
            })
            adapter = BasketAdapter(context, list)
            adapter.setClickListener(object : ItemClickListener<IProductItem>(adapter) {
                override fun onClick(item: IProductItem, position: Int) {}
            })
            recyclerView.layoutManager = NonScrollingManager(context)
            recyclerView.adapter = adapter
        }
    }

    var list: MutableList<IProductItem> = ArrayList()
    private fun initializeBasket() {
        val formatterShort = Utilities.getCurrencyFormatter(
            activeService!!.currency, true
        )
        val formatter = Utilities.getCurrencyFormatter(
            activeService!!.currency
        )
        list.clear()
        list.addAll(model.cart)
        val minCharge = activeService!!.minimumCharge
        val minChargeFee = activeService!!.minimumChargeFee
        list.add(BasketItem(getString(R.string.sub_total), formatter.format(model.subtotalPrice), BasketItem.TOTAL))
        var note = activeService!!.specialNote
        if (model.hasVariablePrice()) {
            note = getString(R.string.items_price_may_vary)
        }
        if (minCharge != null && minCharge >= 0 && (minChargeFee == null || minChargeFee == 0.0 && model.subtotalPrice < minCharge)) {
            note = getString(R.string.minimum_charge_is) + " " + formatterShort.format(
                activeService!!.minimumCharge
            )
        }
        if (Utilities.isEmpty(note)) {
            specialNotes.visibility = View.GONE
        } else {
            specialNotes.visibility = View.VISIBLE
            specialNotes.text = note
        }
        adapter.notifyDataSetChanged()
    }

    fun onNextButtonClick() {
        if (model.cart.size == 0) {
            Utilities.Toast(getString(R.string.you_have_empty_basket))
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
        ViewRouter.instance().startCheckout(this@MissingItemsActivity, activeService)
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
