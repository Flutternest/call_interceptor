import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:call_interceptor/call_interceptor_method_channel.dart';

void main() {
  MethodChannelCallInterceptor platform = MethodChannelCallInterceptor();
  const MethodChannel channel = MethodChannel('call_interceptor');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      if (methodCall.method == 'call_interceptor#isCallInterceptorRunning') {
        return true;
      }
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });
}
