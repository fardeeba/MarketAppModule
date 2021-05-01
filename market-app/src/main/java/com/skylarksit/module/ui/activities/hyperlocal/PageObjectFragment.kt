package com.skylarksit.module.ui.activities.hyperlocal

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skylarksit.module.R
import com.skylarksit.module.analytics.Segment.productListViewed
import com.skylarksit.module.pojos.CategoryViewType
import com.skylarksit.module.pojos.CollectionData
import com.skylarksit.module.pojos.services.MenuCategoryObject
import com.skylarksit.module.pojos.services.MenuItemObject
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.model.ServicesModel
import com.skylarksit.module.ui.model.ViewRouter
import com.skylarksit.module.ui.utils.IClickListener
import com.skylarksit.module.ui.utils.ItemClickListener
import com.skylarksit.module.ui.utils.ItemsGridViewAdapter
import com.skylarksit.module.utils.Utilities
import java.util.*

class PageObjectFragment : Fragment() {
    lateinit var adapter: ItemsGridViewAdapter<*>
    private var finalGridActivity: FinalGridActivity? = null
    lateinit var model: ServicesModel
    private var activeService: ServiceObject? = null
    private var currentItemPosition: Int? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FinalGridActivity) {
            finalGridActivity = context
        }
    }

    @JvmField
    var overallScrollY = 0
    lateinit var recyclerView: RecyclerView
    private var activity: FinalGridActivity? = null
    private var scrollListener: RecyclerView.OnScrollListener? = null

    private lateinit var categoryObject: MenuCategoryObject
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        model = ServicesModel.instance(context)
        val rootView = inflater.inflate(R.layout.view_grid, container, false)
        activity = getActivity() as FinalGridActivity?
        if (arguments == null) return null
        val slug = arguments!!.getString("activeService")
        activeService = model.findProviderByName(slug)
        if (activeService == null) return null
        rootView.tag = arguments!!.getInt(ARG_OBJECT)
        recyclerView = rootView.findViewById(R.id.recyclerGridView)
        if (resources.getBoolean(R.bool.showItemBox)) {
            rootView.background = activity!!.getDrawable(R.color.background)
        } else {
            rootView.background = activity!!.getDrawable(R.color.white)
        }
        subcategoryButtonClicked = false


        activity!!.subCatListener = object : IClickListener<MenuCategoryObject> {
            override fun click(`object`: MenuCategoryObject?) {
                subcategoryButtonClicked = true
                `object`?.let { updateSubcategoryButton(it.label, 0) }
            }
        }

        val listItems = ArrayList<MenuItemObject>()
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                overallScrollY += dy
                if (overallScrollY < activity!!.stickyHeaderHeight) {
                    activity!!.headerView.y = activity!!.initHeaderY - overallScrollY
                } else {
                    activity!!.headerView.y = activity!!.initHeaderY - activity!!.stickyHeaderHeight
                }
                if (subcategoryButtonClicked) return
                var firstItem = 0
                val layoutManager = recyclerView.layoutManager
                if (layoutManager != null) {
                    if (layoutManager is GridLayoutManager) {
                        firstItem = layoutManager.findFirstVisibleItemPosition()
                    } else if (layoutManager is SnappingLinearLayoutManager) {
                        firstItem = layoutManager.findFirstVisibleItemPosition()
                    }
                }
                firstItem++
                if (firstItem >= adapter.itemCount) firstItem = adapter.itemCount - 1
                val item = adapter.getItem(firstItem) as MenuItemObject
                if (item.subcategory != null) {
                    updateSubcategoryButton(item.subcategory, dy)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> subcategoryButtonClicked = false
                    RecyclerView.SCROLL_STATE_DRAGGING, RecyclerView.SCROLL_STATE_SETTLING -> {
                    }
                }
            }
        }
        categoryObject = finalGridActivity!!.pagerList[arguments!!.getInt(ARG_OBJECT)]
        if (categoryObject.subcategories != null && categoryObject.subcategories.isNotEmpty()) {
            for (subCat in categoryObject.subcategories) {
                val subCatHeader = MenuItemObject()
                subCatHeader.itemViewType = "subHeader"
                subCatHeader.label = subCat.label
                if (categoryObject.showSubcategories) {
                    listItems.add(subCatHeader)
                }
                subCat.items.sort()
                listItems.addAll(subCat.items)
            }
        } else {
            categoryObject.items.sort()
            listItems.addAll(categoryObject.items)
        }
        if (listItems.size == 0) return rootView
        productListViewed(listItems, categoryObject.getCollectionData(CollectionData.Type.STORE_SECTION))
        adapter = ItemsGridViewAdapter(context, listItems, activeService!!.viewType, false, false)
        adapter.setHideImages(activeService!!.hideImages)
        adapter.activity = activity
        adapter.setItemClickListener(object : ItemClickListener<MenuItemObject?>(adapter) {
            override fun onClick(item: MenuItemObject?, position: Int) {
                if (item == null || "header" == item.itemViewType || "subHeader" == item.itemViewType) return
                if (item.displayPopup()) {
                    currentItemPosition = position
                    ViewRouter.instance().displayProduct(
                        getActivity(),
                        item,
                        categoryObject.getCollectionData(CollectionData.Type.STORE_SECTION),
                        true,
                        true,
                        false,
                        true
                    )
                    return
                } else if (item.isOutOfStock) {
                    return
                }
                if (model.addToCart(
                        context,
                        item,
                        item.itemsOrdered + 1,
                        categoryObject.getCollectionData(CollectionData.Type.STORE_SECTION),
                        object : IClickListener<Any> {
                            override fun click(`object`: Any?) {
                                finalGridActivity!!.updateCart()
                                if (activeService!!.isCourier) {
                                    ViewRouter.instance().goToBasket(context, activeService, null)
                                    return
                                }
                                if (item.isSingleSelection) {
                                    ViewRouter.instance().goToBasket(context, activeService, null)
                                }
                            }
                        })
                ) {
                    finalGridActivity!!.updateCart()
                    Handler(Looper.getMainLooper()).post { adapter.notifyDataSetChanged() }
                    if (item.isSingleSelection) {
                        ViewRouter.instance().goToBasket(context, activeService, null)
                    }
                }
            }
        })
        val clicker: ItemClickListener<MenuItemObject> = object : ItemClickListener<MenuItemObject>(adapter) {
            override fun onClick(item: MenuItemObject, position: Int) {
                if (item.hasCustomizations()) {
                    currentItemPosition = position
                    ViewRouter.instance().displayProduct(
                        getActivity(),
                        item,
                        categoryObject.getCollectionData(CollectionData.Type.STORE_SECTION),
                        true,
                        true,
                        false,
                        true
                    )
                    return
                }
                if (model.addToCart(
                        context,
                        item,
                        item.itemsOrdered + 1,
                        categoryObject.getCollectionData(CollectionData.Type.STORE_SECTION),
                        object : IClickListener<Any> {
                            override fun click(`object`: Any?) {
                                activity!!.updateCart()
                                adapter.notifyDataSetChanged()
                            }

                        }
                    )
                ) {
                    adapter.notifyItemChanged(position)
                    activity!!.updateCart()
                }
            }
        }
        clicker.setClickTimeInterval(0)
        val remove: ItemClickListener<MenuItemObject> = object : ItemClickListener<MenuItemObject>(adapter) {
            override fun onClick(item: MenuItemObject, position: Int) {
                if (model.addToCart(
                        context,
                        item,
                        item.itemsOrdered - 1,
                        categoryObject.getCollectionData(CollectionData.Type.STORE_SECTION),
                        object : IClickListener<Any> {
                            override fun click(`object`: Any?) {
                                activity!!.updateCart()
                                adapter.notifyDataSetChanged()
                            }

                        }
                    )
                ) {
                    adapter.notifyItemChanged(position)
                    activity!!.updateCart()
                }
            }
        }
        remove.setClickTimeInterval(0)
        adapter.setQuickAddListener(clicker)
        adapter.setQuickRemoveListener(remove)
        recyclerView.adapter = adapter
        var bottom = Utilities.dpToPx(80)
        if (listItems.size < 6) {
            bottom = activity!!.headerView.height
        }
        val paddingH: Int
        var paddingTop =
            if (activeService!!.hasCategories()) activity!!.headerView.height else activity!!.headerView.height - activity!!.tabs.height
        if (model.singleStore && activeService!!.categoryViewType === CategoryViewType.SWIPE) {
            paddingTop += Utilities.dpToPx(10)
        }
        val layoutManager: RecyclerView.LayoutManager
        if ("list".equals(activeService!!.viewType, ignoreCase = true)) {
            layoutManager = SnappingLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false)
            paddingH = Utilities.dpToPx(16)
        } else {
            val spanCount = if ("grid_3".equals(activeService!!.viewType, ignoreCase = true)) 3 else 2
            layoutManager = SnappingGridLayoutManager(getActivity(), spanCount)
            layoutManager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.getItemViewType(position) == ItemsGridViewAdapter.TYPE_ITEM) {
                        1
                    } else spanCount
                }
            }
            paddingH = Utilities.dpToPx(if (resources.getBoolean(R.bool.showItemBox)) 10 else 0)
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.setPadding(paddingH, paddingTop, paddingH, bottom)
        return rootView
    }

    fun updateSubcategoryButton(subCategory: String, position: Int) {
        for (iter in activity!!.subcategoryList) {
            iter.isSelected = false
        }
        for (iter in activity!!.subcategoryList) {
            if (iter.label == subCategory) {
                iter.isSelected = true
                activity!!.activeMenuCategory!!.selectedSubcategory = subCategory
                activity!!.subcategoriesAdapter!!.notifyDataSetChanged()
                var index = activity!!.subcategoryList.indexOf(iter)
                if (position > 0) index++ else if (position < 0) index--
                if (index < 0) index = 0
                if (index >= activity!!.subcategoryList.size) index = activity!!.subcategoryList.size - 1
                activity!!.subcategoriesList.scrollToPosition(index)
                break
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentItemPosition != null) {
            adapter.notifyItemChanged(currentItemPosition!!)
            currentItemPosition = null
        } else {
            adapter.notifyDataSetChanged()
        }
    }

    private var hasListener = false
    fun removeScrollListeners() {
        hasListener = false
        recyclerView.removeOnScrollListener(scrollListener!!)
    }

    fun addScrollListeners() {
        if (!hasListener) {
            hasListener = true
            recyclerView.addOnScrollListener(scrollListener!!)
        }
    }

    companion object {
        const val ARG_OBJECT = "object"
        private var subcategoryButtonClicked = false
    }
}
