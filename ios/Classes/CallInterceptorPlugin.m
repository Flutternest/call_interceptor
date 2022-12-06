#import "CallInterceptorPlugin.h"
#if __has_include(<call_interceptor/call_interceptor-Swift.h>)
#import <call_interceptor/call_interceptor-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "call_interceptor-Swift.h"
#endif

@implementation CallInterceptorPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftCallInterceptorPlugin registerWithRegistrar:registrar];
}
@end
