package com.dolphin.localsocket.listener

import com.dolphin.localsocket.server.LocalClientTransceiver

interface LocalServerListener {
    fun onReceive(clientTransceiver: LocalClientTransceiver, data:String)
    fun onDisconnect(clientTransceiver: LocalClientTransceiver)
}