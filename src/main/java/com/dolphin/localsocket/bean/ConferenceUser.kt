package com.dolphin.localsocket.bean

/**
 * 会议用户信息
 */
data class ConferenceUser(
    val name:String,//用户名
    val position:Int,//根据连接顺序生成的序号
    val status:Int,//0 非激活 1 发言
    val castScreen:Int,//投屏 0 不允许 1允许
    val uuid:String)//生成uuid

