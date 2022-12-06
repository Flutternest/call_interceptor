package com.iamsahilsonawane.call_interceptor

import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import java.util.*

/**
 * @Author Created by Sahil Sonawane (@iamsahilsonawane).
 * @Org: Flutternest (@flutternest)
 * @Date: 01/12/22
 *
 * Package: com.iamsahilsonawane.call_interceptor
 */
class CallTypeHandler {
    fun getCallTypeFromIntent(context: Context, intent: Intent): Int {
        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        return if (intent.action == "com.iamsahilsonawane.call_interceptor.EXECUTE_RECEIVER") {
            CallInterceptorUtils.TYPE_TEST_CALL
        } else {
            val stateStr = intent.extras!!.getString(TelephonyManager.EXTRA_STATE)

            var state = 0
            if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
                state = TelephonyManager.CALL_STATE_IDLE
            } else if (stateStr == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                state = TelephonyManager.CALL_STATE_OFFHOOK
            } else if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
                state = TelephonyManager.CALL_STATE_RINGING
            }
            onCallStateChanged(context, state)
        }
    }

    //Derived classes should override these to respond to specific events of interest
    private fun onIncomingCallStarted(ctx: Context?, start: Date?) {}
    private fun onOutgoingCallStarted(ctx: Context?, start: Date?) {}
    private fun onIncomingCallEnded(ctx: Context?, start: Date?, end: Date?) {}
    private fun onOutgoingCallEnded(ctx: Context?, start: Date?, end: Date?) {}
    private fun onMissedCall(ctx: Context?, start: Date?) {}

    //Deals with actual events
    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    private fun onCallStateChanged(context: Context?, state: Int): Int {
        var type: Int = -2
        if (lastState == state) {
            return type
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                onIncomingCallStarted(context, callStartTime)
                type = CallInterceptorUtils.TYPE_INCOMING_CALL
            }
            TelephonyManager.CALL_STATE_OFFHOOK ->
                //Transition of ringing->off-hook are pickups of incoming calls.
                //Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()
                    onOutgoingCallStarted(context, callStartTime)
                    type = CallInterceptorUtils.TYPE_OUTGOING_CALL
                }
            TelephonyManager.CALL_STATE_IDLE ->
                //Went to idle-  this is the end of a call.
                //What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    onMissedCall(context, callStartTime)
                    type = CallInterceptorUtils.TYPE_MISSED_CALL
                } else if (isIncoming) {
                    onIncomingCallEnded(context, callStartTime, Date())
                    type = CallInterceptorUtils.TYPE_INCOMING_ENDED_CALL
                } else {
                    onOutgoingCallEnded(context, callStartTime, Date())
                    type = CallInterceptorUtils.TYPE_OUTGOING_ENDED_CALL
                }
        }
        lastState = state
        return type
    }

    companion object {
        //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Date? = null
        private var isIncoming = false
    }


}