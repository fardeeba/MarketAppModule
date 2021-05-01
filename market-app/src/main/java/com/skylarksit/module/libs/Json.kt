@file:Suppress("DEPRECATION")

package com.skylarksit.module.libs

import com.google.gson.*

class Json {
    private var json: JsonObject? = null
    fun o(key: String?, `object`: JsonElement?): Json {
        json!!.add(key, `object`)
        return this
    }

    fun p(key: String?, value: String?): Json {
        json!!.addProperty(key, value)
        return this
    }

    fun p(key: String?, value: Boolean?): Json {
        json!!.addProperty(key, value)
        return this
    }

    fun p(key: String?, value: Number?): Json {
        json!!.addProperty(key, value)
        return this
    }

    fun toJson(): JsonObject? {
        return json
    }

    fun p(list: String?, packageList: Collection<String?>): Json {
        val array = JsonArray()
        for (obj in packageList) {
            array.add(obj)
        }
        json!!.add(list, array)
        return this
    }

    operator fun set(key: String?, `object`: Any?): Json {
        val gson = Gson().toJson(`object`)
        val parser = JsonParser()
        return o(key, parser.parse(gson))
    }

    companion object {
        @JvmStatic
        fun param(): Json {
            val obj = JsonObject()
            val j = Json()
            j.json = obj
            return j
        }
    }
}
