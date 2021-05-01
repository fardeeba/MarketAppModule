@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.activities.hyperlocal

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.google.gson.Gson
import com.skylarksit.module.INavBarActivity
import com.skylarksit.module.R
import com.skylarksit.module.analytics.Segment.productAddedToWishList
import com.skylarksit.module.analytics.Segment.productRemovedFromWishList
import com.skylarksit.module.analytics.Segment.productZoomed
import com.skylarksit.module.lib.Rest
import com.skylarksit.module.libs.Json
import com.skylarksit.module.pojos.CollectionData
import com.skylarksit.module.pojos.services.IProductCustomizationItem
import com.skylarksit.module.pojos.services.MenuCategoryObject
import com.skylarksit.module.pojos.services.MenuItemObject
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.activities.SharedElementLaunchedActivity
import com.skylarksit.module.ui.components.NavigationBar
import com.skylarksit.module.ui.components.ProductListView
import com.skylarksit.module.ui.lists.main.adapters.CustomizationAdapter
import com.skylarksit.module.ui.model.ServicesModel
import com.skylarksit.module.ui.utils.IClickListener
import com.skylarksit.module.ui.utils.OnOneOffClickListener
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.NonScrollingManager
import com.skylarksit.module.utils.U
import com.skylarksit.module.utils.Utilities
import java.util.*

class ProductActivity : MyAppCompatActivity(), INavBarActivity {


    lateinit var image: SimpleDraweeView
    private lateinit var images: View
    private lateinit var arrowRight: TextView
    private lateinit var watermark: SimpleDraweeView
    private lateinit var addButtonLayout: RelativeLayout
    lateinit var title: TextView
    private lateinit var tags: TextView
    private lateinit var promoTag: TextView
    private lateinit var scroller: NestedScrollView
    lateinit var remove: ImageView
    lateinit var add: ImageView
    private lateinit var favorite: ImageView
    private lateinit var specialNote: TextView
    lateinit var price: TextView
    private lateinit var strikedPrice: TextView
    private lateinit var discount: TextView
    private lateinit var unitType: TextView
    lateinit var description: TextView
    private lateinit var itemCount: TextView
    private lateinit var addItem: TextView
    lateinit var addRemove: View
    private lateinit var relatedProductsList: ProductListView
    private lateinit var relatedSupplier: ProductListView
    private lateinit var relatedSubcategories: ProductListView
    private lateinit var customizations: RecyclerView
    private lateinit var searchLayout: View
    lateinit var navigationBar: NavigationBar

