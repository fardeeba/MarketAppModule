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
import androidx.recyclerview.widget.RecyclerView
import com.skylarksit.module.R
import com.skylarksit.module.pojos.CollectionData
import com.skylarksit.module.pojos.services.MenuItemObject
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.activities.main.FavoritesActivity
import com.skylarksit.module.ui.model.IListItem
import com.skylarksit.module.ui.model.ServicesModel
import com.skylarksit.module.ui.model.ViewRouter
import com.skylarksit.module.ui.utils.IClickListener
import com.skylarksit.module.ui.utils.ItemClickListener
import com.skylarksit.module.ui.utils.ItemsGridViewAdapter
import com.skylarksit.module.utils.Utilities
import java.util.*

class FavoritesPageObjectFragment : Fragment() {

    lateinit var adapter: ItemsGridViewAdapter<*>
    private var mainActivity: FavoritesActivity? = null
    lateinit var model: ServicesModel
    private var activeService: ServiceObject? = null
    private val listItems = ArrayList<IListItem>()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FavoritesActivity) {
            mainActivity = context
        }
    }

    val collectionData: CollectionData
        get() = CollectionData("my_favorites", "Favorites", CollectionData.Type.FAVORITES)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        model = ServicesModel.instance(context)
        val rootView = inflater.inflate(R.layout.view_grid, container, false)
        if (arguments == null) return null
        rootView.tag = arguments!!.getInt(ARG_OBJECT)
        val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerGridView)
        recyclerView.setPadding(Utilities.dpToPx(12), Utilities.dpToPx(0), Utilities.dpToPx(12), Utilities.dpToPx(72))
        val favoriteServiceBean = model.favorites[arguments!!.getInt(ARG_OBJECT)]
        activeService = model.findProviderByName(favoriteServiceBean.slug)
        if (activeService == null) return null
        adapter = ItemsGridViewAdapter(context, listItems, activeService!!.viewType, false, false)
        adapter.setItemClickListener(object : ItemClickListener<MenuItemObject?>(adapter) {
            override fun onClick(item: MenuItemObject?, position: Int) {


                if (item == null || "header" == item.itemViewType || "subHeader" == item.itemViewType) return
                if (item.displayPopup()) {
                    ViewRouter.instance()
                        .displayProduct(activity, item, collectionData, true, true, false, true)
                    return
                } else if (item.isOutOfStock) {
                    return
                }
                if (model.addToCart(
                        context,
                        item,
                        item.itemsOrdered + 1,
                        collectionData,
                        object : IClickListener<Any> {
                            override fun click(`object`: Any?) {
                                mainActivity!!.updateCart()
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
                    mainActivity!!.updateCart()
                    Handler(Looper.getMainLooper()).post { adapter.notifyDataSetChanged() }
                    if (item.isSingleSelection) {
                        ViewRouter.instance().goToBasket(context, activeService, null)
                    }
                }
            }
        })
        val clicker: ItemClickListener<MenuItemObject> = object : ItemClickListener<MenuItemObject>(adapter) {
            override fun onClick(item: MenuItemObject, position: Int) {
                if (model.addToCart(
                        context,
                        item,
                        item.itemsOrdered + 1,
                        collectionData,
                        object : IClickListener<Any> {
                            override fun click(`object`: Any?) {
                                mainActivity!!.updateCart()
                                adapter.notifyDataSetChanged()
                            }
                        }
                    )
                ) {
                    adapter.notifyItemChanged(position)
                    mainActivity!!.updateCart()
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
                        collectionData, object : IClickListener<Any> {
                            override fun click(`object`: Any?) {
                                mainActivity!!.updateCart()
                                adapter.notifyDataSetChanged()
                            }
                        }
                    )
                ) {
                    adapter.notifyItemChanged(position)
                    mainActivity!!.updateCart()
                }
            }
        }
        remove.setClickTimeInterval(0)
        adapter.setQuickAddListener(clicker)
        adapter.setQuickRemoveListener(remove)
        recyclerView.adapter = adapter
        val numberOfColumns = if (Utilities.isMarketplace()) 2 else 3
        val layoutManager: GridLayoutManager = SnappingGridLayoutManager(activity, numberOfColumns)
        layoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    ItemsGridViewAdapter.TYPE_HEADER, ItemsGridViewAdapter.TYPE_SUB_HEADER, ItemsGridViewAdapter.TYPE_ITEM_FULL -> numberOfColumns
                    ItemsGridViewAdapter.TYPE_ITEM -> 1
                    else -> numberOfColumns
                }
            }
        }
        recyclerView.layoutManager = layoutManager
        return rootView
    }

    override fun onResume() {
        super.onResume()
        refreshView()
    }

    fun refreshView() {
        listItems.clear()
        if (arguments == null) return
        if (model.favorites.size != 0) {
            val favoriteServiceBean = model.favorites[arguments!!.getInt(ARG_OBJECT)]
                ?: return
            for (subCat in favoriteServiceBean.categories) {
                listItems.add(subCat)
                listItems.addAll(subCat.items)
            }
        }
        adapter.notifyDataSetChanged()
    }

    companion object {
        const val ARG_OBJECT = "fav_object"
    }
}
