import 'package:call_interceptor/call_interceptor_method_channel.dart';
import 'package:call_interceptor/src/types.dart';

import 'call_interceptor_platform_interface.dart';

export 'src/enums.dart';

class CallInterceptor {
  static CallInterceptorPlatform get _platform =>
      CallInterceptorPlatform.instance;

  /// Set a call handler function which is called even when the app is in the
  /// background or terminated.
  /// This provided handler must be a top-level function and cannot be
  /// anonymous otherwise an [ArgumentError] will be thrown.
  ///
  /// Currently just for Android.
  static void onBackgroundCall(BackgroundCallHandler handler) {
    CallInterceptorPlatform.onBackgroundCall = handler;
  }

  /// Returns true if the call interceptor is running.
  Future<bool> get isCallInterceptorRunning async {
    return _platform.isCallInterceptorRunning;
  }

  /// Starts the call interceptor. If the call interceptor is already running,
  /// this method does nothing.
  ///
  /// Allows [onBackgroundCall] execution on dart VM running on an isolate.
  Future<void> startCallInterceptor() async {
    return _platform.startCallInterceptor();
  }

  /// Stops the call interceptor, if it is running. Otherwise, does nothing.
  ///
  /// This does not stop the call interceptor from intercepting calls.
  /// Instead it stops the call interceptor from sending the call to the
  /// [onBackgroundCall] handler.
  Future<void> stopCallInterceptor() async {
    return _platform.stopCallInterceptor();
  }

  /// Send test call to test the plugin
  static Future<void> sendTestCall() async {
    await MethodChannelCallInterceptor().sendTestCall();
  }
}
