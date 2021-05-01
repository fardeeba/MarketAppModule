package com.skylarksit.module.analytics

import android.content.Context
import com.skylarksit.module.R
import com.skylarksit.module.pojos.AddressObject
import com.skylarksit.module.pojos.services.MenuItemObject
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.pojos.Address
import com.skylarksit.module.pojos.CollectionData
import com.skylarksit.module.ui.model.ServicesModel
import com.skylarksit.module.utils.TimestampUtils
import com.skylarksit.module.utils.Utilities
import com.segment.analytics.Analytics
import com.segment.analytics.Properties
import com.segment.analytics.Traits


interface IAnalytics {

    val context: Context?
        get() = ServicesModel.instance().appContext

    fun initialize(context: Context) //once when app is initialized
    fun searchViewed() //when search fragment is opened
    fun productsSearched(query: String) //when search query is complete
    fun storeViewed(store: ServiceObject, collectionData: CollectionData)  //when user click on a store

    fun productClicked(product: MenuItemObject, collectionData: CollectionData) //when product is clicked on
    fun productViewed(product: MenuItemObject, collectionData: CollectionData) //when product appears on the screen, scroller settles or collection appears
    fun productZoomed(product: MenuItemObject, collectionData: CollectionData) //when product is zoomed
    fun productAdded(product: MenuItemObject, collectionData: CollectionData) //when product is added to cart
    fun productRemoved(product: MenuItemObject, collectionData: CollectionData) //when product is removed from cart

    fun productListViewed(productList: List<MenuItemObject>, collectionData: CollectionData)
    fun productListItemsViewed(productList:List<MenuItemObject>, collectionData: CollectionData)
    fun storesListViewed(stores:Collection<ServiceObject>, collectionData: CollectionData)

    fun cartViewed(serviceObject: ServiceObject) //everytime you access the cart
    fun checkoutStarted(orderUid: String, serviceObject: ServiceObject) //when user clicks on checkout button
    fun orderCompleted(orderUid: String, serviceObject: ServiceObject) //after order is placed... put it on checkout response
    fun orderCanceled(orderUid: String, serviceObject: ServiceObject) //when user cancels an order
    fun feedbackSubmitted(orderUid: String, serviceObject: ServiceObject, serviceRating: Int?, storeRating: Int?, comment: String?) //when user submits feedback about service and store

    fun couponEntered(orderUid: String, couponId: String, couponName: String) //when promo code is validated
    fun couponApplied(orderUid: String, couponId: String, couponName: String, discount: Number)  //when order is placed successfully and there is a coupon
    fun couponRemoved(orderUid: String, couponId: String, couponName: String, discount: Number) //when order with promo code is canceled
    fun couponDenied(orderUid: String, couponId: String, couponName: String, reason: String) //when apply promo is rejected

    fun productRemovedFromWishList(product: MenuItemObject, collectionData: CollectionData) //remove from favorites
    fun productAddedToWishList(product: MenuItemObject, collectionData: CollectionData) //add to favorites

    fun activationStarted(phoneNumber: String) //on request activation
    fun activationCompleted() //on activation response
    fun identify(id: String) //on login and register existing user
    fun register(id: String, phoneNumber: String) //on register new user
    fun identify(id: String?, phoneNumber: String, name: String, email: String)

    fun locationReceived(location: Address)
    fun appShared()
    fun supportContacted(orderUid: String, serviceObject: ServiceObject)
    fun driverContacted(orderUid: String, serviceObject: ServiceObject)
    fun addressCreated(addressObject: AddressObject)
    fun creditCardAdded()

    fun getStoreValues(store: ServiceObject, collectionData: CollectionData?): Properties {
        val properties = Properties()
        properties["id"] = store.slug
        properties["name"] = store.name
        properties["type"] = store.serviceName
        properties["tags"] = store.storeTags
        properties["image_url"] = store.imageUrl
        collectionData?.let {
            properties["collection"] = getCollectionValues(it)
        }
        return properties
    }

