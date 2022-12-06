# Call interceptor plugin

A Flutter plugin to run dart code when phone call events occur, even if the app is in background or terminated.

Developed by [Flutter nest](https://github.com/flutternest) 💙

## Current supported events ⚡️

✅ Missed call

✅ Incoming call

✅ Outgoing call

✅ Incoming call ended 

✅ Outgoing call ended

## Platform Support 

| Android | 
| :-----: |
|   ✔️    |

## Usage 🚀

To use this plugin, add `call_interceptor` as a dependency in your pubspec.yaml file.

```dart
import 'package:call_interceptor/call_interceptor.dart';
```

Register a callback for receiving phone call events.

```dart
@pragma('vm:entry-point')
Future<void> _callInterceptorBackgroundHandler(CallType type) async {
  print('Handling a background call | Type: $type');
}
```
Things to make sure here,

1. Callback SHOULD be annotated with `@pragma('vm:entry-point')` to ensure that tree-shaking doesn't remove the code since it would be invoked on the native side. 
See [here](https://github.com/dart-lang/sdk/blob/master/runtime/docs/compiler/aot/entry_point_pragma.md) for official documentation on the annotation.

2. Callback SHOULD NOT be an anonymous function.

3. Callback SHOULD be a top level function.

4. All initialization should be done in this callback. For e.g. if using firebase services, `Firebase.initializeApp(...)` should be called. 

Using any of the provided enums for phone call event types.
```dart
enum CallType {
  /// Missed call - when an incoming call is not answered
  missedCall,

  /// Incoming Call Ended - when an incoming call is answered and then ended
  incomingCallEnded,

  /// Outgoing Call Ended - when an outgoing call is answered/unanswered and then ended
  outgoingCallEnded,

  /// Outgoing Call - when an outgoing call is made
  outgoingCall,

  /// Incoming Call - when an incoming call is received (ringing)
  incomingCall,
}
```

## Thanks
This project is heavily derived and inspired by Firebase SDK for Flutter. Special shoutout and thanks for Flutter Fire 💙.
