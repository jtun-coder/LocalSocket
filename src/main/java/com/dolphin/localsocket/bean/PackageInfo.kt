package com.dolphin.localsocket.bean


/**
 * 第三方应用数据
 * packageName 包名
 * versionCode 版本号
 * versionName 版本名称
 * name 应用名称
 * brief 应用简介
 */
data class PackageInfo(val packageName:String,
                       val canonicalName :String?,
                       val versionCode:Long,
                       val versionName : String,
                       val name:String,
                       val brief:String,
                       val isRun : Boolean)
