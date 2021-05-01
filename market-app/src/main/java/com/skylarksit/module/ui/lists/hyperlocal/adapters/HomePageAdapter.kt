@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.lists.hyperlocal.adapters

import android.app.Activity
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.skylarksit.module.MainActivity
import com.skylarksit.module.R
import com.skylarksit.module.pojos.AddressObject
import com.skylarksit.module.pojos.CollectionData
import com.skylarksit.module.pojos.DefaultLocation
import com.skylarksit.module.pojos.services.*
import com.skylarksit.module.ui.activities.hyperlocal.SnappingGridLayoutManager
import com.skylarksit.module.ui.components.ProductListView
import com.skylarksit.module.ui.lists.items.SearchResultItem
import com.skylarksit.module.ui.lists.main.adapters.SearchResultAdapter
import com.skylarksit.module.ui.model.IListItem
import com.skylarksit.module.ui.model.IListItemGroup
import com.skylarksit.module.ui.model.SectionHeaderItem
import com.skylarksit.module.ui.model.ViewRouter
import com.skylarksit.module.ui.utils.*
import com.skylarksit.module.utils.U
import com.skylarksit.module.utils.Utilities
import java.util.*

class HomePageAdapter(activity: Activity?, data: List<IListItem?>?, withHeader: Boolean, withFooter: Boolean) :
    HFRecyclerView<IListItem?>(activity, data, withHeader, withFooter) {
    private var updateCartListener: IClickListener<MenuItemObject>? = null
    private var searchClickListener: IClickListener<*>? = null
    fun setUpdateCartListener(cartListener: IClickListener<MenuItemObject>?) {
        updateCartListener = cartListener
    }

    override fun getItemView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_BANNERS) {
            return BannerViewHolder(inflater.inflate(R.layout.services_list_banner, parent, false))
        } else if (viewType == TYPE_ORDER) {
            return OrderTrackingHolder(inflater.inflate(R.layout.home_page_tracking, parent, false))
        } else if (viewType == TYPE_BLOCKS || viewType == TYPE_COLLECTIONS || viewType == TYPE_GRID_2 || viewType == TYPE_GRID_2_WHITE || viewType == TYPE_GRID_3 || viewType == TYPE_GRID_3_WHITE || viewType == TYPE_GRID_4 || viewType == TYPE_GRID_4_WHITE) {
            return HomePageBlockListViewHolder(inflater.inflate(R.layout.home_page_block, parent, false), viewType)
        } else if (viewType == TYPE_NO_SERVICES) {
            return NoServiceViewHolder(inflater.inflate(R.layout.home_page_no_services, parent, false))
        } else if (viewType == TYPE_PRODUCTS) {
            return ProductListViewHolder(inflater.inflate(R.layout.home_page_products, parent, false))
        } else if (viewType == TYPE_STORES_VERTICAL) {
            return ProvidersHolder(inflater.inflate(R.layout.home_page_providers, parent, false))
        } else if (viewType == TYPE_STORES_HORIZONTAL) {
            return ListItemViewHolder(inflater.inflate(R.layout.home_page_stores, parent, false), viewType)
        } else if (viewType == TYPE_ITEM_FULL) {
            return ItemFullViewHolder(inflater.inflate(R.layout.home_page_full_item, parent, false))
        }
        return ListItemViewHolder(inflater.inflate(R.layout.home_page_stores, parent, false), viewType)
    }

    override fun getHeaderView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ServicesHeader(inflater.inflate(R.layout.home_page_top_view_search, parent, false))
    }

    override fun getFooterView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return null
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is ListItemViewHolder) {
            val item = getItem(position) as IListItemGroup?
            if (item!!.label != null && item.label.isNotEmpty()) {
                viewHolder.headerText.setPadding(0, Utilities.dpToPx(20), 0, 0)
                viewHolder.headerText.alpha = 1f
                viewHolder.headerText.text = item.label
            } else {
                viewHolder.headerText.setPadding(0, 0, 0, 0)
                viewHolder.headerText.alpha = 0f
            }
            viewHolder.adapter.data = item.items
        } else if (viewHolder is OrderTrackingHolder) {
            val iListItem = getItem(position)
            if (iListItem is OrderTrackingBean) {
                viewHolder.eta.text = iListItem.eta
                if (java.lang.Boolean.TRUE == iListItem.feedbackOrder) {
                    viewHolder.image.visibility = View.GONE
                    viewHolder.imageSingle.visibility = View.VISIBLE
                    viewHolder.imageSingle.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.s3))
                    viewHolder.imageSingle.setColorFilter(0)
                } else {
                    if (model.singleStore) {
                        viewHolder.image.visibility = View.GONE
                        viewHolder.imageSingle.visibility = View.VISIBLE
                        viewHolder.imageSingle.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.motorcycle
                            )
                        )
                        viewHolder.imageSingle.setColorFilter(
                            ContextCompat.getColor(context, R.color.secondaryColor),
                            PorterDuff.Mode.SRC_ATOP
                        )
                    } else {
                        viewHolder.image.setImageURI(U.parse(iListItem.imageUrl))
                        viewHolder.image.visibility = View.VISIBLE
                        viewHolder.imageSingle.visibility = View.GONE
                    }
                }
                if (iListItem.etaTime != null && iListItem.etaTime > 0) {
                    viewHolder.etaMinus.text = iListItem.etaTime.toString()
                    viewHolder.etaTimeLayout.visibility = View.VISIBLE
                } else {
                    viewHolder.etaTimeLayout.visibility = View.GONE
                }
                viewHolder.status.text = iListItem.getDescription()
                if (java.lang.Boolean.TRUE == iListItem.feedbackOrder) {
                    viewHolder.etaTimeLayout.visibility = View.GONE
                } else {
                    viewHolder.etaLayout.visibility = View.VISIBLE
                }
            }
        } else if (viewHolder is BannerViewHolder) {
            val cat = getItem(position) as IListItemGroup?
            val item = cat!!.items[0] as HomePageItemBean
            viewHolder.image.setImageURI(U.parse(item.getImageUrl()))
        } else if (viewHolder is NoServiceViewHolder) {
            getItem(position) as IListItemGroup?
        } else if (viewHolder is ServicesHeader) {
            if (viewHolder.searchText != null) {
                if (model.services.size == 0) {
                    viewHolder.searchText!!.visibility = View.GONE
                } else {
                    viewHolder.searchText!!.visibility = View.VISIBLE
                }
            }
            //            if (!model.homePageFixedHeader){
//                if (!model.services.isEmpty()){
////                    String etaText = model.getEtaText();
////                    if ( etaText != null){
////                        holder.etaText.setVisibility(View.VISIBLE);
////                        holder.etaText.setText(etaText);
////                    }
//
//                    if(model.changeLocationText!=null)
//                        holder.changeLocationText.setText(model.changeLocationText);
//                }
//
//                if (model.changeLocationTooltip>-1) {
//                    TooltipsManager.instance().showTooltip(activity, holder.changeLocationText, R.string.tip_location_far, Tooltip.BOTTOM, 3);
//                    model.changeLocationTooltip = -1;
//                }
//            }
        } else if (viewHolder is ProductListViewHolder) {
            val item = getItem(position) as IListItemGroup?
            viewHolder.productListView.initialize(item!!.label, item.items, item.collectionData,
                fromProvider = false,
                hideImages = false
            )
        } else if (viewHolder is ItemFullViewHolder) {
            val item = getItem(position) as IListItemGroup?
            val single = item!!.items[0]
            viewHolder.image.setImageURI(single.imageUrl)
            viewHolder.label.text = single.label
        } else if (viewHolder is HomePageBlockListViewHolder) {
            val cat = getItem(position) as HomePageCategoryBean?

//            if (Utilities.isMarketplace() && model.services.size()>1){
//                if (cat.showActionText){
//                    holder.actionText.setVisibility(View.VISIBLE);
//                }
//                else{
//                    holder.actionText.setVisibility(View.GONE);
//                }
//            }
//            else{
//                holder.actionText.setVisibility(View.GONE);
//            }
            if (viewHolder.headerText != null) {
                if (cat!!.getLabel() != null && cat.getLabel().isNotEmpty() && cat.showLabel) {
                    viewHolder.headerText!!.setPadding(0, Utilities.dpToPx(20), 0, 0)
                    viewHolder.headerText!!.alpha = 1f
                    viewHolder.headerText!!.text = cat.getLabel()
                } else {
                    viewHolder.headerText!!.setPadding(0, Utilities.dpToPx(0), 0, 0)
                    viewHolder.headerText!!.alpha = 0f
                }
            }
            viewHolder.adapter.data = cat!!.getItems()
        } else if (viewHolder is ProvidersHolder) {
            val item = getItem(position) as IListItemGroup?
            viewHolder.headerText.text = item!!.label
            val homePageItemBeans = item.items
            val providers: MutableList<IListItem> = ArrayList()
            for (itemBean in homePageItemBeans) {
                val homePageItemBean = itemBean as HomePageItemBean
                providers.add(model.findProviderById(homePageItemBean.itemId))
            }
            viewHolder.adapter.data = providers.toList()
        } else if (viewHolder is HeaderDeliveryTime) {
            if (viewHolder.deliveryText != null) {
                if (model.services.size == 0) {
                    viewHolder.itemView.visibility = View.GONE
                } else {
                    viewHolder.itemView.visibility = View.VISIBLE
                    if (model.services[0].isClosed()) {
                        viewHolder.deliveryText!!.text = model.services[0].etaText
                        viewHolder.icon.setImageResource(R.drawable.close)
                    } else if (!Utilities.isEmpty(model.homePageTitleMessage)) {
                        viewHolder.deliveryText!!.text = model.homePageTitleMessage
                        viewHolder.searchButton.visibility = View.VISIBLE
                        viewHolder.icon.setImageResource(R.drawable.motorcycle)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (mWithHeader && isPositionHeader(position)) return TYPE_HEADER
        return if (mWithFooter && isPositionFooter(position)) TYPE_FOOTER else getItem(position)!!.viewType
    }

    private inner class ProductListViewHolder(itemView: View) : ItemsHolder(itemView) {
        var productListView: ProductListView = itemView.findViewById(R.id.productListView)

        init {
            productListView.setUpdateListener(object : IClickListener<MenuItemObject> {
                override fun click(`object`: MenuItemObject?) {
                    if (updateCartListener != null) updateCartListener!!.click(
                        `object`
                    )
                }
            })
        }
    }

    private inner class ListItemViewHolder(itemView: View, viewType: Int) : ItemsHolder(itemView) {
        var headerText: TextView = itemView.findViewById(R.id.headerText)
        var list: RecyclerView = itemView.findViewById(R.id.list)
        var adapter: ServicesListViewAdapter

        init {
            list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ServicesListViewAdapter(context, ArrayList(),
                withHeader = false,
                withFooter = false,
                viewType = viewType
            )
            adapter.setClickListener(object : ItemClickListener<HomePageItemBean?>(adapter) {
                override fun onClick(item: HomePageItemBean?, position: Int) {
                    when (itemViewType) {
                        TYPE_STORES_HORIZONTAL, TYPE_STORES_VERTICAL -> ViewRouter.instance()
                            .goToServiceProvider(
                                context as Activity,
                                item!!.serviceSlug,
                                null,
                                null,
                                item.collectionData
                            )
                        3 -> ViewRouter.instance().displayProduct(context as Activity, item)
                        else -> {
                        }
                    }
                }
            })
            list.adapter = adapter
        }
    }

    private inner class HomePageBlockListViewHolder(itemView: View, viewType: Int) :
        ItemsHolder(itemView) {
        var headerText: TextView? = itemView.findViewById(R.id.headerText)
        var list: RecyclerView = itemView.findViewById(R.id.list)
        var adapter: HomePageBlockAdapter

        init {
            var columnSpan = 2
            when (viewType) {
                TYPE_GRID_2, TYPE_GRID_2_WHITE -> columnSpan = 2
                TYPE_GRID_3, TYPE_GRID_3_WHITE -> columnSpan = 3
                TYPE_GRID_4, TYPE_GRID_4_WHITE -> columnSpan = 4
            }
            val layoutManager: GridLayoutManager = SnappingGridLayoutManager(context, columnSpan)
            val finalColumnSpan = columnSpan
            list.layoutManager = layoutManager
            adapter = HomePageBlockAdapter(context, ArrayList(), false, false, viewType)
            layoutManager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (adapter.getItemViewType(position)) {
                        TYPE_ITEM_FULL, TYPE_ORDER -> finalColumnSpan
                        TYPE_ITEM -> 1
                        else -> {
                            finalColumnSpan
                        }
                    }
                }
            }
            list.adapter = adapter
            adapter.setClickListener(object : ItemClickListener<HomePageItemBean?>(adapter) {
                override fun onClick(item: HomePageItemBean?, position: Int) {
                    if (item!!.storeGroupList != null && item.storeGroupList.isNotEmpty()) {
                        ViewRouter.instance()
                            .goToServiceGroup(context as Activity, item.label, item.storeGroupList, item.collectionData)
                    } else {
                        ViewRouter.instance().goToServiceProvider(
                            context as Activity,
                            item.serviceSlug,
                            item.serviceCategory,
                            null,
                            item.collectionData
                        )
                    }
                }
            })
        }
    }

    private inner class ServicesHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var searchText: EditText? = itemView.findViewById(R.id.searchTextBox)
        var filtersButton: Button? = itemView.findViewById(R.id.filtersButton)
        var openButton: Button? = itemView.findViewById(R.id.openButton)

        init {
            searchText!!.hint = if (Utilities.isMarketplace()) context.getString(R.string.search_shops_and_products) else context.getString(
                R.string.search_brands_and_products
            )
            searchText!!.onFocusChangeListener = OnFocusChangeListener { _: View?, focus: Boolean ->
                if (focus) {
                    searchClickListener!!.click(null)
                }
            }
            if (filtersButton != null) {
                filtersButton!!.setOnClickListener(object : OnOneOffClickListener() {
                    override fun onSingleClick(v: View) {}
                })
            }
            if (openButton != null) {
                openButton!!.setOnClickListener(object : OnOneOffClickListener() {
                    override fun onSingleClick(v: View) {
                        model.homePageOpenNowFilter = !model.homePageOpenNowFilter
                        if (!model.homePageOpenNowFilter) {
                            openButton!!.background =
                                ContextCompat.getDrawable(context, R.drawable.rounded_corners_white_10)
                            openButton!!.setTextColor(ContextCompat.getColor(context, R.color.color_light_grey))
                        } else {
                            openButton!!.background =
                                ContextCompat.getDrawable(context, R.drawable.rounded_corners_dark)
                            openButton!!.setTextColor(ContextCompat.getColor(context, R.color.white))
                        }
                        model.refreshHomePageItems()
                        notifyDataSetChanged()
                    }
                })
            }
        }
    }

    private inner class HeaderDeliveryTime(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var deliveryText: TextView? = itemView.findViewById(R.id.deliveryText)
        var searchButton: View = itemView.findViewById(R.id.searchButton)
        var icon: ImageView = itemView.findViewById(R.id.icon)

        init {
            searchButton.setOnClickListener { searchClickListener!!.click(null) }
        }
    }

    private inner class ItemFullViewHolder(inflate: View) : RecyclerView.ViewHolder(inflate) {
        var image: SimpleDraweeView = inflate.findViewById(R.id.image)
        var label: TextView = inflate.findViewById(R.id.label)

        init {
            image.setOnClickListener(object : OnOneOffClickListener() {
                override fun onSingleClick(v: View) {
                    val item = (getItem(adapterPosition) as HomePageCategoryBean?)!!.items[0]
                    if (item.storeGroupList != null && item.storeGroupList.isNotEmpty()) {
                        ViewRouter.instance()
                            .goToServiceGroup(context as Activity, item.label, item.storeGroupList, item.collectionData)
                    } else {
                        ViewRouter.instance().goToServiceProvider(
                            context as Activity,
                            item.serviceSlug,
                            item.serviceCategory,
                            null,
                            item.collectionData
                        )
                    }
                }
            })
        }
    }

    private inner class BannerViewHolder(inflate: View) : RecyclerView.ViewHolder(inflate) {
        var image: SimpleDraweeView = inflate.findViewById(R.id.image)

        init {
            image.setOnClickListener(object : OnOneOffClickListener() {
                override fun onSingleClick(v: View) {
                    ViewRouter.instance().displayBanner(
                        context as Activity,
                        (getItem(adapterPosition) as HomePageCategoryBean?)!!.items[0]
                    )
                }
            })
        }
    }

    private inner class NoServiceViewHolder(inflate: View) : ItemsHolder(inflate) {
        //        TextView noServicesTextView;
        var defaultLocationsList: RecyclerView = inflate.findViewById(R.id.defaultLocationsList)
        var changeLocationButton: Button = inflate.findViewById(R.id.changeLocationButton)
        private val searchResultAdapter: SearchResultAdapter
        private val searchResultItems: MutableList<IListItem>
        private fun populateDefaultLocations() {
            searchResultItems.clear()
            for (defaultLocation in model.defaultLocations) {
                val item = SearchResultItem()
                item.label = defaultLocation.label
                item.description = defaultLocation.country
                item.icon = R.drawable.otheron
                item.colorizeIcon = true
                item.data = defaultLocation
                searchResultItems.add(item)
            }
            if (searchResultItems.size > 0) {
                val section = SectionHeaderItem(context.getString(R.string.header_available_areas), null)
                searchResultItems.add(0, section)
            }
            searchResultAdapter.notifyDataSetChanged()
        }

        init {
            //            noServicesTextView = inflate.findViewById(R.id.noServicesText);
            searchResultItems = ArrayList()
            searchResultAdapter = SearchResultAdapter(context, searchResultItems, false, false)
            searchResultAdapter.setClickListener(object : ItemClickListener<SearchResultItem?>(searchResultAdapter) {
                override fun onClick(item: SearchResultItem?, position: Int) {
                    val location = item!!.data as DefaultLocation
                    val addressObject = AddressObject()
                    addressObject.lat = location.lat
                    addressObject.lon = location.lon
                    addressObject.locality = location.label
                    //addressObject.code = location.getLabel();
                    (context as MainActivity).updateCurrentAddress(addressObject, true)
                }
            })
            defaultLocationsList.layoutManager = LinearLayoutManager(context)
            defaultLocationsList.adapter = searchResultAdapter
            changeLocationButton.setOnClickListener {
                ViewRouter.instance().changeYourLocation(context as Activity, false)
            }
            populateDefaultLocations()
        }
    }

    private inner class OrderTrackingHolder(inflate: View) : ItemsHolder(inflate) {
        var etaMinus: TextView = inflate.findViewById(R.id.etaMins)
        var status: TextView = inflate.findViewById(R.id.status)
        var eta: TextView = inflate.findViewById(R.id.eta)
        var image: SimpleDraweeView = inflate.findViewById(R.id.image)
        var imageSingle: ImageView = inflate.findViewById(R.id.imageSingle)
        var etaLayout: View = inflate.findViewById(R.id.etaLayout)
        var etaTimeLayout: View = inflate.findViewById(R.id.etaTimeLayout)

        init {
            inflate.setOnClickListener(object : OnOneOffClickListener() {
                override fun onSingleClick(v: View) {
                    ViewRouter.instance()
                        .openOrderDetails(context, (getItem(adapterPosition) as OrderTrackingBean?)!!.orderUid)
                }
            })
        }
    }

    private inner class ProvidersHolder(itemView: View) : ItemsHolder(itemView) {
        var list: RecyclerView = itemView.findViewById(R.id.providersListView)
        var adapter: ProviderEntryAdapter = ProviderEntryAdapter(context, ArrayList())
        var headerText: TextView = itemView.findViewById(R.id.headerText)

        init {
            adapter.setClickListener(object : ItemClickListener<ServiceObject?>(adapter) {
                override fun onClick(item: ServiceObject?, position: Int) {
                    ViewRouter.instance().goToServiceProvider(
                        context as Activity,
                        item!!.slug,
                        null,
                        null,
                        CollectionData("home_page_stores", "Home page stores", CollectionData.Type.HOME_PAGE)
                    )
                }
            })
            list.layoutManager = LinearLayoutManager(context)
            list.adapter = adapter
        }
    }
}
