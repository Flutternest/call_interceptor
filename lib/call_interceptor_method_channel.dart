import 'dart:ui';

import 'package:call_interceptor/src/enums.dart';
import 'package:call_interceptor/src/types.dart';
import 'package:call_interceptor/src/utils.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'call_interceptor_platform_interface.dart';

// This is the entrypoint for the background isolate. Since we can only enter
// an isolate once, we setup a MethodChannel to listen for method invocations
// from the native portion of the plugin. This allows for the plugin to perform
// any necessary processing in Dart (e.g., populating a custom object) before
// invoking the provided callback.
@pragma('vm:entry-point')
void _callInterceptorCallbackDispatcher() {
  // Initialize state necessary for MethodChannels.
  WidgetsFlutterBinding.ensureInitialized();

  const MethodChannel channel = MethodChannel(
    'plugins.iamsahilsonawane.call_interceptor/call_interceptor_background',
  );

  // This is where we handle background events from the native portion of the plugin.
  channel.setMethodCallHandler((MethodCall call) async {
    if (call.method == 'CallBackground#onCall') {
      final CallbackHandle handle =
          CallbackHandle.fromRawHandle(call.arguments['userCallbackHandle']);

      // PluginUtilities.getCallbackFromHandle performs a lookup based on the
      // callback handle and returns a tear-off of the original callback.
      final closure = PluginUtilities.getCallbackFromHandle(handle)!
          as Future<void> Function(CallType type);

      try {
        await closure(Utils.getCallTypeFromInt(call.arguments['type']));
      } catch (e) {
        // ignore: avoid_print
        print(
            'Call Interceptor: An error occurred in your background call handler:');
        // ignore: avoid_print
        print(e);
      }
    } else {
      throw UnimplementedError('${call.method} has not been implemented');
    }
  });

  // Once we've finished initializing, let the native portion of the plugin
  // know that it can start scheduling alarms.
  channel.invokeMethod<void>('CallInterceptorBackground#initialized');
}

/// An implementation of [CallInterceptorPlatform] that uses method channels.
class MethodChannelCallInterceptor extends CallInterceptorPlatform {
  MethodChannelCallInterceptor() : super();

  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel(
      'plugins.iamsahilsonawane.call_interceptor/call_interceptor');

  static bool _bgHandlerInitialized = false;

  @override
  Future<void> registerBackgroundCallHandler(
      BackgroundCallHandler handler) async {
    if (defaultTargetPlatform != TargetPlatform.android) {
      return;
    }

    if (!_bgHandlerInitialized) {
      _bgHandlerInitialized = true;
      final CallbackHandle bgHandle = PluginUtilities.getCallbackHandle(
          _callInterceptorCallbackDispatcher)!;
      final CallbackHandle userHandle =
          PluginUtilities.getCallbackHandle(handler)!;
      await methodChannel.invokeMapMethod('call_interceptor#initialize', {
        'pluginCallbackHandle': bgHandle.toRawHandle(),
        'userCallbackHandle': userHandle.toRawHandle(),
      });
    }
  }

  Future<void> sendTestCall() async {
    await methodChannel.invokeMethod('call_interceptor#sendTestIntent');
  }

  @override
  Future<bool> get isCallInterceptorRunning async {
    return await methodChannel
        .invokeMethod('call_interceptor#isCallInterceptorRunning');
  }

  @override
  Future<void> startCallInterceptor() async {
    await methodChannel.invokeMethod('call_interceptor#startCallInterceptor');
  }

  @override
  Future<void> stopCallInterceptor() async {
    await methodChannel.invokeMethod('call_interceptor#stopCallInterceptor');
  }
}