    fun getCouponValues(orderUid: String, couponId: String, couponName: String, discount: Number?=null): Properties {
        val properties = Properties()
        properties["order_id"] = orderUid
        properties["cart_id"] = ServicesModel.instance().cartSessionId
        properties["coupon_id"] = couponId
        properties["coupon_name"] = couponName
        discount?.let{
            properties["discount"] = discount
        }
        return properties
    }

    fun getOrderValues(store: ServiceObject): Properties {

        val properties = Properties()
        with(ServicesModel.instance()){
            properties["affiliation"] = "Google Store"
            properties["store"] = getStoreValues(store,null)
            properties["revenue"] = subtotalPrice
            properties["total"] = totalPrice
            properties["surcharge"] = surcharge
            properties["tips"] = tip
            properties["shippingTax"] = deliveryVatAmount
            properties["shipping"] = deliveryPrice
            properties["discount"] = discount
            properties["wallet"] = deductionFromWalletInServiceCurrency
            properties["currency"] = store.currency.iso
            properties["products"] = cart.map { getProductValues(it) }
            promo?.let{
                properties["coupon"] = it.uid
            }
        }
        return properties
    }

    fun getCollectionValues(collectionData: CollectionData): Properties {
        val properties = Properties()
        properties["list_id"] = collectionData.id
        properties["name"] = collectionData.name
        properties["category"] = collectionData.type
        return properties
    }
    fun getProductValues(product: MenuItemObject, collectionData: CollectionData?=null): Properties {

        val properties = Properties()
        properties.putSku(product.sku)
        properties.putCategory(product.category)
        properties.putName(product.label)
        properties.putProductId(product.id)
        properties.putPrice(product.price)
        properties.putCurrency(product.service.currency.iso)
        properties["tags"] = product.tags
        properties["subcategory"] = product.subcategory
        properties["image_url"] = product.imageUrl
        properties["variant"] = product.description
        properties["store"] = getStoreValues(product.service,null)
        properties["brand"] = product.brandName
        properties["quantity"] = product.itemsOrdered
        collectionData?.let {
            properties["collection"] = getCollectionValues(it)
        }

        if (product.discountLabel != null){
            product.strikedPrice?.let{
                val discount = product.strikedPrice - product.price
                if (discount > 0)
                    properties.putDiscount(discount)
            }
        }

        return properties
    }


    fun getAddressProperties(addressObject: AddressObject): Properties {

        val properties = Properties()

        properties["description"] = addressObject.notes
        properties["city"] = addressObject.locality
        properties["country"] = addressObject.countryCode
        properties["eddress"] = addressObject.code
        properties["building"] = addressObject.building
        properties["unit"] = addressObject.unit
        properties["coordinates"] = Properties().also {it["latitude"] = addressObject.lat}.also { it["longitude"] = addressObject.lon }

        return properties
    }

    fun getLocationValues(location: Address): Properties {

        val properties = Properties()

        properties["city"] = location.city
        properties["country"] = location.country
        properties["street"] = location.street
        properties["locality"] = location.locality
        if (location.coordinates!=null)
            properties["coordinates"] = Properties().also {it["latitude"] = location.coordinates?.lat}.also { it["longitude"] = location.coordinates?.lon }

        return properties
    }


}

object Segment : IAnalytics {

    var isActive:Boolean = false

    override fun productListItemsViewed(productList:List<MenuItemObject>, collectionData: CollectionData) {

        if (!isActive) return

        if (productList.isEmpty()) return

        val props = productList.filter { it.price != null }.map { Segment.getProductValues(it, null) }
        props.forEachIndexed { index, element -> element["position"] = index }

        val properties = Segment.getCollectionValues(collectionData)
        properties["products"] = props
        Analytics.with(Segment.context).track("Product List Items Viewed", properties)

    }

