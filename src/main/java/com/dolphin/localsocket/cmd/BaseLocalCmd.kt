package com.dolphin.localsocket.cmd

import com.google.gson.Gson

/**
 * 发送命令封装类
 */
data class BaseLocalCmd<T>(val cmd: Int, val message: String = "",val value:Int = 0,val data : T? = null){
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
