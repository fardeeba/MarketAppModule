@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.activities.hyperlocal

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skylarksit.module.R
import com.skylarksit.module.analytics.Segment.productsSearched
import com.skylarksit.module.analytics.Segment.searchViewed
import com.skylarksit.module.pojos.CollectionData
import com.skylarksit.module.pojos.services.MenuItemObject
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.components.ProductListView
import com.skylarksit.module.ui.model.IListItem
import com.skylarksit.module.ui.model.SectionHeaderItem
import com.skylarksit.module.ui.model.ServicesModel
import com.skylarksit.module.ui.model.ViewRouter
import com.skylarksit.module.ui.utils.IClickListener
import com.skylarksit.module.ui.utils.ItemClickListener
import com.skylarksit.module.ui.utils.ItemsGridViewAdapter
import com.skylarksit.module.utils.Utilities
import java.util.*

class MainSearchFragment : Fragment() {
    private var clearSearchCallback: IClickListener<*>? = null
    private var updateCartCallback: IClickListener<*>? = null


    lateinit var searchAdapter: ItemsGridViewAdapter<*>
    private val searchItems: MutableList<IListItem> = ArrayList()
    private var promoItems: List<MenuItemObject> = ArrayList()
    private var searchProducts: EditText? = null
    private var searchList: RecyclerView? = null
    lateinit var model: ServicesModel
    private var searchTitle: String? = null
    private var activeService: ServiceObject? = null
    var fromProvider = false
    private var searchImageRelativeLayout: View? = null
    private lateinit var promoListView: ProductListView
    private var collectionData: CollectionData? = null

