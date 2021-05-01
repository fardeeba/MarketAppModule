package com.skylarksit.module.pojos

import com.google.android.gms.maps.model.LatLng
import com.skylarksit.module.pojos.services.Coord
import com.skylarksit.module.utils.Utilities
import java.io.Serializable
import java.util.*

class Address(lat: Double?=null, lon: Double?=null) {

    fun validate(): String? {
        return when {
            locality.isNullOrEmpty() -> "Address is required"
            building.isNullOrEmpty() -> "City is required"
            else -> null
        }
    }

    constructor(address: android.location.Address, lat: Double, lon: Double) : this(lat, lon) {
        this.locality = address.featureName
        this.city = address.subAdminArea
        this.country = Country(address.countryName, address.countryCode)
    }

    constructor(addressObject: AddressObject) : this(addressObject.lat, addressObject.lon) {
        this.locality = addressObject.locality
        this.city = addressObject.city
        this.notes = addressObject.notes
        this.unit = addressObject.unit
        this.building = addressObject.building
        this.country = Country(addressObject.countryName, addressObject.countryCode)
    }

    var locality: String? = null
    var street: String? = null
    var country: Country? = null
    var city: String? = null
    var coordinates: Coord? = Coord(lat,lon)
    var building: String? = null
    var unit: String? = null
    var eddressCode: String? = null
    var notes: String? = null
}

data class Country(
    var name: String?,
    var iso: String? = null
)

class AddressResponse : ResponseBean() {
    lateinit var address: Address
}

data class CollectionData(val id:String? = null, val name: String, val type: Type){
    enum class Type {
        HOME_PAGE, FAVORITES, RECOMMENDATION, SEARCH, STORE_SECTION, SERVICE, STORES, DEEP_LINK, BASKET
    }
}

enum class CategoryViewType {
    SCROLL, SWIPE
}

class InventoryResponse(val inventory: Map<String, Int>? = null) : ResponseBean()

class DefaultLocation(
        var lat: Double,
        var lon: Double,
        var label: String,
        var country: String
)

class MissingItem(var id: String, var label: String?, var imageUrl: String?, quantityOnHand:Int){
    var quantityOnHand: Int = quantityOnHand
        get() {
            return if (field < 0) 0 else field
        }
}


class MarketStoreTurf {

    var id: String? = null
    private var points: List<Coord> = listOf()

    private var polygon:List<LatLng>? = null

    fun getTurfPoly(): List<LatLng>? {
        if (polygon == null) {
            val poly = ArrayList<LatLng>()

            if (points.isNotEmpty()) {

                for (coord in points) {
                    poly.add(LatLng(coord.lat, coord.lon))
                }
                polygon = Utilities.bspline(poly)
            }
        }
        return polygon
    }

}

class CartBean (var service: String, var items: List<CartItemBean> ) : Serializable {
    class CartItemBean(var id: String, var qty: Int) : Serializable
}

class CalculateEtaResponse(val promiseTime: Int) : ResponseBean()
