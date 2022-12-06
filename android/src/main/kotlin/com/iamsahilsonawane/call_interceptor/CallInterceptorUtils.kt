package com.iamsahilsonawane.call_interceptor

/**
 * @Author Created by Sahil Sonawane (@iamsahilsonawane).
 * @Org: Flutternest (@flutternest)
 * @Date: 01/12/22
 *
 * Package: com.iamsahilsonawane.call_interceptor
 */
class CallInterceptorUtils {
    companion object {
        const val SHARED_PREFERENCES_KEY = "plugins.iamsahilsonawane.call_interceptor.prefs"
        const val EXTRA_CALL_TYPE = "plugins.iamsahilsonawane.call_interceptor.EXTRA_CALL_TYPE"

        const val TYPE_TEST_CALL = -1
        const val TYPE_MISSED_CALL = 1
        const val TYPE_INCOMING_ENDED_CALL = 2
        const val TYPE_OUTGOING_ENDED_CALL = 3
        const val TYPE_OUTGOING_CALL = 4
        const val TYPE_INCOMING_CALL = 5

        const val JOB_ID = 2020

        const val IS_INTERCEPTOR_RUNNING_KEY = "IS_INTERCEPTOR_RUNNING"
    }

}