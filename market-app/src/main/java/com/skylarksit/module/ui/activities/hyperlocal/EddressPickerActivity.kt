@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.activities.hyperlocal

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fenchtose.tooltip.Tooltip
import com.skylarksit.module.R
import com.skylarksit.module.libs.alertdialog.NokAlertDialog
import com.skylarksit.module.pojos.AddressObject
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.activities.main.EditEddressActivity
import com.skylarksit.module.ui.lists.items.SearchResultItem
import com.skylarksit.module.ui.lists.main.adapters.SearchResultAdapter
import com.skylarksit.module.ui.model.IListItem
import com.skylarksit.module.ui.utils.ItemClickListener
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities
import java.util.*

class EddressPickerActivity : MyAppCompatActivity() {
    private var searchResultAdapter: SearchResultAdapter? = null
    private var searchResultItems: MutableList<IListItem>? = null
    private var isPickup = false
    private var activeService: ServiceObject? = null
    private var returnToMainView = false
    lateinit var subtitle: View
    private lateinit var addNewAddress: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eddress_picker)
        subtitle = findViewById(R.id.subtitle)
        addNewAddress = findViewById(R.id.addNewAddress)
        addNewAddress.setOnClickListener { newLocationHandler() }
        returnToMainView = intent.getBooleanExtra("returnToMainView", false)
        val slug = intent.getStringExtra("activeService")
        if (slug != null) activeService = model.findProviderByName(slug)
        val searchListView = findViewById<RecyclerView>(R.id.searchResultList)
        searchResultItems = ArrayList()
        searchResultAdapter = SearchResultAdapter(context, searchResultItems, false, false)
        searchResultAdapter!!.setClickListener(object : ItemClickListener<SearchResultItem?>(searchResultAdapter!!) {
            override fun onClick(item: SearchResultItem?, position: Int) {
                if (item?.type == "REQUEST_LOCATION") {
                    sendLocationRequest()
                } else if (item?.type == "EDDRESS") {
                    selectMyEddress(item)
                }
            }
        })
        searchListView.layoutManager = LinearLayoutManager(this)
        searchResultAdapter!!.setActionButton(object : ItemClickListener<SearchResultItem?>(searchResultAdapter!!) {
            override fun onClick(item: SearchResultItem?, position: Int) {
                val `object` = item?.data as AddressObject
                val eddressDetailsActivity = Intent(this@EddressPickerActivity, EditEddressActivity::class.java)
                eddressDetailsActivity.putExtra("isHLS", activeService != null)
                eddressDetailsActivity.putExtra(
                    "activeService",
                    if (activeService != null) activeService!!.slug else null
                )
                Utilities.activeAddressObject = `object`
                this@EddressPickerActivity.startActivity(eddressDetailsActivity)
            }
        })
        isPickup = intent.getBooleanExtra("isPickup", false)
        searchListView.adapter = searchResultAdapter
        val button = findViewById<View>(R.id.addNewAddress)
        addTooltip(button, R.string.tip_add_location, Tooltip.TOP)
        //        subtitle.setVisibility(activeService != null ? View.VISIBLE : View.GONE);
        subtitle.visibility = View.GONE
        showMyAddresses()
        if (searchResultItems!!.size == 0) {
            newLocationHandler()
        }

//        addNewAddress.setVisibility(activeService != null ? View.GONE : View.VISIBLE );
    }

    private fun selectMyEddress(item: SearchResultItem) {
        val selectedAddress = item.data as AddressObject
        val serviceObject = model.cartService
        if (serviceObject != null) {
            if (model.isCartUsed && !serviceObject.deliversTo(selectedAddress.lat, selectedAddress.lon)) {
                val alert = NokAlertDialog(this@EddressPickerActivity)
                    .setTitle(context.getString(R.string.confirm_change_address))
                alert.setConfirmText(context.getString(R.string.proceed))
                alert.setConfirmClickListener {
                    model.deliveryEddress = selectedAddress
                    model.recalculateDeliveryPrice = true
                    returnToMainView = true
                    confirmSelectedAddress(selectedAddress, true)
                }.show()
                return
            }
            confirmSelectedAddress(selectedAddress, true)
        } else {
            confirmSelectedAddress(selectedAddress, false)
        }
    }

    private fun confirmSelectedAddress(addressObject: AddressObject, isHls: Boolean) {
        if (isHls) {
            if (isPickup) {
                model.pickupEddress = addressObject
            } else {
                model.deliveryEddress = addressObject
            }
            model.recalculateDeliveryPrice = true
            if (model.currentAddress !== addressObject) {
                model.setAsCurrentAddress = addressObject
            }
        }
        val returnIntent = Intent()
        returnIntent.putExtra("addressCode", addressObject.code)
        setResult(RESULT_OK, returnIntent)
        if (returnToMainView) {
            model.setAsCurrentAddress = addressObject
            saveValue("returnToMainView", true)
        }
        finish()
    }

    fun sendLocationRequest() {
        val uri = Uri.parse("content://contacts")
        val intent = Intent(Intent.ACTION_PICK, uri)
        intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        startActivityForResult(intent, PICK_CONTACT)
    }

    // Listen for results.
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        // See which child activity is calling us back.
        if (requestCode == PICK_CONTACT) {
            if (resultCode == RESULT_OK) {
                try {
                    val uri = intent!!.data
                    val projection = arrayOf(
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    )
                    assert(uri != null)
                    val cursor = contentResolver.query(uri!!, projection, null, null, null)!!
                    cursor.moveToFirst()
                    val numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val number = cursor.getString(numberColumnIndex)
                    val nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val name = cursor.getString(nameColumnIndex)
                    cursor.close()
                    if (number == null || name == null) {
                        Toast.makeText(context, getString(R.string.invalid_contact), Toast.LENGTH_LONG).show()
                    }
                    AlertDialog.Builder(this@EddressPickerActivity)
                        .setMessage(getString(R.string.once_the_order_is_placed) + name + getString(R.string.will_receive_sms_to_pin_location))
                        .setPositiveButton(android.R.string.yes) { _: DialogInterface?, _: Int ->
                            val addressObject = AddressObject()
                            addressObject.code = "PIN_LOCATION"
                            //                                        addressObject.addressName = name;
                            addressObject.phoneNumber = number
                            if (isPickup) model.pickupEddress = addressObject else {
                                model.deliveryEddress = addressObject
                            }
                            finish()
                        }
                        .setNegativeButton(android.R.string.no, null)
                        .show()
                } catch (ignored: Exception) {
                }
            }
        }
    }

    private fun showMyAddresses() {
        searchResultItems!!.clear()
        for (addressObject in model.myLocations) {
            if (activeService != null && activeService!!.isCourier) {
                if (!activeService!!.deliversTo(addressObject.lat, addressObject.lon)) {
                    continue
                }
            }
            val item = SearchResultItem(addressObject)
            if (activeService != null) {
                item.actionButtonLabel = null
            } else {
                item.actionButtonLabel = "Edit"
            }
            searchResultItems!!.add(item)
            item.colorizeIcon = true
        }
        searchResultAdapter!!.notifyDataSetChanged()
    }

    override fun onResume() {
        if (getBooleanValue("continueFinish")) {
            saveValue("continueFinish", false)
            finish()
        }
        showMyAddresses()
        super.onResume()
    }

    fun newLocationHandler() {
        Utilities.addNewEddress(
            this,
            activeService != null,
            isPickup,
            true,
            if (activeService != null) activeService!!.slug else null,
            returnToMainView
        )
    }

    companion object {
        private const val PICK_CONTACT = 123
    }
}
