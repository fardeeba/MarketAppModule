package com.skylarksit.module.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.skylarksit.module.INavBarActivity
import com.skylarksit.module.MainActivity
import com.skylarksit.module.R
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.activities.hyperlocal.MainSearchFragment
import com.skylarksit.module.ui.activities.hyperlocal.PurchaseOrdersActivity
import com.skylarksit.module.ui.activities.main.FavoritesActivity
import com.skylarksit.module.ui.activities.main.ProfileActivity
import com.skylarksit.module.ui.model.ServicesModel
import com.skylarksit.module.ui.model.ViewRouter
import com.skylarksit.module.ui.utils.IClickListener
import com.skylarksit.module.ui.utils.OnOneOffClickListener
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities

class NavigationBar : RelativeLayout {
    private var activeService: ServiceObject? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    var activity: INavBarActivity? = null
    lateinit var cart: ImageView
    private var totalItemsText: TextView? = null
    private var historyButtonBubble: ImageView? = null
    private lateinit var favoritesButton: View
    private lateinit var profileButton: View
    private lateinit var homeButton: View
    private var homeButtonImage: ImageView? = null

    //    View searchButton;
    private lateinit var historyButton: View
    private var mInflater: LayoutInflater = LayoutInflater.from(context)
    var model: ServicesModel = ServicesModel.instance()

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        val view = mInflater.inflate(R.layout.naviagtion_bar, this, true)
        totalItemsText = view.findViewById(R.id.totalItems)
        historyButtonBubble = view.findViewById(R.id.historyButtonBubble)
        favoritesButton = view.findViewById(R.id.favoritesButton)
        profileButton = view.findViewById(R.id.profileButton)
        historyButton = view.findViewById(R.id.historyButton)
        homeButton = view.findViewById(R.id.homeButton)
        homeButtonImage = view.findViewById(R.id.homeButtonImage)
        cart = view.findViewById(R.id.cart)
        cart.setOnClickListener(object : OnOneOffClickListener() {
            override fun onSingleClick(v: View) {
                onCartClick()
            }
        })
        favoritesButton.setOnClickListener(object : OnOneOffClickListener() {
            override fun onSingleClick(v: View) {
                favoritesButtonClicked()
            }
        })
        historyButton.setOnClickListener(object : OnOneOffClickListener() {
            override fun onSingleClick(v: View) {
                onHistoryClick()
            }
        })
        homeButton.setOnClickListener(object : OnOneOffClickListener() {
            override fun onSingleClick(v: View) {
                homeButtonClicked()
            }
        })
        cart.setOnClickListener(object : OnOneOffClickListener() {
            override fun onSingleClick(v: View) {
                onCartClick()
            }
        })
        profileButton.setOnClickListener(object : OnOneOffClickListener() {
            override fun onSingleClick(v: View) {
                profileButtonClicked()
            }
        })
    }

    private fun onHistoryClick() {
        if (!Utilities.isLoggedIn()) {
            Utilities.alertSignInFirst(context, true)
            return
        }
        historyButtonBubble!!.visibility = GONE
        context.startActivity(Intent(context, PurchaseOrdersActivity::class.java))
    }

    fun displayHistoryBubble() {
        historyButtonBubble!!.visibility = VISIBLE
    }

    private fun homeButtonClicked() {
        if (activity == null || (activity as MyAppCompatActivity).isFinishing) return
        if (activity is MainActivity) {
            (activity as MainActivity).homePageRecycleView.smoothScrollToPosition(0)
        } else {
            (activity as MyAppCompatActivity).saveValue("returnToMainView", true)
            (activity as Activity).onBackPressed()
        }
    }

    fun onCartClick() {
        if (!Utilities.isLoggedIn()) {
            Utilities.alertSignInFirst(context, true)
            return
        }
        if (model.isCartUsed) ViewRouter.instance().goToBasket(context, model.cart[0].service, null) else {
            Utilities.Toast(context.getString(R.string.empty_basket))
        }
    }

    fun init(appCompatActivity: INavBarActivity, serviceObject: ServiceObject?) {
        activity = appCompatActivity
        activeService = serviceObject
        appCompatActivity.searchView.id = CONTENT_VIEW_ID
        refreshView()
    }

    fun profileButtonClicked() {
        if (Utilities.isLoggedIn()) {
            context.startActivity(Intent(context, ProfileActivity::class.java))
        } else {
            Utilities.alertSignInFirst(context, true)
        }
    }

    fun favoritesButtonClicked() {
        if (!Utilities.isLoggedIn()) {
            Utilities.alertSignInFirst(context, true)
            return
        }
        context.startActivity(Intent(context, FavoritesActivity::class.java))
    }

    fun refreshView() {
        model.calcuateTotal()
        if (totalItemsText != null) {
            if (model.isCartUsed) {
                cart.background =
                    ContextCompat.getDrawable(context, R.drawable.circle_secondary)
                cart.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_ATOP)
                totalItemsText!!.visibility = VISIBLE
                totalItemsText!!.text = model.itemCount.toString()
            } else {
                cart.background =
                    ContextCompat.getDrawable(context, R.drawable.circle_grey_light)
                cart.setColorFilter(
                    ContextCompat.getColor(context, R.color.navigationBarBasket),
                    PorterDuff.Mode.SRC_ATOP
                )
                totalItemsText!!.visibility = GONE
            }
        }
        if (searchFragment != null) searchFragment!!.searchAdapter.notifyDataSetChanged()
        homeButtonImage!!.setImageDrawable(context.getDrawable(R.drawable.home_icon))
    }

    private var searchFragment: MainSearchFragment? = null
    fun onSearchClick() {
        activity!!.searchView.visibility = VISIBLE
        val fm = (activity as AppCompatActivity?)!!.supportFragmentManager
        val ft = fm.beginTransaction()
        searchFragment = MainSearchFragment()
        searchFragment!!.setClearSearchCallback(object : IClickListener<Any> {
            override fun click(`object`: Any?) {
                onClearSearch()
            }
        })
        searchFragment!!.setUpdateCartCallback(object : IClickListener<Any> {
            override fun click(`object`: Any?) {
                refreshView()
            }
        })
        searchFragment!!.setActiveService(activeService)
        searchFragment!!.setSearchTitle(
            if (Utilities.isMarketplace()) context.getString(R.string.search_shops_and_products) else context.getString(
                R.string.search_brands_and_products
            )
        )
        ft.add(CONTENT_VIEW_ID, searchFragment!!)
        ft.commit()
    }

    fun onClearSearch(): Boolean {
        activity!!.searchView.visibility = VISIBLE
        val fm = (activity as AppCompatActivity?)!!.supportFragmentManager
        val fragment = fm.findFragmentById(CONTENT_VIEW_ID)
        val ft = fm.beginTransaction()
        if (fragment == null) return false
        ft.remove(fragment)
        ft.commit()
        return true
    }

    companion object {
        const val CONTENT_VIEW_ID = 112002
    }
}
