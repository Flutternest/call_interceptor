import 'package:call_interceptor/src/types.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'call_interceptor_method_channel.dart';

abstract class CallInterceptorPlatform extends PlatformInterface {
  /// Constructs a CallInterceptorPlatform.
  CallInterceptorPlatform() : super(token: _token);

  static final Object _token = Object();

  static CallInterceptorPlatform _instance = MethodChannelCallInterceptor();

  /// The default instance of [CallInterceptorPlatform] to use.
  ///
  /// Defaults to [MethodChannelCallInterceptor].
  static CallInterceptorPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [CallInterceptorPlatform] when
  /// they register themselves.
  static set instance(CallInterceptorPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  static BackgroundCallHandler? _onBackgroundCallHandler;

  /// Set a call handler function which is called when the app is in the
  /// background or terminated.
  ///
  /// This provided handler must be a top-level function and cannot be
  /// anonymous otherwise an [ArgumentError] will be thrown.
  static BackgroundCallHandler? get onBackgroundCall {
    return _onBackgroundCallHandler;
  }

  /// Allows the background call handler to be created and calls the
  /// instance delegate [registerBackgroundCallHandler] to perform any
  /// platform specific registration logic.
  ///
  /// Currently just for Android.
  static set onBackgroundCall(BackgroundCallHandler? handler) {
    _onBackgroundCallHandler = handler;

    if (handler != null) {
      instance.registerBackgroundCallHandler(handler);
    }
  }

  /// Allows delegates to create a background call handler implementation.
  ///
  /// For example, on native platforms this could be to setup an isolate
  void registerBackgroundCallHandler(BackgroundCallHandler handler) {
    throw UnimplementedError(
        'registerBackgroundCallHandler() is not implemented');
  }

  Future<bool> get isCallInterceptorRunning async {
    throw UnimplementedError(
        'isCallInterceptorRunning() has not been implemented.');
  }

  Future<void> startCallInterceptor() async {
    throw UnimplementedError(
        'startCallInterceptor() has not been implemented.');
  }

  Future<void> stopCallInterceptor() async {
    throw UnimplementedError('stopCallInterceptor() has not been implemented.');
  }
}
