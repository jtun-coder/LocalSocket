package com.dolphin.localsocket.client

import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.text.TextUtils
import android.util.Log
import com.dolphin.localsocket.Const
import com.dolphin.localsocket.LocalDataType
import com.dolphin.localsocket.cmd.BaseLocalCmd
import com.dolphin.localsocket.cmd.LocalCmd
import com.dolphin.localsocket.listener.LocalClientListener
import com.dolphin.localsocket.utils.Utils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.concurrent.Executors


/**
 * 本地socket连接客户端
 */
class LocalClient private constructor() {
    private var runFlag = false
    private val TAG = "LocalSocket"
    private var filePath: String? = null
    private var clientType = 0
    private var localClientListener : LocalClientListener? = null
    private var socketClient:LocalSocket? = null

    companion object {
        @JvmStatic
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            LocalClient()
        }
    }

    //生成LocketServer 监听
    fun init(clientType: Int,name :String = Const.CONNECT_TAG) {
        if (runFlag)
            return
        this.clientType = clientType
        runFlag = true
        val pool = Executors.newSingleThreadExecutor()
        val localSocketAddress = LocalSocketAddress(name)
        pool.execute {
            while (runFlag){
                try {
                    val localSocket = LocalSocket()
                    socketClient = localSocket
                    localSocket.connect(localSocketAddress)//连接服务器
                    if (isConnected(localSocket)) {
                        localClientListener?.onConnect(this)
//                    //发送当前模式信息到监控端
//                    send(
//                        localSocket.outputStream,
//                        BaseLocalCmd<Any?>(LocalCmd.MODEL, value = clientType).toString()
//                    )
                        //连接成功
                        Log.i(TAG, "连接成功")
                        receiveData(localSocket)
                    } else {
                        //连接失败
                        Log.i(TAG, "连接失败")
                    }
                } catch (e: Exception) {
                    // 连接出错
                    e.printStackTrace()
                }finally {
                    Log.i(TAG, "stopLocalSocket")
                    stopLocalSocket(socketClient)
                }
                Thread.sleep(5000)
            }
        }
    }

    private fun stopLocalSocket(localSocket: LocalSocket?){
        try {
            localSocket?.shutdownOutput()
            localSocket?.shutdownInput()
            localSocket?.close()
            localClientListener?.onDisconnect(this)
        }catch (e:Exception){}

    }

    private fun receiveData(socket: LocalSocket) {
        while (runFlag && isConnected(socket)) {
            try {
                /**得到的是16进制数，需要进行解析 */
                val bt = ByteArray(10)
                //                获取接收到的字节和字节数
                val length: Int = socket.inputStream.read(bt)
                Log.i(TAG, "length : $length")
                if (length == -1) {
                    break
                }
                if (length == 10) {
                    val lenBytes = ByteArray(8)
                    System.arraycopy(bt, 2, lenBytes, 0, 8)
                    val len: Long = Utils.bytesToLong(lenBytes)
                    //解析类型
                    when (bt[0]) {
                        LocalDataType.COMMAND -> receiveMsg(socket, len)
                        LocalDataType.FILE_SEND -> receiveFile(socket, len)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // 连接被断开(被动)
            }
        }
    }

    @Throws(IOException::class)
    private fun receiveMsg(socket: LocalSocket, receiveLen: Long) {
        /**得到的是16进制数，需要进行解析 */
        val str: String? = receiveStr(socket, receiveLen.toInt())
        Log.i(TAG, "接收成功：$str")
        localClientListener?.onReceive(this,str)
    }

    @Throws(IOException::class)
    private fun receiveStr(socket: LocalSocket, receiveLen: Int): String? {
        /**得到的是16进制数，需要进行解析 */
        var receiveLen = receiveLen
        val bt = ByteArray(receiveLen)
        //获取接收到的字节和字节数
        var readBt: ByteArray? = null
        readBt = if (receiveLen >= 1024) {
            ByteArray(1024)
        } else {
            ByteArray(receiveLen)
        }
        var length = 0
        while (receiveLen > 0 && socket.inputStream?.read(readBt).also { length = it!! } != -1) {
            System.arraycopy(readBt, 0, bt, (bt.size - receiveLen), length)
            receiveLen -= length
            readBt = if (receiveLen >= 1024) {
                ByteArray(1024)
            } else {
                ByteArray(receiveLen.toInt())
            }
        }
        return String(bt, Charset.forName("UTF-8"))
    }

    @Throws(IOException::class)
    private fun receiveFile(socket: LocalSocket, receiveLen: Long) {
        //获取名称长度 int 四字节
        var receiveLen = receiveLen
        val nameLengthBt = ByteArray(4)
        socket.inputStream.read(nameLengthBt)
        val nameLength = Utils.byte2int(nameLengthBt)
        //获取文件名
        val fileName = receiveStr(socket, nameLength)
        Log.i(TAG, "fileName : $fileName")
        //创建一个文件,指定保存路径和刚才传输过来的文件名
        val file = File(filePath, fileName)
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        val saveFile: OutputStream = FileOutputStream(file)
        var buf: ByteArray? = null
        buf = if (receiveLen >= 1024) {
            ByteArray(1024)
        } else {
            ByteArray(receiveLen.toInt())
        }
        var len = 0
        while (receiveLen > 0 && socket.inputStream.read(buf).also { len = it } != -1) {
            Log.i(TAG, "length : $len")
            receiveLen -= len.toLong()
            saveFile.write(buf, 0, len)
            buf = if (receiveLen >= 1024) {
                ByteArray(1024)
            } else {
                ByteArray(receiveLen.toInt())
            }
        }
        saveFile.flush()
        saveFile.close()
        localClientListener?.onReceiveFile(this,file.absolutePath)
    }

    /**
     * 发送数据
     */
    fun send(data:String){
        val pool = Executors.newSingleThreadExecutor()
        pool.execute {
            socketClient?.let {
                send(it.outputStream,data)
            }
        }
    }
    @Synchronized
    private fun send(outputStream: OutputStream, data: String) {
        if (!TextUtils.isEmpty(data)) {
            try {
                if (!send(outputStream, data.toByteArray())) {
                    Log.i(TAG, "发送失败")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
    private fun send(outputStream: OutputStream, data: ByteArray): Boolean {
        try {
            val sendBytes = ByteArray(10)
            sendBytes[0] = LocalDataType.COMMAND
            sendBytes[1] = ((255 - LocalDataType.COMMAND).toByte())
            val sendLength: ByteArray = Utils.longToBytes(data.size.toLong())
            System.arraycopy(sendLength, 0, sendBytes, 2, 8)
            writeData(outputStream, sendBytes, 0, sendBytes.size)
            writeData(outputStream, data, 0, data.size)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @Synchronized
    @Throws(IOException::class)
    open fun writeData(outputStream: OutputStream, data: ByteArray, off: Int, length: Int) {
        // 写入数据
        outputStream.write(data, off, length)
    }

    /**
     * 判断是否连接
     */
    private fun isConnected(socket: LocalSocket): Boolean {
        return socket.isConnected
    }
    fun setLocalClientListener(localClientListener: LocalClientListener){
        this.localClientListener = localClientListener
    }
    fun release(){
        runFlag = true
        stopLocalSocket(socketClient)
    }
}