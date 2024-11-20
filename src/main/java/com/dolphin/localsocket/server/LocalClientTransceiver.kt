package com.dolphin.localsocket.server

import android.net.LocalSocket
import android.text.TextUtils
import android.util.Log
import com.dolphin.localsocket.LocalDataType
import com.dolphin.localsocket.bean.PackageInfo
import com.dolphin.localsocket.listener.LocalTransceiverListener
import com.dolphin.localsocket.utils.Utils
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.concurrent.Executors

/**
 * 本地控制端连接收发器
 */
class LocalClientTransceiver(
    private val localSocket: LocalSocket,
    private val localClientListener: LocalTransceiverListener
) : Runnable {
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var runFlag = false
    private val TAG = LocalClientTransceiver::class.java.simpleName
    var model = 0
    var packageInfo : PackageInfo? = null
    override fun run() {
        /* start localSocket listener */
        try {
            inputStream = localSocket.inputStream
            outputStream = localSocket.outputStream
            //接收包名信息
            receive()
        } catch (e: Exception) {
            e.printStackTrace()
            localClientListener.onDisconnect(this)
            stop()
        }
    }
    fun receiveCmd():String{
        try {
            /**得到的是16进制数，需要进行解析 */
            val bt = ByteArray(10)
            //获取接收到的字节和字节数
            var length = inputStream?.read(bt)
            if (length == -1) {
                throw Exception("length -1")
            }
            if (length == 10) {
                val lenBytes = ByteArray(8)
                System.arraycopy(bt, 2, lenBytes, 0, 8)
                val len = Utils.bytesToLong(lenBytes)
                when (bt[0]) {
                    LocalDataType.COMMAND -> {
                        //接收文本命令
                        return receiveString(len)
                    }
                }
            }
        } catch (e: Exception) {
            // 连接被断开(被动)
        }
        return ""
    }
    private fun receive(){
            try {
                /**得到的是16进制数，需要进行解析 */
                val bt = ByteArray(10)
                //获取接收到的字节和字节数
                var length = inputStream?.read(bt)
                if (length == -1) {
                    throw Exception("length -1")
                }
                if (length == 10) {
                    val lenBytes = ByteArray(8)
                    System.arraycopy(bt, 2, lenBytes, 0, 8)
                    val len = Utils.bytesToLong(lenBytes)
                    when (bt[0]) {
                        LocalDataType.END -> {
                            //结束连接
                            localClientListener.onDisconnect(this)
                            stop()
                        }
                        LocalDataType.COMMAND -> {
                            //接收文本命令
                            receiveMsg(len)
                        }
                        LocalDataType.FILE_SEND -> {}
                    }
                }
            } catch (e: Exception) {
                // 连接被断开(被动)
                e.printStackTrace()
                localClientListener.onDisconnect(this)
                stop()
            }
    }

    /**
     * 发送字符串
     *
     *
     * 字符串
     *
     * @return 发送成功返回true
     */
    @Synchronized
    open fun send(data: ByteArray): Boolean {
        try {
            val sendBytes = ByteArray(10)
            sendBytes[0] = LocalDataType.COMMAND
            sendBytes[1] = ((255 - LocalDataType.COMMAND).toByte())
            val sendLength: ByteArray = Utils.longToBytes(data.size.toLong())
            System.arraycopy(sendLength, 0, sendBytes, 2, 8)
            writeData(sendBytes, 0, sendBytes.size)
            writeData(data, 0, data.size)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @Throws(IOException::class)
    private fun receiveString(receiveLen: Long) :String{
        /**得到的是16进制数，需要进行解析 */
        var receiveLen = receiveLen
        val bt = ByteArray(receiveLen.toInt())
        //获取接收到的字节和字节数
        var readBt: ByteArray? = null
        readBt = if (receiveLen >= 1024) {
            ByteArray(1024)
        } else {
            ByteArray(receiveLen.toInt())
        }
        var length = 0
        while (receiveLen > 0 && inputStream?.read(readBt).also { length = it!! } != -1) {
            System.arraycopy(readBt, 0, bt, (bt.size - receiveLen).toInt(), length)
            receiveLen -= length
            readBt = if (receiveLen >= 1024) {
                ByteArray(1024)
            } else {
                ByteArray(receiveLen.toInt())
            }
        }
        val str = String(bt, Charset.forName("UTF-8"))
        Log.i(TAG,"server transceiver $str")
        return str
    }
    @Throws(IOException::class)
    private fun receiveMsg(receiveLen: Long) {
        /**得到的是16进制数，需要进行解析 */
        var receiveLen = receiveLen
        val bt = ByteArray(receiveLen.toInt())
        //获取接收到的字节和字节数
        var readBt: ByteArray? = null
        readBt = if (receiveLen >= 1024) {
            ByteArray(1024)
        } else {
            ByteArray(receiveLen.toInt())
        }
        var length = 0
        while (receiveLen > 0 && inputStream?.read(readBt).also { length = it!! } != -1) {
            System.arraycopy(readBt, 0, bt, (bt.size - receiveLen).toInt(), length)
            receiveLen -= length
            readBt = if (receiveLen >= 1024) {
                ByteArray(1024)
            } else {
                ByteArray(receiveLen.toInt())
            }
        }
        val str = String(bt, Charset.forName("UTF-8"))
        Log.i(TAG,"server transceiver $str")
        localClientListener.onReceive(this,str)
    }

    @Synchronized
    @Throws(IOException::class)
    open fun writeData(data: ByteArray, off: Int, length: Int) {
        // 写入数据
        outputStream?.write(data, off, length)
    }

    @Synchronized
    open fun send(data: String) {
        if (!TextUtils.isEmpty(data)) {
            try {
                if (!send(data.toByteArray(charset("utf-8")))) {
                    Log.i(TAG,"发送失败")
                    localClientListener.onDisconnect(this)
                    stop()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     * 启动客户端连接监听
     */
    fun start() {
        runFlag = true
        val pool = Executors.newSingleThreadExecutor()
        pool.execute(this)
    }
    fun killClient(){
        localClientListener.onDisconnect(this)
        stop()
    }
    fun isConnected() : Boolean{
        return localSocket.isConnected
    }

    /**
     * 断开连接(主动)
     *
     *
     * 连接断开后，会回调`onDisconnect()`
     */
    private fun stop() {
        if (!runFlag) return
        runFlag = false
        if (localSocket.isConnected) {
            try {
                localSocket.shutdownInput()
                localSocket.shutdownOutput()
                outputStream?.close()
                inputStream?.close()
                localSocket.close() // 关闭socket
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                outputStream = null
                inputStream = null
            }
        }
    }

}

