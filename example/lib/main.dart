import 'package:call_interceptor/call_interceptor.dart';
import 'package:flutter/material.dart';

@pragma('vm:entry-point')
Future<void> _callInterceptorBackgroundHandler(CallType type) async {
  print('Handling a background call | Type: $type');
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  CallInterceptor.onBackgroundCall(_callInterceptorBackgroundHandler);

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(home: HomePage());
  }
}

class HomePage extends StatelessWidget {
  HomePage({super.key});

  final CallInterceptor callInterceptor = CallInterceptor();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Call Interceptor Example'),
      ),
      body: Center(
        child: Column(
          children: [
            TextButton(
              onPressed: () async {
                final scaffoldMessenger = ScaffoldMessenger.of(context);
                final isRunning =
                    await callInterceptor.isCallInterceptorRunning;
                if (isRunning) {
                  scaffoldMessenger.showSnackBar(const SnackBar(
                      content: Text("Call Interceptor is running")));
                } else {
                  scaffoldMessenger.showSnackBar(const SnackBar(
                      content: Text("Call Interceptor is not running")));
                }
              },
              child: const Text("Check if service is running"),
            ),
            TextButton(
              onPressed: () {
                final scaffoldMessenger = ScaffoldMessenger.of(context);

                callInterceptor.startCallInterceptor().catchError((e) {
                  debugPrint("Error (startCallInterceptor): $e");
                }).then(
                  (_) => scaffoldMessenger.showSnackBar(
                    const SnackBar(
                      content: Text("Started call interceptor sucessfully"),
                    ),
                  ),
                );
              },
              child: const Text("Start Service"),
            ),
            TextButton(
              onPressed: () {
                final scaffoldMessenger = ScaffoldMessenger.of(context);

                callInterceptor.stopCallInterceptor().catchError((e) {
                  debugPrint("Error (stopCallInterceptor): $e");
                }).then(
                  (_) => scaffoldMessenger.showSnackBar(
                    const SnackBar(
                      content: Text("Stopped call interceptor sucessfully"),
                    ),
                  ),
                );
              },
              child: const Text("Stop Service"),
            ),
          ],
        ),
      ),
    );
  }
}
