package com.skylarksit.module.ui.activities.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skylarksit.module.R
import com.skylarksit.module.pojos.AddressObject
import com.skylarksit.module.ui.lists.items.SearchResultItem
import com.skylarksit.module.ui.lists.main.adapters.SearchResultAdapter
import com.skylarksit.module.ui.model.IListItem
import com.skylarksit.module.ui.utils.ItemClickListener
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities
import java.util.*

class MyAddressesActivity : MyAppCompatActivity() {

    private lateinit var profileList: RecyclerView
    private lateinit var addNewAddress: View
    private val profileListItems: MutableList<IListItem> = ArrayList()
    var profileListAdapter: SearchResultAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_addresses)
        profileList = findViewById(R.id.profileList)
        addNewAddress = findViewById(R.id.addNewAddress)

        addNewAddress.setOnClickListener { addNewAddressButtonTapped() }
        profileList.layoutManager = LinearLayoutManager(this)
        profileListAdapter = SearchResultAdapter(context, profileListItems, false, false)
        profileListAdapter!!.setClickListener(object : ItemClickListener<SearchResultItem?>(profileListAdapter!!) {
            override fun onClick(item: SearchResultItem?, position: Int) {
                if (item == null) return
                if ("EDDRESS".equals(item.type, ignoreCase = true)) {
                    saveValue("continueFinish", false)
                    if (item.data == null) {
                        Utilities.addNewEddress(this@MyAddressesActivity, false, null, false)
                        return
                    }
                    Utilities.activeAddressObject = item.data as AddressObject
                    val eddressDetailsActivity = Intent(this@MyAddressesActivity, EditEddressActivity::class.java)
                    startActivity(eddressDetailsActivity)
                }
            }
        })
        profileList.adapter = profileListAdapter
        initializeMyProfileList()
    }

    override fun onResume() {
        super.onResume()
        initializeMyProfileList()
    }


    private fun addNewAddressButtonTapped() {
        saveValue("continueFinish", false)
        Utilities.addNewEddress(this@MyAddressesActivity, false, null, false)
    }

    private fun initializeMyProfileList() {
        profileListItems.clear()
        for (addressObject in model.myLocations) {
            val item = SearchResultItem(addressObject)
            item.colorizeIcon = true
            profileListItems.add(item)
        }
        if (model.myLocations.size == 0) {
            val item = SearchResultItem()
            item.label = getString(R.string.new_address)
            item.description = getString(R.string.add_first_address)
            item.icon = R.drawable.plus_icon
            item.type = "EDDRESS"
            item.colorizeIcon = true
            profileListItems.add(item)
            addNewAddress.visibility = View.GONE
        } else {
            addNewAddress.visibility = View.VISIBLE
        }
        profileListAdapter!!.notifyDataSetChanged()
    }
}
