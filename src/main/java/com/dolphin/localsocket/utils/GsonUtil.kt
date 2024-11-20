package com.dolphin.localsocket.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object GsonUtil {

    public inline fun <reified T> getGsonObject(json: String): T? {
        val result = Gson().fromJson<T>(json)
        return result

    }
    //type 泛型扩展
    inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)


}