    override fun productListViewed(productList:List<MenuItemObject>, collectionData: CollectionData) {
        if (!isActive) return

        val props = productList.filter { it.price != null }.map { getProductValues(it,null) }
        props.forEachIndexed { index, element -> element["position"] = index }

        val properties = getCollectionValues(collectionData)
        properties["products"] = props
        Analytics.with(context).track("Product List Viewed", properties)

    }
    override fun storesListViewed(stores:Collection<ServiceObject>, collectionData: CollectionData) {
        if (!isActive) return

        val props = stores.map { getStoreValues(it,null) }
        props.forEachIndexed { index, element -> element["position"] = index }

        val properties = getCollectionValues(collectionData)
        properties["stores"] = props

        Analytics.with(context).track("Stores List Viewed", properties)
    }

    override fun appShared() {
        if (!isActive) return
        Analytics.with(context).track("App Shared")
    }

    override fun supportContacted(orderUid: String, serviceObject: ServiceObject) {
        if (!isActive) return
        val properties = Properties().also { it["store"] = getStoreValues(serviceObject,null) }.also { it["order_id"] = orderUid }
        Analytics.with(context).track("Support Contacted", properties)
    }

    override fun driverContacted(orderUid: String, serviceObject: ServiceObject) {

        if (!isActive) return

        val properties = Properties().also { it["store"] = getStoreValues(serviceObject,null) }.also { it["order_id"] = orderUid }
        Analytics.with(context).track("Worker Contacted", properties)
    }

    override fun creditCardAdded() {

        if (!isActive) return

        Analytics.with(context).track("Credit Card Added")

    }

    override fun feedbackSubmitted(orderUid: String, serviceObject: ServiceObject, serviceRating: Int?, storeRating: Int?, comment: String?) {

        if (!isActive) return

        val properties = Properties().also { it["serviceRating"] = serviceRating }.also { it["comment"] = comment }.also { it["storeRating"] = storeRating }
        Analytics.with(context).track("Feedback Submitted", getOrderValues(serviceObject).also { it["order_id"] = orderUid }.also { it["serviceFeedback"] = properties })
    }

    override fun couponEntered(orderUid: String, couponId:String, couponName: String) {

        if (!isActive) return

        Analytics.with(context).track("Coupon Entered", getCouponValues(orderUid, couponId, couponName))
    }

    override fun couponApplied(orderUid: String, couponId: String, couponName: String, discount: Number) {

        if (!isActive) return

        Analytics.with(context).track("Coupon Applied", getCouponValues(orderUid, couponId, couponName, discount))
    }

    override fun couponRemoved(orderUid: String, couponId: String, couponName: String, discount: Number) {
        if (!isActive) return
        Analytics.with(context).track("Coupon Removed", getCouponValues(orderUid, couponId, couponName, discount))
    }

    override fun couponDenied(orderUid: String, couponId: String, couponName: String, reason: String) {
        if (!isActive) return
        Analytics.with(context).track("Coupon Denied", getCouponValues(orderUid, couponId, couponName).also { it["reason"] = reason })
    }

    override fun storeViewed(store: ServiceObject, collectionData: CollectionData) {
        if (!isActive) return
        Analytics.with(context).track("Store Viewed", getStoreValues(store,collectionData))
    }

    override fun productClicked(product: MenuItemObject, collectionData: CollectionData) {
        if (!isActive) return
        Analytics.with(context).track("Product Clicked", getProductValues(product,collectionData))
    }

    override fun productViewed(product: MenuItemObject, collectionData: CollectionData) {
        if (!isActive) return
        Analytics.with(context).track("Product Viewed", getProductValues(product,collectionData))
    }

    override fun productZoomed(product: MenuItemObject, collectionData: CollectionData) {
        if (!isActive) return
        Analytics.with(context).track("Product Zoomed", getProductValues(product,collectionData))
    }

    override fun productAdded(product: MenuItemObject, collectionData: CollectionData) {
        if (!isActive) return
        Analytics.with(context).track("Product Added", getProductValues(product,collectionData).also { it["cart_id"] = ServicesModel.instance().cartSessionId })
    }

    override fun productsSearched(query: String) {
        if (!isActive) return
        Analytics.with(context).track("Products Searched", Properties().also { it["query"] = query } )
    }

    override fun activationStarted(phoneNumber: String) {
        if (!isActive) return
        Analytics.with(context).track("Activation Started", Properties().also { it["phone_number"] = phoneNumber })
    }

