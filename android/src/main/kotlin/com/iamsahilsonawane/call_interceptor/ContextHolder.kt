package com.iamsahilsonawane.call_interceptor

import android.content.Context
import android.util.Log

/**
 * @Author Created by Sahil Sonawane (@iamsahilsonawane).
 * @Org: Flutternest (@flutternest)
 * @Date: 01/12/22
 *
 * Package: com.iamsahilsonawane.call_interceptor
 */
object ContextHolder {
    var applicationContext: Context? = null
        set(applicationContext) {
            Log.d("CIContextHolder", "received application context.")
            field = applicationContext
        }
}
