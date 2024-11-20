/*
 * Copyright (c) 2012-2024 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.dolphin.localsocket.utils

import android.util.Base64

object Base64Decoder {
    fun decodeBase64(base64EncodedData: String?): String {
        val decodedBytes: ByteArray = Base64.decode(base64EncodedData, Base64.DEFAULT)
        return String(decodedBytes)
    }
}