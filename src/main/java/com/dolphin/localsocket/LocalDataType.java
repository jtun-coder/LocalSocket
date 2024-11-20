package com.dolphin.localsocket;

public class LocalDataType {
    /**
     * 命令
     */
    public static final byte END = 0x00;//结束
    public static final byte HEART = 0x01;//心跳

    public static final byte COMMAND = 0X10;//文本
    /**
     * 文件发送
     */
    public static final byte FILE_SEND = 0x21;//文件

}
