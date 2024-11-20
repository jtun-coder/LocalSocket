package com.dolphin.localsocket.cmd

object LocalCmd {
    const val MODEL = 1//模式信息
    const val PUSH_ANSWER = 2//抢答模式
    const val CLIENT_LIST = 3//用户列表
    const val CLIENT_MANAGER = 4//列表管理
    const val ANSWER_CLIENT = 5//抢答用户
    const val PACKAGE_INFO = 6 //发送应用包信息
    const val V2RAY_GET_CONFIG = 7//获取v2ray 配置信息
    const val V2RAY_IMPORT_CONFIG = 8//导入v2ray 配置
    const val V2RAY_START = 9 //启动v2ray服务
    const val V2RAY_STOP = 10 //停止v2ray服务
    const val V2RAY_CHECK_CONFIG = 11 //选中指定配置文件
    const val V2RAY_DELETE_CONFIG = 12 //删除指定配置文件
    const val V2RAY_PING_TEST = 13 //测试当前连接信息
    const val ALIST_START = 20 //启动ALIST服务
    const val ALIST_STOP = 21 //关闭AList服务
    const val ALIST_ADMIN_PASS = 22 //管理员密码
    const val ALIST_STATE = 23 //运行状态
    const val OPENVPN_IMPORT = 30 //导入openvpn 配置信息
    const val OPENVPN_START= 31//启动open vpn
    const val OPENVPN_STOP = 32//停止open vpn
    const val OPENVPN_LIST = 33//获取open vpn 配置列表
    const val OPENVPN_REMOVE = 34//删除open vpn 配置

}