    override fun activationCompleted() {
        if (!isActive) return
        Analytics.with(context).track("Activation Completed")
    }

    override fun addressCreated(addressObject: AddressObject) {
        if (!isActive) return
        val properties = getAddressProperties(addressObject)
        val traits = Traits().also {it["address"] = properties}
        Analytics.with(context).identify(null, traits, null)
    }

//    override fun productCollectionViewed(collection:CollectionData, items: List<MenuItemObject>) {
//
//        val properties = Properties()
//        properties["list_id"] = collection.id
//        properties["category"] = collection.name
//        properties["list_type"] = collection.type
//        properties["products"] = items.map { getProductValues(it) }
//        Analytics.with(context).track("Product List Viewed", properties)
//
//    }

    override fun productRemoved(product: MenuItemObject, collectionData: CollectionData) {
        if (!isActive) return
        Analytics.with(context).track("Product Removed", getProductValues(product,collectionData).also { it["cart_id"] = ServicesModel.instance().cartSessionId })
    }

    override fun cartViewed(serviceObject: ServiceObject) {
        if (!isActive) return
        Analytics.with(context).track("Cart Viewed", getOrderValues(serviceObject).also { it["cart_id"] = ServicesModel.instance().cartSessionId })
    }

    override fun checkoutStarted(orderUid: String, serviceObject: ServiceObject) {
        if (!isActive) return
        Analytics.with(context).track("Checkout Started", getOrderValues(serviceObject).also { it["order_id"] = orderUid })
    }

    override fun orderCompleted(orderUid: String, serviceObject: ServiceObject) {
        if (!isActive) return
        Analytics.with(context).track("Order Completed", getOrderValues(serviceObject).also { it["order_id"] = orderUid })
    }

    override fun orderCanceled(orderUid: String, serviceObject: ServiceObject) {
        if (!isActive) return
        Analytics.with(context).track("Order Canceled", getOrderValues(serviceObject).also { it["order_id"] = orderUid })
    }

    override fun productRemovedFromWishList(product: MenuItemObject, collectionData: CollectionData) {
        if (!isActive) return
        Analytics.with(context).track("Product Removed from Wishlist", getProductValues(product,collectionData).also { it["wishlist_id"] = "my_favorites" }.also { it["wishlist_name"] = "Favorites" })
    }

    override fun productAddedToWishList(product: MenuItemObject, collectionData: CollectionData) {
        if (!isActive) return
        Analytics.with(context).track("Product Added to Wishlist", getProductValues(product,collectionData).also { it["wishlist_id"] = "my_favorites" }.also { it["wishlist_name"] = "Favorites" })
    }

    override fun initialize(context: Context) {

        val segmentKey: String = context.getString(R.string.segment_key)

        if (!Utilities.isEmpty(segmentKey)) {
            val analytics = Analytics.Builder(context, segmentKey)
                    .trackApplicationLifecycleEvents()
                    .collectDeviceId(true)
                    .recordScreenViews()
                    .build()
            Analytics.setSingletonInstance(analytics)

            isActive = true
        }
    }

    override fun searchViewed() {
        if (!isActive) return
        Analytics.with(context).screen("Search View")
    }

    override fun identify(id: String) {
        if (!isActive) return
        Analytics.with(context).identify(id)
    }

    override fun identify(id: String?, phoneNumber: String, name: String, email: String) {
        if (!isActive) return
        Analytics.with(context).identify(id, Traits().putPhone(phoneNumber).putName(name).putEmail(email), null)
    }

    override fun register(id: String, phoneNumber: String) {
        if (!isActive) return
        Analytics.with(context).identify(id, Traits().putPhone(phoneNumber).putCreatedAt(TimestampUtils.getISO8601StringForCurrentDate()), null)
    }

    override fun locationReceived(location: Address) {
        if (!isActive) return
        val trait = Traits().also{ it["location"] = getLocationValues(location)}
        Analytics.with(context).identify(null, trait, null)
    }


}
