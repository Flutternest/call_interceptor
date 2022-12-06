package com.iamsahilsonawane.call_interceptor

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import androidx.core.app.JobIntentService
import io.flutter.embedding.engine.FlutterShellArgs
import java.util.*
import java.util.concurrent.CountDownLatch


/**
 * @Author Created by Sahil Sonawane (@iamsahilsonawane).
 * @Org: Flutternest (@flutternest)
 * @Date: 01/12/22
 *
 * Package: com.iamsahilsonawane.call_interceptor
 */
class CallInterceptorBackgroundService : JobIntentService() {
    override fun onCreate() {
        super.onCreate()
        if (flutterBackgroundExecutor == null) {
            flutterBackgroundExecutor = CallInterceptorBackgroundExecutor()
        }
        flutterBackgroundExecutor!!.startBackgroundIsolate()
    }

    /**
     * Executes a Dart callback, as specified within the incoming `intent`.
     *
     *
     * Invoked by our [JobIntentService] superclass after a call to [ ][JobIntentService.enqueueWork].
     *
     *
     * If there are no pre-existing callback execution requests, other than the incoming `intent`, then the desired Dart callback is invoked immediately.
     *
     *
     * If there are any pre-existing callback requests that have yet to be executed, the incoming
     * `intent` is added to the [.messagingQueue] to be invoked later, after all
     * pre-existing callbacks have been executed.
     */
    override fun onHandleWork(intent: Intent) {
        if (!flutterBackgroundExecutor!!.isDartBackgroundHandlerRegistered) {
            Log.w(
                TAG,
                "A background call interception could not be handled in Dart as no onCallReceived handler has been registered."
            )
            return
        }

        // If we're in the middle of processing queued messages, add the incoming
        // intent to the queue and return.
        synchronized(messagingQueue) {
            if (flutterBackgroundExecutor!!.isNotRunning) {
                Log.i(
                    TAG,
                    "Service has not yet started, messages will be queued."
                )
                messagingQueue.add(intent)
                return
            }
        }

        // There were no pre-existing callback requests. Execute the callback
        // specified by the incoming intent.
        val latch = CountDownLatch(1)
        Handler(mainLooper)
            .post {
                flutterBackgroundExecutor!!.executeDartCallbackInBackgroundIsolate(
                    intent,
                    latch
                )
            }
        try {
            latch.await()
        } catch (ex: InterruptedException) {
            Log.i(TAG, "Exception waiting to execute Dart callback", ex)
        }
    }

    companion object {
        private const val TAG = "CIBGService"
        private val messagingQueue: MutableList<Intent> = Collections.synchronizedList(LinkedList())

        /** Background Dart execution context.  */
        private var flutterBackgroundExecutor: CallInterceptorBackgroundExecutor? = null

        /**
         * Schedule the message to be handled by the [CallInterceptorBackgroundService].
         */
        fun enqueueMessageProcessing(context: Context?, messageIntent: Intent) {
            enqueueWork(
                context!!,
                CallInterceptorBackgroundService::class.java,
                CallInterceptorUtils.JOB_ID,
                messageIntent,
            )
        }

        /**
         * Starts the background isolate for the [CallInterceptorBackgroundService].
         *
         *
         * Preconditions:
         *
         *
         *  * The given `callbackHandle` must correspond to a registered Dart callback. If the
         * handle does not resolve to a Dart callback then this method does nothing.
         *  * A static [.pluginRegistrantCallback] must exist, otherwise a [       ] will be thrown.
         *
         */
        fun startBackgroundIsolate(callbackHandle: Long, shellArgs: FlutterShellArgs?) {
            if (flutterBackgroundExecutor != null) {
                Log.w(TAG, "Attempted to start a duplicate background isolate. Returning...")
                return
            }
            flutterBackgroundExecutor = CallInterceptorBackgroundExecutor()
            flutterBackgroundExecutor!!.startBackgroundIsolate(callbackHandle, shellArgs)
        }

        /**
         * Called once the Dart isolate (`flutterBackgroundExecutor`) has finished initializing.
         *
         *
         * Invoked by [CallInterceptorPlugin] when it receives the `CallInterceptor.initialized` message. Processes all messaging events that came in while the
         * isolate was starting.
         */
        /* package */
        fun onInitialized() {
            Log.i(TAG, "CallInterceptorBackgroundService started!")
            synchronized(messagingQueue) {

                // Handle all the message events received before the Dart isolate was
                // initialized, then clear the queue.
                for (intent in messagingQueue) {
                    flutterBackgroundExecutor!!.executeDartCallbackInBackgroundIsolate(
                        intent,
                        null
                    )
                }
                messagingQueue.clear()
            }
        }

        /**
         * Sets the Dart callback handle for the Dart method that is responsible for initializing the
         * background Dart isolate, preparing it to receive Dart callback tasks requests.
         */
        fun setCallbackDispatcher(callbackHandle: Long) {
            CallInterceptorBackgroundExecutor.setCallbackDispatcher(callbackHandle)
        }

        /**
         * Sets the Dart callback handle for the users Dart handler that is responsible for handling
         * messaging events in the background.
         */
        fun setUserCallbackHandle(callbackHandle: Long) {
            CallInterceptorBackgroundExecutor.setUserCallbackHandle(callbackHandle)
        }
    }
}