    private var timer: Timer? = null
    fun getCollectionData(): CollectionData {
        return CollectionData("main_search", "Search", CollectionData.Type.SEARCH)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        model = ServicesModel.instance(context)
        val rootView = inflater.inflate(R.layout.main_search_view, container, false)
        searchViewed()
        searchList = rootView.findViewById(R.id.searchList)
        rootView.findViewById<RelativeLayout>(R.id.searchEditLayout)
        searchImageRelativeLayout = rootView.findViewById(R.id.searchImageRelativeLayout)
        searchProducts = rootView.findViewById(R.id.searchProducts)
        promoListView = rootView.findViewById(R.id.promoListView)
        val basketLabel = getString(R.string.search_promo_items)
        promoListView.setUpdateListener(object : IClickListener<MenuItemObject> {
            override fun click(`object`: MenuItemObject?) {
                initializeBasket()
            }
        })
        collectionData = CollectionData("basket_items", basketLabel, CollectionData.Type.RECOMMENDATION)
        searchProducts?.hint = searchTitle
        val clearSearch = rootView.findViewById<View>(R.id.clearSearch)
        clearSearch.setOnClickListener { onClearSearch() }
        searchAdapter = ItemsGridViewAdapter(activity, searchItems, "list", false, false)
        val clicker: ItemClickListener<MenuItemObject> = object : ItemClickListener<MenuItemObject>(searchAdapter) {
            override fun onClick(item: MenuItemObject, position: Int) {
                if (model.addToCart(
                        context,
                        item,
                        item.itemsOrdered + 1,
                        getCollectionData(),
                        object : IClickListener<Any> {
                            override fun click(`object`: Any?) {
                                updateCartCallback!!.click(null)
                                searchAdapter.notifyDataSetChanged()
                            }

                        }
                    )
                ) {
                    searchAdapter.notifyItemChanged(position)
                    updateCartCallback!!.click(null)
                }
            }
        }
        clicker.setClickTimeInterval(0)
        val remove: ItemClickListener<MenuItemObject> = object : ItemClickListener<MenuItemObject>(searchAdapter) {
            override fun onClick(item: MenuItemObject, position: Int) {
                if (model.addToCart(
                        context,
                        item,
                        item.itemsOrdered - 1,
                        getCollectionData(),
                        object : IClickListener<Any> {
                            override fun click(`object`: Any?) {
                                updateCartCallback!!.click(null)
                                searchAdapter.notifyDataSetChanged()
                            }

                        }
                    )
                ) {
                    searchAdapter.notifyItemChanged(position)
                    //                    parentActivity.updateCart();
                    updateCartCallback!!.click(null)
                }
            }
        }
        remove.setClickTimeInterval(0)
        searchAdapter.setHideImages(activeService != null && activeService!!.hideImages)
        searchAdapter.setQuickAddListener(clicker)
        searchAdapter.setQuickRemoveListener(remove)
        searchAdapter.setClickListener(object : ItemClickListener<IListItem>(searchAdapter) {
            override fun onClick(item: IListItem, position: Int) {
                if (item is MenuItemObject) {
                    ViewRouter.instance()
                        .displayProduct(activity, item, getCollectionData(), true, true, false, fromProvider)
                }
            }
        }, 50)
        searchAdapter.setServiceClickButtonListener(object : ItemClickListener<ServiceObject>(searchAdapter) {
            override fun onClick(item: ServiceObject, position: Int) {
                ViewRouter.instance().goToServiceProvider(
                    activity,
                    item.slug,
                    null,
                    null,
                    CollectionData("search", "Search", CollectionData.Type.SEARCH)
                )
            }
        })
        searchList!!.adapter = searchAdapter
        searchList!!.layoutManager = LinearLayoutManager(activity)
        searchList!!.setOnTouchListener { _: View?, _: MotionEvent? ->
            Utilities.hideKeyboard(
                activity
            )
            false
        }
        searchList!!.post {
            searchProducts?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequernce: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    // user is typing: reset already started timer (if existing)
                    if (timer != null) {
                        timer!!.cancel()
                    }
                }

                override fun afterTextChanged(editable: Editable) {
                    updateView()
                    if (editable.length < 2) {
                        searchItems.clear()
                        updateView()
                        return
                    }
                    timer = Timer()
                    timer!!.schedule(object : TimerTask() {
                        override fun run() {
                            AsyncSearch().execute(editable.toString().toLowerCase())
                            productsSearched(editable.toString())
                        }
                    }, 300) // 600ms delay before the timer executes the „run“ method from TimerTask
                }
            })
        }
        return rootView
    }

    private fun initializeBasket() {
//        parentActivity.updateCart();
        updateCartCallback!!.click(null)
        searchAdapter.notifyDataSetChanged()
    }

    fun setClearSearchCallback(clearSearchCallback: IClickListener<*>?) {
        this.clearSearchCallback = clearSearchCallback
    }

    fun setUpdateCartCallback(updateCartCallback: IClickListener<*>?) {
        this.updateCartCallback = updateCartCallback
    }


    @SuppressLint("StaticFieldLeak")
    internal inner class AsyncSearch : AsyncTask<String, Void, Void>() {
        override fun doInBackground(vararg strings: String): Void? {
            val searchText = strings[0]
            searchItems.clear()
            if (searchText.isNotEmpty()) {
                if (activeService == null && !model.singleStore) {
                    val providers = model.findServiceProviders(searchText)
                    if (providers.size > 0) {
                        val section = SectionHeaderItem("Merchants", null)
                        searchItems.add(section)
                        searchItems.addAll(providers)
                    }
                }
                val slug = if (activeService != null) activeService!!.slug else null
                val products = model.searchDatabase(searchText, slug)
                if (products.size > 0) {
                    val section = SectionHeaderItem("Products", null)
                    searchItems.add(section)
                    searchItems.addAll(products)
                }

//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (searchItems.isEmpty()) {
//                            String message = getString(R.string.no_result_found) + searchText + "'";
//                            if (noResultToast != null) {
//                                noResultToast.cancel();
//                            }
//                            noResultToast = Toast.makeText(ServicesModel.instance().appContext, message, Toast.LENGTH_LONG);
//                            noResultToast.show();
//                        }
//                    }
//                });
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            updateView()
        }
    }

    private fun updatePromoItems() {
        if (model.singleStore || activeService != null) {
            if (model.services.size > 0) {
                if (activeService == null) activeService = model.services[0]
                promoItems = model.findItemsWithTag(activeService!!, "search_item", false)
                @Suppress("JavaCollectionsStaticMethodOnImmutableList")
                Collections.sort(promoItems) { t0: MenuItemObject, t1: MenuItemObject ->
                    t1.recommendationLevel.compareTo(
                        t0.recommendationLevel
                    )
                }
                val hideImages = activeService != null && activeService!!.hideImages
                promoListView.initialize(
                    getString(R.string.search_promo_items),
                    promoItems,
                    collectionData,
                    true,
                    hideImages
                )
                promoListView.adapter.notifyDataSetChanged()
            }
        }
        updateView()
    }

    private fun updateView() {
        searchAdapter.notifyDataSetChanged()
        promoListView.visibility = if (searchItems.isNotEmpty() || promoItems.isEmpty()) View.GONE else View.VISIBLE
        searchList!!.visibility = if (searchItems.isEmpty()) View.GONE else View.VISIBLE
        searchImageRelativeLayout!!.visibility =
            if (promoItems.isEmpty() && searchItems.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onSearchClick()
    }

    fun setSearchTitle(searchTitle: String?) {
        this.searchTitle = searchTitle
        searchProducts?.hint = searchTitle
    }

    fun setActiveService(activeService: ServiceObject?) {
        this.activeService = activeService
    }

    private fun onSearchClick() {
        searchItems.clear()
        searchProducts?.post {
            searchProducts?.requestFocus()
            if (activity != null) {
                val inputManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.showSoftInput(searchProducts, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        updatePromoItems()
    }

    private fun onClearSearch() {
        searchProducts?.setText("")
        Utilities.hideKeyboard(activity)
        clearSearchCallback!!.click(null)

    }

}
