package com.iamsahilsonawane.call_interceptor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.iamsahilsonawane.call_interceptor.ContextHolder.applicationContext

/**
 * @Author Created by Sahil Sonawane (@iamsahilsonawane).
 * @Org: Flutternest (@flutternest)
 * @Date: 01/12/22
 *
 * Package: com.iamsahilsonawane.call_interceptor
 */
class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Broadcast received for call")

        if (applicationContext == null) {
            applicationContext = context.applicationContext
        }

        if (intent.action == "android.intent.action.PHONE_STATE" || intent.action == "com.iamsahilsonawane.call_interceptor.EXECUTE_RECEIVER" )  {
            if (!isCallInterceptorRunning) return

            val callType = CallTypeHandler().getCallTypeFromIntent(context, intent)
            if (callType == -2) {
                return //debounce
            }
            //  |-> ---------------------
            //    App in Background/Quit
            //   ------------------------
            val onBackgroundMessageIntent = Intent(
                context,
                CallInterceptorBackgroundService::class.java,
            )
            onBackgroundMessageIntent.putExtra(
                CallInterceptorUtils.EXTRA_CALL_TYPE,
                callType
            )
            CallInterceptorBackgroundService.enqueueMessageProcessing(
                context, onBackgroundMessageIntent
            )

        }
    }
    private val isCallInterceptorRunning: Boolean
        get() {
            val prefs: SharedPreferences = applicationContext!!
                .getSharedPreferences(CallInterceptorUtils.SHARED_PREFERENCES_KEY, 0)
            return prefs.getBoolean(CallInterceptorUtils.IS_INTERCEPTOR_RUNNING_KEY, true)
        }

    companion object {
        private const val TAG = "CICallReceiver"
    }
}