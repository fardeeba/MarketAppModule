package com.skylarksit.module.ui.activities.main

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skylarksit.module.R
import com.skylarksit.module.lib.Rest
import com.skylarksit.module.libs.alertdialog.NokAlertDialog
import com.skylarksit.module.pojos.ResponseBean
import com.skylarksit.module.pojos.services.CreditCardBean
import com.skylarksit.module.ui.lists.items.SearchResultItem
import com.skylarksit.module.ui.lists.main.adapters.SearchResultAdapter
import com.skylarksit.module.ui.model.IListItem
import com.skylarksit.module.ui.utils.ItemClickListener
import com.skylarksit.module.utils.MyAppCompatActivity
import java.util.*

class MyPaymentMethodsActivity : MyAppCompatActivity() {

    private lateinit var paymentMethodsList: RecyclerView
    private lateinit var addCreditCard: Button
    private val profileListItems: MutableList<IListItem> = ArrayList()
    var profileListAdapter: SearchResultAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_payment_methods)
        paymentMethodsList = findViewById(R.id.paymentMethodsList)
        addCreditCard = findViewById(R.id.addCreditCard)
        addCreditCard.setOnClickListener { addNewPaymentMethodButtonTapped() }
        paymentMethodsList.layoutManager = LinearLayoutManager(this)
        profileListAdapter = SearchResultAdapter(context, profileListItems, false, false)
        profileListAdapter!!.setClickListener(object : ItemClickListener<SearchResultItem?>(profileListAdapter!!) {
            override fun onClick(item: SearchResultItem?, position: Int) {
                if (item == null) return
                if ("CARD".equals(item.type, ignoreCase = true)) {
                    if (getString(R.string.new_card).equals(item.label, ignoreCase = true)) {
                        model.addCreditCardActivity(this@MyPaymentMethodsActivity)
                        return
                    }
                    NokAlertDialog(this@MyPaymentMethodsActivity).setTitle(getString(R.string.title_delete_credit_card))
                        .setMessage(
                            getString(
                                R.string.are_you_sure_to_delete_card
                            ) + item.getLabel() + "?"
                        ).setCancelText(getString(R.string.nevermind)).setConfirmText(
                            getString(
                                R.string.confirm_yes_delete_it
                            )
                        )
                        .setConfirmClickListener {
                            Rest.request().uri("deleteCreditCard/" + item.uid).showLoader(
                                getString(
                                    R.string.loading_text_deleting
                                )
                            ).response(
                                ResponseBean::class.java
                            ) { removeCardFromList(item.uid) }.post()
                        }.show()
                }
            }
        })
        paymentMethodsList.adapter = profileListAdapter
        initializeMyProfileList()
    }

    override fun onResume() {
        super.onResume()
        initializeMyProfileList()
    }

    private fun addNewPaymentMethodButtonTapped() {
        model.addCreditCardActivity(this@MyPaymentMethodsActivity)
    }

    private fun initializeMyProfileList() {
        profileListItems.clear()
        if (model.creditCardsEnabled) {
            if (model.creditCards != null && model.creditCards.size > 0) {
                for (item in model.creditCards) {
                    val si = SearchResultItem()
                    si.label = item.cardNumber
                    si.description = item.expiryDateText
                    si.icon = R.drawable.creditcard_icon
                    si.type = "CARD"
                    si.uid = item.uid
                    si.colorizeIcon = true
                    profileListItems.add(si)
                }
            }
            if (model.creditCards == null || model.creditCards.size == 0) {
                val item = SearchResultItem()
                item.label = getString(R.string.new_card)
                item.description = getString(R.string.add_payment_method)
                item.icon = R.drawable.plus_icon
                item.type = "CARD"
                item.colorizeIcon = true
                profileListItems.add(item)
                addCreditCard.visibility = View.GONE
            } else {
                addCreditCard.visibility = View.VISIBLE
            }
        }
        profileListAdapter!!.notifyDataSetChanged()
    }

    private fun removeCardFromList(cardUid: String) {
        var cardToDelete: CreditCardBean? = null
        for (cardBean in model.creditCards) {
            if (cardBean.uid.equals(cardUid, ignoreCase = true)) {
                cardToDelete = cardBean
                break
            }
        }
        if (cardToDelete != null) {
            model.creditCards.remove(cardToDelete)
        }
        initializeMyProfileList()
    }
}
