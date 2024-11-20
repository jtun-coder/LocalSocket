package com.dolphin.localsocket.server

import android.net.LocalServerSocket
import android.net.LocalSocket
import android.util.Log
import com.dolphin.localsocket.Const
import com.dolphin.localsocket.cmd.BaseLocalCmd
import com.dolphin.localsocket.cmd.LocalCmd
import com.dolphin.localsocket.listener.LocalServerListener
import com.dolphin.localsocket.listener.LocalTransceiverListener
import com.google.gson.Gson
import java.util.concurrent.Executors

class LocalServer private constructor() : LocalTransceiverListener{
    private val TAG = "LocalSocket"
    private var runFlag = false
    private val clientList = ArrayList<LocalClientTransceiver>()
    var localServerListener : LocalServerListener? = null
    companion object {
        @JvmStatic
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED){
            LocalServer()
        }
    }
    //生成LocketServer 监听
    fun init(name :String = Const.CONNECT_TAG){
        if(runFlag)
            return
        runFlag = true
        val pool = Executors.newSingleThreadExecutor()
        pool.execute {
            val localSocketServer = LocalServerSocket(name)
            Log.i(TAG,"init server success $localSocketServer")
            while (runFlag) {
                try {
                    val socket: LocalSocket = localSocketServer.accept()
                    startLocalClient(socket)
                } catch (e: Exception) {
                    // 接受客户端连接出错
                    e.printStackTrace()
                }
            }
        }
    }
    /**
     * 启动与客户端通讯
     */
    private fun startLocalClient(localSocket: LocalSocket){
        Log.i(TAG,"connect localSocket : $localSocket")
        val localClientTransceiver = LocalClientTransceiver(localSocket,this)
        localClientTransceiver.start()
        clientList.add(localClientTransceiver)
    }

    override fun onReceive(clientTransceiver: LocalClientTransceiver,data: String) {
        val cmdData = Gson().fromJson(data,BaseLocalCmd::class.java)
        when(cmdData.cmd){
            LocalCmd.MODEL ->{
                clientTransceiver.model = cmdData.value
            }
        }
        localServerListener?.onReceive(clientTransceiver,data)
    }

    override fun onDisconnect(clientTransceiver: LocalClientTransceiver) {
        clientList.remove(clientTransceiver)
        localServerListener?.onDisconnect(clientTransceiver)
    }

    override fun onConnect(clientTransceiver: LocalClientTransceiver) {
    }

    override fun onReceiveFile(clientTransceiver: LocalClientTransceiver,path: String) {
    }
    //根据包名获得已连接客户端
    fun getClient(packageName : String) : LocalClientTransceiver?{
        for (client in clientList){
            client.packageInfo?.let {
                if(packageName == it.packageName && client.isConnected()){
                    return client
                }
            }
        }
        return null
    }
    fun killClient(packageName: String){
        for (client in clientList){
            client.packageInfo?.let {
                if(packageName == it.packageName){
                    client.killClient()
                }
            }
        }
    }

}