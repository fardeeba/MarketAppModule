package com.skylarksit.module.pojos

class ActivateUserResponseBean : ResponseBean() {
    @JvmField
    var uid: String? = null

    @JvmField
    var idUser: Int? = null

    @JvmField
    var phoneNumber: String? = null

    @JvmField
    var fullName: String? = null

    @JvmField
    var imageUrl: String? = null

    @JvmField
    var email: String? = null

    @JvmField
    var birthdate: String? = null

    @JvmField
    var gender: String? = null

    @JvmField
    var jwtToken: String? = null
}
