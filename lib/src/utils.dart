import 'package:call_interceptor/src/enums.dart';

class Utils {
  static CallType getCallTypeFromInt(int type) {
    switch (type) {
      case 1:
        return CallType.missedCall;
      case 2:
        return CallType.incomingCallEnded;
      case 3:
        return CallType.outgoingCallEnded;
      case 4:
        return CallType.outgoingCall;
      case 5:
        return CallType.incomingCall;
      default:
        return CallType.testing;
    }
  }
}
