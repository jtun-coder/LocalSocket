package com.dolphin.localsocket.utils

import android.content.Context
import android.content.pm.PackageManager
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader

class Utils {
    companion object{
        fun int2byte(res: Int): ByteArray {
            val targets = ByteArray(4)
            targets[0] = (res and 0xff).toByte() // 最低位
            targets[1] = (res shr 8 and 0xff).toByte() // 次低位
            targets[2] = (res shr 16 and 0xff).toByte() // 次高位
            targets[3] = (res ushr 24).toByte() // 最高位,无符号右移。
            return targets
        }

        fun byte2int(res: ByteArray): Int {
            // 一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000
            return (res[0].toInt() and 0xff or (res[1].toInt() shl 8 and 0xff00) // | 表示安位或
                    or (res[2].toInt() shl 24 ushr 8) or (res[3].toInt() shl 24))
        }

        fun longToBytes(values: Long): ByteArray {
            val buffer = ByteArray(8)
            for (i in 0..7) {
                val offset = 64 - (i + 1) * 8
                buffer[i] = (values shr offset and 0xffL).toByte()
            }
            return buffer
        }

        fun bytesToLong(buffer: ByteArray): Long {
            var values: Long = 0
            for (i in 0..7) {
                values = values shl 8
                values = values or (buffer[i].toInt() and 0xff).toLong()
            }
            return values
        }

        /**
         * 字符串转换为BufferedReader
         * @param source
         * @return
         */
        fun stringToBufferedReader(source: String): BufferedReader {
            val byteArrayInputStream = ByteArrayInputStream(source.toByteArray())
            val inputStream: InputStream = byteArrayInputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            return reader
        }
        /**
         * 获取当前本地apk的版本
         *
         * @param mContext
         * @return
         */
        fun getVersionCode(mContext: Context): Int {
            var versionCode = 0
            try {
                versionCode = mContext.packageManager.getPackageInfo(mContext.packageName, 0).versionCode
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return versionCode
        }

        /**
         * 获取当前本地apk的版本
         *
         * @param mContext
         * @return
         */
        fun getVersionName(mContext: Context): String {
            var versionName = ""
            try {
                versionName = mContext.packageManager.getPackageInfo(mContext.packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return versionName
        }
    }

}