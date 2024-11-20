package com.dolphin.localsocket.listener

import com.dolphin.localsocket.client.LocalClient


interface LocalClientListener {
    fun onReceive(client: LocalClient,data:String?)
    fun onDisconnect(client: LocalClient)
    fun onConnect(client: LocalClient)
    fun onReceiveFile(client: LocalClient,path:String?)
}