    override var model: ServicesModel = ServicesModel.instance()
    var product: MenuItemObject? = null
    lateinit var serviceObject: ServiceObject
    private var isEditable = false
    private var itemQty = 0
    private var sourceCollection: CollectionData? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.product_activity)
        setFinishOnTouchOutside(false)

        image = findViewById(R.id.image)
        arrowRight = findViewById(R.id.arrowRight)
        images = findViewById(R.id.images)
        watermark = findViewById(R.id.watermark)
        addButtonLayout = findViewById(R.id.addButtonLayout)
        title = findViewById(R.id.label)
        tags = findViewById(R.id.tags)
        promoTag = findViewById(R.id.promoTag)
        scroller = findViewById(R.id.scroller)
        remove = findViewById(R.id.remove)
        add = findViewById(R.id.add)
        favorite = findViewById(R.id.favorite)
        specialNote = findViewById(R.id.specialNote)
        price = findViewById(R.id.priceText)
        strikedPrice = findViewById(R.id.strikedPrice)
        discount = findViewById(R.id.discount)
        unitType = findViewById(R.id.unitType)
        description = findViewById(R.id.description)
        itemCount = findViewById(R.id.itemCount)
        addItem = findViewById(R.id.addItem)
        navigationBar = findViewById(R.id.navigationBar)
        searchLayout = findViewById(R.id.searchLayout)
        customizations = findViewById(R.id.customizations)
        relatedSubcategories = findViewById(R.id.relatedSubcategories)
        relatedSupplier = findViewById(R.id.relatedSupplier)
        relatedProductsList = findViewById(R.id.relatedProducts)
        addRemove = findViewById(R.id.addRemove)

        addItem.setOnClickListener { addToCart() }
        add.setOnClickListener { add() }
        remove.setOnClickListener { remove() }
        arrowRight.setOnClickListener { goBack() }


        val collectionDataString = intent.getStringExtra("collectionData")
        if (collectionDataString != null) {
            sourceCollection = Gson().fromJson(collectionDataString, CollectionData::class.java)
        }
        product = model.activeProduct
        if (product == null) return
        product!!.isFavorite = model.favoriteIds.contains(product!!.id)
        if (Utilities.isLoggedIn()) {
            updateFavoriteColor()
            addTooltip(favorite, R.string.tip_favorites)
        } else {
            favorite.visibility = View.GONE
        }
        if (Utilities.isEmpty(product!!.promoTag)) {
            promoTag.visibility = View.GONE
        } else {
            promoTag.visibility = View.VISIBLE
            promoTag.text = product!!.promoTag
        }
        favorite.setOnClickListener(object : OnOneOffClickListener() {
            override fun onSingleClick(v: View) {
                product!!.isFavorite = !product!!.isFavorite
                updateFavoriteColor()
                if (product!!.isFavorite) {
                    if (model.favoriteIds.size == 0) model.favoritesTooltip = true
                    model.favoriteIds.add(product!!.id)
                    model.favoritesProducts.add(product)
                    productAddedToWishList(product!!, sourceCollection!!)
                } else {
                    model.favoritesProducts.remove(product)
                    model.favoriteIds.remove(product!!.id)
                    productRemovedFromWishList(product!!, sourceCollection!!)
                }
                val oldSize = model.favorites.size
                model.buildFavorites()
                if (oldSize != model.favorites.size) {
                    model.favoritesTabsModified = true
                } else {
                    model.favoritesViewModified = true
                }
                val param = Json.param().p("productId", product!!.id).p("addItem", product!!.isFavorite).toJson()
                Rest.request().baseUrl("api/market/app/").uri("addToFavorites/").params(param).post()
            }
        })
        if (product!!.isItemSelected) itemQty = product!!.itemsOrdered
        isEditable = intent.getBooleanExtra("isEditable", false)
        val fromProvider = intent.getBooleanExtra("fromProvider", false)
        if (product == null) return
        serviceObject = product!!.service
        price.text = product!!.getPrice()
        if (product!!.discountLabel != null) {
            discount.visibility = View.VISIBLE
            discount.text = product!!.discountLabel
        } else {
            discount.visibility = View.GONE
        }
        if (product!!.strikedPriceLabel != null) {
            strikedPrice.text = product!!.strikedPriceLabel
            strikedPrice.paintFlags = strikedPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            strikedPrice.visibility = View.GONE
        }
        if (product!!.unitLabel != null) {
            unitType.text = product!!.unitLabel
            unitType.visibility = View.VISIBLE
        } else {
            unitType.visibility = View.GONE
        }
        if (product!!.getDescription() != null && product!!.getDescription().isNotEmpty()) {
            description.visibility = View.VISIBLE
            description.text = product!!.getDescription()
        } else {
            description.visibility = View.GONE
        }
        if (product!!.specialNote != null && product!!.specialNote.isNotEmpty()) {
            specialNote.visibility = View.VISIBLE
            specialNote.text = product!!.specialNote
        } else {
            specialNote.visibility = View.GONE
        }
        title.text = product!!.getLabel()
        images.visibility = if (serviceObject.hideImages) View.GONE else View.VISIBLE
        image.setImageURI(U.parse(product!!.imageUrl))
        ViewCompat.setTransitionName(image, SHARED_ELEMENT_NAME)
        image.setOnClickListener { zoomImage() }
        watermark.visibility = if (Utilities.isEmpty(product!!.watermarkUrl)) View.GONE else View.VISIBLE
        watermark.setImageURI(U.parse(product!!.watermarkUrl))
        if (product!!.customizationItems != null && product!!.customizationItems.size > 0) {
            val items: List<IProductCustomizationItem> = ArrayList<IProductCustomizationItem>(
                product!!.customizationItems
            )
            val adapter = CustomizationAdapter(this, items, serviceObject)
            customizations.adapter = adapter
            customizations.visibility = View.VISIBLE
            customizations.layoutManager = NonScrollingManager(this)
        } else customizations.visibility = View.GONE
        val relatedProducts: MutableList<MenuItemObject> = ArrayList()
        if (serviceObject.showRecommendations()) {
            if (product!!.recommendationTags != null && model.productRecommendations != null && model.productRecommendations.isNotEmpty()) {
                for (tag in product!!.recommendationTags) {
                    val tags = model.productRecommendations[tag]
                    if (tags != null) {
                        for (t in tags) {
                            for (product in model.productsMap.values) {
                                if (product.recommendationTags != null && product.recommendationTags.contains(t)) {
                                    relatedProducts.add(product)
                                }
                            }
                        }
                    }
                }
            }
            relatedProducts.sortWith { t0: MenuItemObject, t1: MenuItemObject ->
                t1.recommendationLevel.compareTo(
                    t0.recommendationLevel
                )
            }
        }
        val relatedSubs: MutableList<MenuItemObject> = ArrayList()
        if (serviceObject.showRecommendations()) {
            val menuCategoryObject = subCategory
            if (menuCategoryObject != null) {
                menuCategoryObject.items.sort()
                for (itemObject in menuCategoryObject.items) {
                    if (product!!.sku != itemObject.sku) {
                        relatedSubs.add(itemObject)
                    }
                }
            }
            relatedSubs.sortWith { t0: MenuItemObject, t1: MenuItemObject ->
                t1.recommendationLevel.compareTo(
                    t0.recommendationLevel
                )
            }
        }
        val labelRecommend = getString(R.string.recommeded_for_you)
        val relatedCollection = CollectionData("related_products", labelRecommend, CollectionData.Type.RECOMMENDATION)
        relatedProductsList.initialize(labelRecommend, relatedProducts, relatedCollection, fromProvider, false)
        relatedProductsList.setUpdateListener(object : IClickListener<MenuItemObject> {
            override fun click(`object`: MenuItemObject?) {
                updateCart()
            }

        })
        relatedProductsList.visibility = if (relatedProducts.size > 0) View.VISIBLE else View.GONE
        val labelCategories = getString(R.string.related_cateogries)
        val sameSubsCollection = CollectionData("same_categories", labelCategories, CollectionData.Type.RECOMMENDATION)
        relatedSubcategories.initialize(labelCategories, relatedSubs, sameSubsCollection, fromProvider, false)
        relatedSubcategories.setUpdateListener(object : IClickListener<MenuItemObject> {
            override fun click(`object`: MenuItemObject?) {
                updateCart()
            }

        })
        relatedSubcategories.visibility = if (relatedSubs.size > 0) View.VISIBLE else View.GONE
        val sameSupplier: MutableList<MenuItemObject> = ArrayList()
        for (item in model.productsMap.values) {
            if (item == product) continue
            if (item.brandName != null && item.service == product!!.service && item.brandName == product!!.brandName) {
                sameSupplier.add(item)
            }
        }
        sameSupplier.sortWith { t0: MenuItemObject, t1: MenuItemObject -> t1.recommendationLevel.compareTo(t0.recommendationLevel) }
        val label = getString(R.string.other_products_from) + " " + product!!.brandName
        val brandCollection = CollectionData("related_brand", label, CollectionData.Type.RECOMMENDATION)
        relatedSupplier.initialize(label, sameSupplier, brandCollection, fromProvider, false)
        relatedSupplier.setUpdateListener(object : IClickListener<MenuItemObject> {
            override fun click(`object`: MenuItemObject?) {
                updateCart()
            }
        })
        relatedSupplier.visibility = if (sameSupplier.size > 0) View.VISIBLE else View.GONE
        if (product!!.tags != null && product!!.tags.isNotEmpty()) {
            tags.visibility = View.VISIBLE
            tags.text = product!!.tags.toString().replace("[\\[.\\]]".toRegex(), "")
        } else {
            tags.visibility = View.GONE
        }
        scroller.post { scroller.scrollTo(0, 0) }
        navigationBar.init(this, serviceObject)
        updateCart()
    }

    private val subCategory: MenuCategoryObject?
        get() {

            for (menuCategoryObject in product!!.service.categories) {
                for (subCategory in menuCategoryObject.subcategories) {
                    if (product!!.subcategory != null && product!!.subcategory == subCategory.getLabel()) {
                        return subCategory
                    }
                }
            }
            return null
        }

    private fun zoomImage() {
        val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this@ProductActivity, image, SHARED_ELEMENT_NAME
        )

        // FIX BUG https://github.com/facebook/fresco/issues/1445
        ActivityCompat.setExitSharedElementCallback(this@ProductActivity,
            object : SharedElementCallback() {
                override fun onSharedElementEnd(
                    sharedElementNames: List<String>,
                    sharedElements: List<View>, sharedElementSnapshots: List<View>
                ) {
                    super.onSharedElementEnd(
                        sharedElementNames, sharedElements,
                        sharedElementSnapshots
                    )
                    for (view in sharedElements) {
                        (view as? SimpleDraweeView)?.visibility = View.VISIBLE
                    }
                }
            })
        val intent = Intent(this@ProductActivity, SharedElementLaunchedActivity::class.java)
        intent.putExtra("imageUrl", product!!.getImageUrl())
        productZoomed(product!!, sourceCollection!!)
        ActivityCompat.startActivity(
            this@ProductActivity,
            intent,
            optionsCompat.toBundle()
        )
    }

    private fun updateFavoriteColor() {
        favorite.setColorFilter(
            if (product!!.isFavorite) ContextCompat.getColor(
                context,
                R.color.favoriteFillColor
            ) else ContextCompat.getColor(context, R.color.white)
        )
        favorite.background =
            if (product!!.isFavorite) getDrawable(R.drawable.circle_white) else getDrawable(R.drawable.circle_grey)
    }


    private fun addToCart() {
        add()
    }

    override fun finish() {
        val returnIntent = Intent()
        if (product != null) returnIntent.putExtra("productSku", product!!.sku)
        setResult(RESULT_OK, returnIntent)
        super.finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out)
    }


    fun add() {
        if (product!!.isOutOfStock) {
            return
        }
        if (product!!.isSingleSelection && itemQty > 0) {
            return
        }
        if (model.addToCart(this@ProductActivity, product, itemQty + 1, sourceCollection, null)) {
            itemQty++
            updateCart()
        }
    }


    fun remove() {
        if (itemQty <= 0 || product!!.minQty != null && itemQty <= product!!.minQty) itemQty = 0 else itemQty--
        if (model.addToCart(this@ProductActivity, product, itemQty, sourceCollection, null)) updateCart()
    }

    private fun updateCart() {
        if (isEditable) {
            addButtonLayout.visibility = View.VISIBLE
            if (product!!.isOutOfStock) {
                addItem.alpha = 0.7f
                addItem.text = getString(R.string.out_of_stock)
                addRemove.visibility = View.GONE
                addButtonLayout.elevation = Utilities.dpToPx(1).toFloat()
                addButtonLayout.background = context.getDrawable(R.drawable.rounded_corners_grey)
            } else {
                addButtonLayout.elevation = resources.getDimension(R.dimen.toolbarElevation)
                if (itemQty > 0) {
                    addItem.visibility = View.GONE
                    itemCount.text = product!!.itemsCount()
                    addRemove.visibility = View.VISIBLE
                    addButtonLayout.background = context.getDrawable(R.drawable.rounded_corners_button_secondary)
                } else {
                    addItem.alpha = 1f
                    addItem.text = getString(R.string.add_to_basket)
                    addItem.visibility = View.VISIBLE
                    addRemove.visibility = View.GONE
                    addButtonLayout.background = context.getDrawable(R.drawable.rounded_corners_white_10)
                }
            }
        } else {
            addButtonLayout.visibility = View.GONE
        }
        itemCount.text = product!!.itemsCount(itemQty)
        navigationBar.refreshView()
    }


    private fun goBack() {
        saveValue("finishAllProductsPopup", true)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (getBooleanValue("finishAllProductsPopup") || getBooleanValue("continueFinish")) {
            finish()
            return
        }
        relatedProductsList.refresh()
        relatedSupplier.refresh()
    }

    override fun getSearchView(): View {
        return searchLayout
    }

    companion object {
        const val SHARED_ELEMENT_NAME = "SHARED_ELEMENT_NAME"
    }
}
