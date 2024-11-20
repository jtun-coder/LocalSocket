package com.dolphin.localsocket.listener

import com.dolphin.localsocket.server.LocalClientTransceiver


interface LocalTransceiverListener {
    fun onReceive(clientTransceiver: LocalClientTransceiver,data:String)
    fun onDisconnect(clientTransceiver: LocalClientTransceiver)
    fun onConnect(clientTransceiver: LocalClientTransceiver)
    fun onReceiveFile(clientTransceiver: LocalClientTransceiver,path:String)
}