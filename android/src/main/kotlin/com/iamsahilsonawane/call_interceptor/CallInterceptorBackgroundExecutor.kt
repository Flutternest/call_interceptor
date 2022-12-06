package com.iamsahilsonawane.call_interceptor

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterShellArgs
import io.flutter.embedding.engine.dart.DartExecutor.DartCallback
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.view.FlutterCallbackInformation
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean


/**
 * @Author Created by Sahil Sonawane (@iamsahilsonawane).
 * @Org: Flutternest (@flutternest)
 * @Date: 01/12/22
 *
 * Package: com.iamsahilsonawane.call_interceptor
 */

/**
 * An background execution abstraction which handles initializing a background isolate running a
 * callback dispatcher, used to invoke Dart callbacks while backgrounded.
 */
class CallInterceptorBackgroundExecutor : MethodCallHandler {
    private val isCallbackDispatcherReady = AtomicBoolean(false)

    /**
     * The [MethodChannel] that connects the Android side of this plugin with the background
     * Dart isolate that was created by this plugin.
     */
    private var backgroundChannel: MethodChannel? = null
    private var backgroundFlutterEngine: FlutterEngine? = null

    /**
     * Returns true when the background isolate has started and is ready to handle background
     * messages.
     */
    val isNotRunning: Boolean
        get() = !isCallbackDispatcherReady.get()

    private fun onInitialized() {
        isCallbackDispatcherReady.set(true)
        CallInterceptorBackgroundService.onInitialized()
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        val method = call.method
        try {
            if (method == "CallInterceptorBackground#initialized") {
                // This message is sent by the background method channel as soon as the background isolate
                // is running. From this point forward, the Android side of this plugin can send
                // callback handles through the background method channel, and the Dart side will execute
                // the Dart methods corresponding to those callback handles.
                onInitialized()
                result.success(true)
            } else {
                result.notImplemented()
            }
        } catch (e: Error) {
            result.error(
                "error",
                "Call Interceptor Error (CallInterceptorBackground#initialized): " + e.message,
                null
            )
        }
    }

    /**
     * Starts running a background Dart isolate within a new [FlutterEngine] using a previously
     * used entrypoint.
     *
     *
     * The isolate is configured as follows:
     *
     *
     *  * Bundle Path: `io.flutter.view.FlutterMain.findAppBundlePath(context)`.
     *  * Entrypoint: The Dart method used the last time this plugin was initialized in the
     * foreground.
     *  * Run args: none.
     *
     *
     *
     * Preconditions:
     *
     *
     *  * The given callback must correspond to a registered Dart callback. If the handle does not
     * resolve to a Dart callback then this method does nothing.
     *
     */
    fun startBackgroundIsolate() {
        if (isNotRunning) {
            val callbackHandle = pluginCallbackHandle
            if (callbackHandle != 0L) {
                startBackgroundIsolate(callbackHandle, null)
            }
        }
    }

    /**
     * Starts running a background Dart isolate within a new [FlutterEngine].
     *
     *
     * The isolate is configured as follows:
     *
     *
     *  * Bundle Path: `io.flutter.view.FlutterMain.findAppBundlePath(context)`.
     *  * Entrypoint: The Dart method represented by `callbackHandle`.
     *  * Run args: none.
     *
     *
     *
     * Preconditions:
     *
     *
     *  * The given `callbackHandle` must correspond to a registered Dart callback. If the
     * handle does not resolve to a Dart callback then this method does nothing.
     *
     */
    fun startBackgroundIsolate(callbackHandle: Long, shellArgs: FlutterShellArgs?) {
        if (backgroundFlutterEngine != null) {
            Log.e(TAG, "Background isolate already started.")
            return
        }
        val loader = FlutterLoader()
        val mainHandler = Handler(Looper.getMainLooper())
        val myRunnable = Runnable {
            loader.startInitialization(ContextHolder.applicationContext!!)
            loader.ensureInitializationCompleteAsync(
                ContextHolder.applicationContext!!,
                null,
                mainHandler
            ) {
                val appBundlePath = loader.findAppBundlePath()
                val assets: AssetManager = ContextHolder.applicationContext!!.assets
                if (isNotRunning) {
                    backgroundFlutterEngine = if (shellArgs != null) {
                        Log.i(
                            TAG, "Creating background FlutterEngine instance, with args: "
                                    + shellArgs.toArray().contentToString()
                        )
                        FlutterEngine(
                            ContextHolder.applicationContext!!, shellArgs.toArray()
                        )
                    } else {
                        Log.i(
                            TAG,
                            "Creating background FlutterEngine instance."
                        )
                        FlutterEngine(ContextHolder.applicationContext!!)
                    }
                    // We need to create an instance of `FlutterEngine` before looking up the
                    // callback. If we don't, the callback cache won't be initialized and the
                    // lookup will fail.
                    val flutterCallback =
                        FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
                    val executor = backgroundFlutterEngine!!.dartExecutor
                    initializeMethodChannel(executor)
                    val dartCallback =
                        DartCallback(assets, appBundlePath, flutterCallback)
                    executor.executeDartCallback(dartCallback)
                }
            }
        }
        mainHandler.post(myRunnable)
    }

