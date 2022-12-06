import 'package:call_interceptor/call_interceptor.dart';
import 'package:call_interceptor/src/types.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:call_interceptor/call_interceptor_platform_interface.dart';
import 'package:call_interceptor/call_interceptor_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockCallInterceptorPlatform
    with MockPlatformInterfaceMixin
    implements CallInterceptorPlatform {
  @override
  void registerBackgroundCallHandler(BackgroundCallHandler handler) {}

  @override
  Future<bool> get isCallInterceptorRunning async {
    return Future.value(true);
  }

  @override
  Future<void> startCallInterceptor() async {}

  @override
  Future<void> stopCallInterceptor() async {}
}

void main() {
  final CallInterceptorPlatform initialPlatform =
      CallInterceptorPlatform.instance;

  test('$MethodChannelCallInterceptor is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelCallInterceptor>());
  });

  test('isCallInterceptRunning test', () async {
    CallInterceptor callInterceptorPlugin = CallInterceptor();
    MockCallInterceptorPlatform fakePlatform = MockCallInterceptorPlatform();
    CallInterceptorPlatform.instance = fakePlatform;

    expect(await callInterceptorPlugin.isCallInterceptorRunning, true);
  });
}
