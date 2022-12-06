enum CallType {
  /// Testing. Ignore
  testing,

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
