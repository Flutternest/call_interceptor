package com.iamsahilsonawane.call_interceptor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.annotation.NonNull
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import io.flutter.embedding.engine.FlutterShellArgs
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/**
 * @Author Created by Sahil Sonawane (@iamsahilsonawane).
 * @Org: Flutternest (@flutternest)
 * @Date: 01/12/22
 *
 * Package: com.iamsahilsonawane.call_interceptor
 */
class CallInterceptorPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    val DEFAULT_ERROR_CODE = "call_interceptor"
    private val METHOD_CHANNEL_NAME = "plugins.iamsahilsonawane.call_interceptor/call_interceptor"
    private var mainActivity: Activity? = null


    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, METHOD_CHANNEL_NAME)
        channel.setMethodCallHandler(this)
    }


    override fun onMethodCall(call: MethodCall, result: Result) {
        val methodCallTask: Task<*>
        when (call.method) {
            "call_interceptor#initialize" -> {
                val arguments = call.arguments as Map<*, *>

                var pluginCallbackHandle: Long = 0
                var userCallbackHandle: Long = 0

                val arg1 = arguments["pluginCallbackHandle"]
                val arg2 = arguments["userCallbackHandle"]

                pluginCallbackHandle = if (arg1 is Long) {
                    arg1
                } else {
                    java.lang.Long.valueOf((arg2 as Int).toLong())
                }

                userCallbackHandle = if (arg2 is Long) {
                    arg2
                } else {
                    java.lang.Long.valueOf((arg2 as Int).toLong())
                }

                var shellArgs: FlutterShellArgs? = null
                if (mainActivity != null) {
                    // Supports both Flutter Activity types:
                    //    io.flutter.embedding.android.FlutterFragmentActivity
                    //    io.flutter.embedding.android.FlutterActivity
                    // We could use `getFlutterShellArgs()` but this is only available on `FlutterActivity`.
                    shellArgs = FlutterShellArgs.fromIntent(mainActivity!!.intent)
                }

                CallInterceptorBackgroundService.setCallbackDispatcher(pluginCallbackHandle)
                CallInterceptorBackgroundService.setUserCallbackHandle(userCallbackHandle)
                CallInterceptorBackgroundService.startBackgroundIsolate(
                    pluginCallbackHandle, shellArgs
                )
                methodCallTask = Tasks.forResult(null)
            }
            "call_interceptor#sendTestIntent" -> {
                val intent = Intent(ContextHolder.applicationContext, CallReceiver::class.java)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.action = "com.iamsahilsonawane.call_interceptor.EXECUTE_RECEIVER"
                ContextHolder.applicationContext!!.sendBroadcast(intent)
                methodCallTask = Tasks.forResult(null)
            }
            "call_interceptor#isCallInterceptorRunning" -> {
                methodCallTask = Tasks.forResult(isCallInterceptorRunning)
            }
            "call_interceptor#startCallInterceptor" -> {
                setIsCallInterceptorRunning(true)
                methodCallTask = Tasks.forResult(null)
            }
            "call_interceptor#stopCallInterceptor" -> {
                setIsCallInterceptorRunning(false)
                methodCallTask = Tasks.forResult(null)
            }

            else -> {
                result.notImplemented()
                val taskCompletionSource = TaskCompletionSource<Map<String, Int>>()
                taskCompletionSource.setException(Exception("Method not implemented"))
                methodCallTask = taskCompletionSource.task
            }
        }

        methodCallTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                result.success(task.result)
            } else {
                val exception = task.exception
                result.error(
                    DEFAULT_ERROR_CODE,
                    exception?.message,
                    getExceptionDetails(exception)
                )
            }
        }
    }

    private val isCallInterceptorRunning: Boolean
        get() {
            val prefs: SharedPreferences = ContextHolder.applicationContext!!
                .getSharedPreferences(CallInterceptorUtils.SHARED_PREFERENCES_KEY, 0)
            return prefs.getBoolean(CallInterceptorUtils.IS_INTERCEPTOR_RUNNING_KEY, true)
        }

    private fun setIsCallInterceptorRunning(isRunning: Boolean) {
        val context: Context = ContextHolder.applicationContext!!
        val prefs = context.getSharedPreferences(
            CallInterceptorUtils.SHARED_PREFERENCES_KEY,
            0
        )
        prefs.edit().putBoolean(CallInterceptorUtils.IS_INTERCEPTOR_RUNNING_KEY, isRunning).apply()
    }


    private fun getExceptionDetails(exception: Exception?): Map<String, Any?> {
        val details: MutableMap<String, Any?> = HashMap()
        details["code"] = "unknown"
        if (exception != null) {
            details["message"] = exception.message
        } else {
            details["message"] = "An unknown error has occurred."
        }
        return details
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.mainActivity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        this.mainActivity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        this.mainActivity = binding.activity
    }

    override fun onDetachedFromActivity() {
        this.mainActivity = null
    }
}
