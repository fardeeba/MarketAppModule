package com.skylarksit.module.ui.activities.hyperlocal

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.skylarksit.module.R
import com.skylarksit.module.lib.Rest
import com.skylarksit.module.pojos.services.PurchaseOrderObject
import com.skylarksit.module.ui.lists.main.adapters.OrderHistoryAdapter
import com.skylarksit.module.ui.utils.ItemClickListener
import com.skylarksit.module.utils.MyAppCompatActivity
import java.util.*

class PurchaseOrdersActivity : MyAppCompatActivity() {
    private val items: MutableList<PurchaseOrderObject> = ArrayList()
    lateinit var listView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_orders)

        listView = findViewById(R.id.list)

        val adapter = OrderHistoryAdapter(this@PurchaseOrdersActivity, items)
        adapter.setClickListener(object : ItemClickListener<PurchaseOrderObject?>(adapter) {
            override fun onClick(item: PurchaseOrderObject?, position: Int) {
                model.activePurchaseOrder = null
                Rest.request().uri("getOrderDetails/" + item!!.uid).showLoader(getString(R.string.loader_text))
                    .response(
                        PurchaseOrderObject::class.java, Response.Listener { response: PurchaseOrderObject? ->
                            model.activePurchaseOrder = response
                            val orderCompletedActivity = Intent(context, OrderCompletedActivity::class.java)
                            this@PurchaseOrdersActivity.startActivity(orderCompletedActivity)
                        }).post()
            }
        })
        listView.adapter = adapter
        listView.layoutManager = LinearLayoutManager(this)
        Rest.request().uri("getOrderHistory/").showLoader(getString(R.string.loader_text)).response(
            Array<PurchaseOrderObject>::class.java, Response.Listener { response: Array<PurchaseOrderObject> ->
                if (!response.isNullOrEmpty()) {
                    items.clear()
                    response.toMutableList().let { items.addAll(it) }
                }

                adapter.notifyDataSetChanged()
            } as Response.Listener<Array<PurchaseOrderObject>>).post()
    }
}
