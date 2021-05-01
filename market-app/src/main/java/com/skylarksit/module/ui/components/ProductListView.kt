package com.skylarksit.module.ui.components

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skylarksit.module.R
import com.skylarksit.module.pojos.CollectionData
import com.skylarksit.module.pojos.services.MenuItemObject
import com.skylarksit.module.ui.activities.hyperlocal.SnappingGridLayoutManager
import com.skylarksit.module.ui.activities.hyperlocal.SnappingLinearLayoutManager
import com.skylarksit.module.ui.model.IListItem
import com.skylarksit.module.ui.model.ServicesModel
import com.skylarksit.module.ui.model.ViewRouter
import com.skylarksit.module.ui.utils.IClickListener
import com.skylarksit.module.ui.utils.ItemClickListener
import com.skylarksit.module.ui.utils.ItemsGridViewAdapter
import com.skylarksit.module.ui.utils.OnOneOffClickListener

class ProductListView : RelativeLayout {
    var actionClick: IClickListener<*>? = null
    fun setHideImages() {}
    enum class ListType {
        HORIZONTAL, VERTICAL, GRID, BASKET
    }

    var data: List<IListItem>? = null
    var recyclerView: RecyclerView? = null
    private var titleView: TextView? = null
    private var subTitleView: TextView? = null
    private var listType = ListType.HORIZONTAL
    private var mInflater: LayoutInflater = LayoutInflater.from(context)
    lateinit var adapter: ItemsGridViewAdapter<*>
    private var fromProvider = false
    private var updateCartListener: IClickListener<MenuItemObject>? = null
    private var collectionData: CollectionData? = null
    fun setUpdateListener(clickListener: IClickListener<MenuItemObject>?) {
        updateCartListener = clickListener
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        val productList = mInflater.inflate(R.layout.product_list_layout, this, true)
        recyclerView = productList.findViewById(R.id.recyclerView)
        titleView = productList.findViewById(R.id.title)
        subTitleView = productList.findViewById(R.id.subTitle)
    }

    private fun init() {
        val layoutManager: RecyclerView.LayoutManager?
        subTitleView!!.visibility = GONE
        if (listType == ListType.HORIZONTAL) {
            adapter = ItemsGridViewAdapter(context, data, listType == ListType.HORIZONTAL)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        } else {
            if (listType == ListType.VERTICAL || listType == ListType.BASKET) {
                layoutManager = SnappingLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            } else {
                val spanCount = 3
                layoutManager = SnappingGridLayoutManager(context, spanCount)
                layoutManager.spanSizeLookup = object : SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (adapter.getItemViewType(position)) {
                            ItemsGridViewAdapter.TYPE_HEADER, ItemsGridViewAdapter.TYPE_SUB_HEADER, ItemsGridViewAdapter.TYPE_ITEM_FULL -> spanCount
                            ItemsGridViewAdapter.TYPE_ITEM -> 1
                            else -> spanCount
                        }
                    }
                }
            }
            var list: String? = null
            if (listType == ListType.GRID || listType == ListType.HORIZONTAL) {
                list = "grid"
            } else if (listType == ListType.VERTICAL) {
                list = "list"
            } else if (listType == ListType.BASKET) {
                list = "basket"
            }
            adapter = ItemsGridViewAdapter(context, data, list, false, false)
        }
        recyclerView!!.layoutManager = layoutManager
        val listener: ItemClickListener<MenuItemObject> = object : ItemClickListener<MenuItemObject>(adapter, 0) {
            override fun onClick(item: MenuItemObject, position: Int) {
                ServicesModel.instance().addToCart(context, item, item.itemsOrdered + 1, collectionData, null)
                adapter.notifyItemChanged(position)
                if (updateCartListener != null) {
                    updateCartListener!!.click(item)
                }
            }
        }
        adapter.setQuickAddListener(listener)
        val remove: ItemClickListener<MenuItemObject> = object : ItemClickListener<MenuItemObject>(adapter, 0) {
            override fun onClick(item: MenuItemObject, position: Int) {
                ServicesModel.instance().addToCart(context, item, item.itemsOrdered - 1, collectionData, null)
                adapter.notifyDataSetChanged()
                if (updateCartListener != null) {
                    updateCartListener!!.click(item)
                }
            }
        }
        adapter.setQuickRemoveListener(remove)
        adapter.setItemClickListener(object : ItemClickListener<MenuItemObject>(adapter) {
            override fun onClick(item: MenuItemObject, position: Int) {
                ViewRouter.instance()
                    .displayProduct(context as Activity, item, collectionData, true, true, false, fromProvider)
            }
        })
        recyclerView!!.adapter = adapter
    }

    fun setTitle(text: String?) {
        titleView!!.text = text
    }

    fun setSubtitle(text: String?) {
        subTitleView!!.text = text
        subTitleView!!.visibility = VISIBLE
        if (actionClick != null) {
            subTitleView!!.setOnClickListener(object : OnOneOffClickListener() {
                override fun onSingleClick(v: View) {
                    actionClick!!.click(null)
                }
            })
        }
    }

    fun initialize(
        text: String?,
        data: List<IListItem>,
        collectionData: CollectionData?,
        fromProvider: Boolean,
        listType: ListType,
        hideImages: Boolean
    ) {
        this.listType = listType
        initialize(text, data, collectionData, fromProvider, hideImages)
    }

    fun initialize(
        text: String?,
        data: List<IListItem>,
        collectionData: CollectionData?,
        fromProvider: Boolean,
        hideImages: Boolean
    ) {
        init()
        setTitle(text)
        this.fromProvider = fromProvider
        this.collectionData = collectionData
        var list: String? = null
        if (listType == ListType.GRID || listType == ListType.HORIZONTAL) {
            list = "grid"
        } else if (listType == ListType.VERTICAL) {
            list = "list"
        } else if (listType == ListType.BASKET) {
            list = "basket"
        }
        @Suppress("UNCHECKED_CAST")
        adapter.init(data as List<Nothing>?, listType == ListType.HORIZONTAL, list)
        adapter.setHideImages(hideImages)
    }

    fun refresh() {
        adapter.notifyDataSetChanged()
    }
}