    val isDartBackgroundHandlerRegistered: Boolean
        get() = pluginCallbackHandle != 0L

    /**
     * Executes the desired Dart callback in a background Dart isolate.
     *
     *
     * The given `intent` should contain a `long` extra called "callbackHandle", which
     * corresponds to a callback registered with the Dart VM.
     */
    fun executeDartCallbackInBackgroundIsolate(intent: Intent, latch: CountDownLatch?) {
        if (backgroundFlutterEngine == null) {
            Log.i(
                TAG,
                "A background message could not be handled in Dart as no onBackgroundMessage handler has been registered."
            )
            return
        }
        var result: MethodChannel.Result? = null
        if (latch != null) {
            result = object : MethodChannel.Result {
                override fun success(result: Any?) {
                    // If another thread is waiting, then wake that thread when the callback returns a result.
                    latch.countDown()
                }

                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                    latch.countDown()
                }

                override fun notImplemented() {
                    latch.countDown()
                }
            }
        }

        val type: Int =
            intent.getIntExtra(CallInterceptorUtils.EXTRA_CALL_TYPE, -1)
        // Handle the message event in Dart.
        backgroundChannel!!.invokeMethod(
            "CallBackground#onCall",
            object : HashMap<String?, Any?>() {
                init {
                    put("userCallbackHandle", userCallbackHandle)
                    put("type", type)
                }
            },
            result
        )
    }

    /**
     * Get the users registered Dart callback handle for background messaging. Returns 0 if not set.
     */
    private val userCallbackHandle: Long
        private get() {
            val prefs: SharedPreferences = ContextHolder.applicationContext!!
                .getSharedPreferences(CallInterceptorUtils.SHARED_PREFERENCES_KEY, 0)
            return prefs.getLong(USER_CALLBACK_HANDLE_KEY, 0)
        }

    /** Get the registered Dart callback handle for the messaging plugin. Returns 0 if not set.  */
    private val pluginCallbackHandle: Long
        private get() {
            val prefs: SharedPreferences = ContextHolder.applicationContext!!
                .getSharedPreferences(CallInterceptorUtils.SHARED_PREFERENCES_KEY, 0)
            return prefs.getLong(CALLBACK_HANDLE_KEY, 0)
        }

    private fun initializeMethodChannel(isolate: BinaryMessenger) {
        // backgroundChannel is the channel responsible for receiving the following messages from
        // the background isolate that was setup by this plugin method call:
        // - "FirebaseBackgroundMessaging#initialized"
        //
        // This channel is also responsible for sending requests from Android to Dart to execute Dart
        // callbacks in the background isolate.
        backgroundChannel =
            MethodChannel(
                isolate,
                "plugins.iamsahilsonawane.call_interceptor/call_interceptor_background"
            )
        backgroundChannel!!.setMethodCallHandler(this)
    }

    companion object {
        private const val TAG = "CIBGExecutor"
        private const val CALLBACK_HANDLE_KEY = "callback_handle"
        private const val USER_CALLBACK_HANDLE_KEY = "user_callback_handle"

        /**
         * Sets the Dart callback handle for the Dart method that is responsible for initializing the
         * background Dart isolate, preparing it to receive Dart callback tasks requests.
         */
        fun setCallbackDispatcher(callbackHandle: Long) {
            val context: Context = ContextHolder.applicationContext!!
            val prefs = context.getSharedPreferences(
                CallInterceptorUtils.SHARED_PREFERENCES_KEY,
                0
            )
            prefs.edit().putLong(CALLBACK_HANDLE_KEY, callbackHandle).apply()
        }

        /**
         * Sets the Dart callback handle for the users Dart handler that is responsible for handling
         * messaging events in the background.
         */
        fun setUserCallbackHandle(callbackHandle: Long) {
            val context: Context = ContextHolder.applicationContext!!
            val prefs = context.getSharedPreferences(
                CallInterceptorUtils.SHARED_PREFERENCES_KEY,
                0
            )
            prefs.edit().putLong(USER_CALLBACK_HANDLE_KEY, callbackHandle).apply()
        }
    }
}
