#import "BFCDVSDKBugfender.h"
#import <BugfenderSDK/BugfenderSDK.h>

@interface BFPendingObfuscation : NSObject
@property (nonatomic, strong) dispatch_semaphore_t semaphore;
@property (nonatomic, copy) NSDictionary *response;
@end

@implementation BFPendingObfuscation
@end

@interface BFCDVSDKBugfender ()
@property (nonatomic, strong) NSMutableDictionary<NSString *, BFPendingObfuscation *> *pendingObfuscations;
@end

@implementation BFCDVSDKBugfender

static BOOL BFPreferenceEnabled(NSString *value)
{
    if (value == nil || value.length == 0 || [value isEqualToString:@"unset"] || [value isEqualToString:@"false"]) {
        return NO;
    }
    return [value caseInsensitiveCompare:@"true"] == NSOrderedSame
        || [value caseInsensitiveCompare:@"YES"] == NSOrderedSame
        || [value isEqualToString:@"1"];
}

- (void)pluginInitialize
{
    self.pendingObfuscations = [NSMutableDictionary dictionary];

    NSString *hideDeviceName = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"BUGFENDER_HIDE_DEVICE_NAME"];
    if(![@"unset" isEqualToString:hideDeviceName]) {
        [Bugfender overrideDeviceName:@"Unknown"];
    }

    NSString *baseURL = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"BUGFENDER_BASE_URL"];
    if(![@"unset" isEqualToString:baseURL]) {
        [Bugfender setBaseURL:[NSURL URLWithString:baseURL]];
    }

    NSString *apiURL = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"BUGFENDER_API_URL"];
    if(![@"unset" isEqualToString:apiURL]) {
        [Bugfender setApiURL:[NSURL URLWithString:apiURL]];
    }

    NSString *key = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"BUGFENDER_APP_KEY"];
    if(key == nil) {
        NSLog(@"Please set BUGFENDER_APP_KEY in config.xml");
        return;
    }
    [Bugfender activateLogger:key];

    NSString *enabled = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"BUGFENDER_AUTOMATIC"];
    if(enabled == nil || [enabled isEqualToString:@"ALL"])
        enabled = @"UI,CRASH";

    NSArray* enables = [enabled componentsSeparatedByString:@","];
    if ([enables containsObject:@"UI"]) {
        [Bugfender enableUIEventLogging];
    }
    if ([enables containsObject:@"CRASH"]) {
        [Bugfender enableCrashReporting];
    }

    NSString *networkLogging = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"BUGFENDER_NETWORK_LOGGING"];
    if (BFPreferenceEnabled(networkLogging)) {
        [Bugfender setNetworkLoggingEnabled:YES];
    }

    NSString *captureBodies = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"BUGFENDER_NETWORK_CAPTURE_BODIES"];
    if (BFPreferenceEnabled(captureBodies)) {
        [Bugfender setNetworkLoggingCaptureBodies:YES];
    }

    NSString *captureErrorBodies = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"BUGFENDER_NETWORK_CAPTURE_ERROR_BODIES"];
    if (BFPreferenceEnabled(captureErrorBodies)) {
        [Bugfender setNetworkLoggingCaptureErrorResponseBodies:YES];
    }
}

- (void)forceSendOnce:(CDVInvokedUrlCommand*)command
{
    [Bugfender forceSendOnce];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)removeDeviceKey:(CDVInvokedUrlCommand*)command
{
    NSString* key = [command.arguments objectAtIndex:0];
    [Bugfender removeDeviceKey:key];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)sendIssue:(CDVInvokedUrlCommand*)command
{
    NSString* title = [command.arguments objectAtIndex:0];
    NSString* text = [command.arguments objectAtIndex:1];
    NSURL* url = [Bugfender sendIssueReturningUrlWithTitle:title text:text];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:url.absoluteString];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setDeviceKey:(CDVInvokedUrlCommand*)command
{
    NSString* key = [command.arguments objectAtIndex:0];
    id value = [command.arguments objectAtIndex:1];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    if ([value isKindOfClass:[NSString class]]) {
        [Bugfender setDeviceString:value forKey:key];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else if ([value isKindOfClass:[NSNumber class]]) {
        NSNumber* n = value;
        if (strcmp([n objCType], @encode(BOOL)) == 0) {
            [Bugfender setDeviceBOOL:[n boolValue] forKey:key];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        } else if (strcmp([n objCType], @encode(double)) == 0) {
            [Bugfender setDeviceDouble:[n doubleValue] forKey:key];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        } else if (strcmp([n objCType], @encode(UInt64)) == 0) {
            [Bugfender setDeviceInteger:[n unsignedLongLongValue] forKey:key];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setForceEnabled:(CDVInvokedUrlCommand*)command
{
    BOOL enabled = [[command.arguments objectAtIndex:0] boolValue];
    [Bugfender setForceEnabled:enabled];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setMaximumLocalStorageSize:(CDVInvokedUrlCommand*)command
{
    UInt64 size = [[command.arguments objectAtIndex:0] unsignedLongLongValue];
    [Bugfender setMaximumLocalStorageSize:size];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)log:(CDVInvokedUrlCommand*)command
{
    NSInteger lineNumber = 0;
    if([[command.arguments objectAtIndex:0] isKindOfClass:NSNumber.class])
        lineNumber = [[command.arguments objectAtIndex:0] unsignedLongLongValue];
    NSString* method = [command.arguments objectAtIndex:1];
    NSString* fileName = [command.arguments objectAtIndex:2];
    NSString* levelString = [command.arguments objectAtIndex:3];
    NSString* tag = [command.arguments objectAtIndex:4];
    NSString* message = [command.arguments objectAtIndex:5];
    
    BFLogLevel level = BFLogLevelDefault; // in the future will probably be BFLogLevelDebug to avoid misunderstandings
    if([levelString isEqualToString:@"fatal"]) {
        level = BFLogLevelFatal;
    } else if([levelString isEqualToString:@"error"]) {
        level = BFLogLevelError;
    } else if([levelString isEqualToString:@"warn"]) {
        level = BFLogLevelWarning;
    } else if([levelString isEqualToString:@"info"]) {
        level = BFLogLevelInfo;
    } else if([levelString isEqualToString:@"trace"]) {
        level = BFLogLevelTrace;
    }
    
    [Bugfender logWithLineNumber:lineNumber method:method file:fileName level:level tag:tag message:message];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getDeviceUrl:(CDVInvokedUrlCommand*)command
{
    NSURL* deviceURL = [Bugfender deviceIdentifierUrl];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:deviceURL.absoluteString];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getSessionUrl:(CDVInvokedUrlCommand*)command
{
    NSURL* sessionURL = [Bugfender sessionIdentifierUrl];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:sessionURL.absoluteString];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)showUserFeedbackUI:(CDVInvokedUrlCommand*)command
{
    NSString* title = [command.arguments objectAtIndex:0];
    NSString* hint = [command.arguments objectAtIndex:1];
    NSString* subjectHint = [command.arguments objectAtIndex:2];
    NSString* messageHint = [command.arguments objectAtIndex:3];
    NSString* sendButtonText = [command.arguments objectAtIndex:4];
    NSString* cancelButtonText = [command.arguments objectAtIndex:5];
    BFUserFeedbackNavigationController *nvc = [Bugfender userFeedbackViewControllerWithTitle:title
                                                                                        hint:hint
                                                                          subjectPlaceholder:subjectHint
                                                                          messagePlaceholder:messageHint
                                                                             sendButtonTitle:sendButtonText
                                                                           cancelButtonTitle:cancelButtonText
                                                                                  completion:^(BOOL feedbackSent, NSURL * _Nullable __strong url) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:feedbackSent];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
    [self.viewController presentViewController:nvc animated:YES completion:nil];
}

- (void)sendCrash:(CDVInvokedUrlCommand*)command
{
    NSString* title = [command.arguments objectAtIndex:0];
    NSString* text = [command.arguments objectAtIndex:1];
    NSURL* url = [Bugfender sendCrashWithTitle:title text:text];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:url.absoluteString];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)sendUserFeedback:(CDVInvokedUrlCommand*)command
{
    NSString* title = [command.arguments objectAtIndex:0];
    NSString* text = [command.arguments objectAtIndex:1];
    NSURL* url = [Bugfender sendUserFeedbackReturningUrlWithSubject:title message:text];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:url.absoluteString];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setNetworkLoggingEnabled:(CDVInvokedUrlCommand*)command
{
    BOOL enabled = [[command.arguments objectAtIndex:0] boolValue];
    [Bugfender setNetworkLoggingEnabled:enabled];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setNetworkLoggingCaptureBodies:(CDVInvokedUrlCommand*)command
{
    BOOL capture = [[command.arguments objectAtIndex:0] boolValue];
    [Bugfender setNetworkLoggingCaptureBodies:capture];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setNetworkLoggingCaptureErrorResponseBodies:(CDVInvokedUrlCommand*)command
{
    BOOL capture = [[command.arguments objectAtIndex:0] boolValue];
    [Bugfender setNetworkLoggingCaptureErrorResponseBodies:capture];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setNetworkLoggingURLFilter:(CDVInvokedUrlCommand*)command
{
    id allowlistValue = [command argumentAtIndex:0];
    id denylistValue = [command argumentAtIndex:1];
    NSArray *allowlist = [allowlistValue isKindOfClass:[NSArray class]] ? allowlistValue : nil;
    NSArray *denylist = [denylistValue isKindOfClass:[NSArray class]] ? denylistValue : nil;
    [Bugfender setNetworkLoggingURLFilterWithAllowlist:allowlist denylist:denylist];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setNetworkLoggingMaxRequestsPerMinute:(CDVInvokedUrlCommand*)command
{
    id countValue = [command argumentAtIndex:0];
    NSNumber *count = nil;
    if ([countValue isKindOfClass:[NSNumber class]] && countValue != [NSNull null]) {
        count = countValue;
    }
    [Bugfender setNetworkLoggingMaxRequestsPerMinute:count];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setNetworkLoggingRequestObfuscationHandlerEnabled:(CDVInvokedUrlCommand*)command
{
    BOOL enabled = [[command.arguments objectAtIndex:0] boolValue];
    if (enabled) {
        [self installRequestObfuscationHandler];
    } else {
        [Bugfender setNetworkLoggingRequestObfuscationHandler:nil];
    }

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setNetworkLoggingResponseObfuscationHandlerEnabled:(CDVInvokedUrlCommand*)command
{
    BOOL enabled = [[command.arguments objectAtIndex:0] boolValue];
    if (enabled) {
        [self installResponseObfuscationHandler];
    } else {
        [Bugfender setNetworkLoggingResponseObfuscationHandler:nil];
    }

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)completeNetworkObfuscation:(CDVInvokedUrlCommand*)command
{
    NSString *requestId = [command argumentAtIndex:0];
    id resultValue = [command argumentAtIndex:1];
    if (requestId.length == 0) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    BFPendingObfuscation *pending = nil;
    @synchronized (self.pendingObfuscations) {
        pending = self.pendingObfuscations[requestId];
    }
    if (pending != nil) {
        pending.response = [resultValue isKindOfClass:[NSDictionary class]] ? resultValue : nil;
        dispatch_semaphore_signal(pending.semaphore);
    }

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)fireDocumentEvent:(NSString *)eventName payload:(NSDictionary *)payload
{
    NSError *error = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:payload options:0 error:&error];
    if (jsonData == nil || error != nil) {
        return;
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSString *js = [NSString stringWithFormat:@"cordova.fireDocumentEvent('%@',%@);", eventName, jsonString];
    [self.commandDelegate evalJs:js];
}

- (NSDictionary *)invokeJSObfuscation:(NSString *)eventName arguments:(NSDictionary *)arguments
{
    if ([NSThread isMainThread]) {
        return nil;
    }

    NSString *requestId = [[NSUUID UUID] UUIDString];
    BFPendingObfuscation *pending = [[BFPendingObfuscation alloc] init];
    pending.semaphore = dispatch_semaphore_create(0);

    @synchronized (self.pendingObfuscations) {
        self.pendingObfuscations[requestId] = pending;
    }

    NSMutableDictionary *payload = [NSMutableDictionary dictionaryWithDictionary:arguments ?: @{}];
    payload[@"requestId"] = requestId;

    __weak BFCDVSDKBugfender *weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        BFCDVSDKBugfender *strongSelf = weakSelf;
        if (strongSelf != nil) {
            [strongSelf fireDocumentEvent:eventName payload:payload];
        }
    });

    long waitResult = dispatch_semaphore_wait(pending.semaphore, dispatch_time(DISPATCH_TIME_NOW, (int64_t)(3 * NSEC_PER_SEC)));

    @synchronized (self.pendingObfuscations) {
        [self.pendingObfuscations removeObjectForKey:requestId];
    }

    if (waitResult != 0) {
        return nil;
    }
    return pending.response;
}

- (NSDictionary<NSString *, NSString *> *)stringMapFrom:(id)value
{
    NSMutableDictionary<NSString *, NSString *> *mapped = [NSMutableDictionary dictionary];
    if (![value isKindOfClass:[NSDictionary class]]) {
        return mapped;
    }
    NSDictionary *raw = (NSDictionary *)value;
    for (id key in raw) {
        id entry = raw[key];
        mapped[[key description]] = entry == [NSNull null] || entry == nil ? @"" : [entry description];
    }
    return mapped;
}

- (void)installRequestObfuscationHandler
{
    __weak BFCDVSDKBugfender *weakSelf = self;
    [Bugfender setNetworkLoggingRequestObfuscationHandler:^BFNetworkRequestData * _Nonnull(NSString * _Nonnull url, NSDictionary<NSString *,NSString *> * _Nonnull headers, NSString * _Nullable body) {
        BFCDVSDKBugfender *strongSelf = weakSelf;
        if (strongSelf == nil) {
            return [[BFNetworkRequestData alloc] initWithURL:url headers:headers body:body];
        }

        NSDictionary *response = [strongSelf invokeJSObfuscation:@"BugfenderObfuscateNetworkRequest"
                                                       arguments:@{
            @"url": url ?: @"",
            @"headers": headers ?: @{},
            @"body": body ?: [NSNull null],
        }];
        if (response == nil) {
            return [[BFNetworkRequestData alloc] initWithURL:url headers:headers body:body];
        }

        NSString *obfuscatedUrl = [response[@"url"] isKindOfClass:[NSString class]] ? response[@"url"] : url;
        NSDictionary<NSString *, NSString *> *obfuscatedHeaders = [strongSelf stringMapFrom:response[@"headers"]];
        NSString *obfuscatedBody = nil;
        if ([response[@"body"] isKindOfClass:[NSString class]]) {
            obfuscatedBody = response[@"body"];
        }
        return [[BFNetworkRequestData alloc] initWithURL:obfuscatedUrl headers:obfuscatedHeaders body:obfuscatedBody];
    }];
}

- (void)installResponseObfuscationHandler
{
    __weak BFCDVSDKBugfender *weakSelf = self;
    [Bugfender setNetworkLoggingResponseObfuscationHandler:^BFNetworkResponseData * _Nonnull(NSDictionary<NSString *,NSString *> * _Nonnull headers, NSString * _Nullable body) {
        BFCDVSDKBugfender *strongSelf = weakSelf;
        if (strongSelf == nil) {
            return [[BFNetworkResponseData alloc] initWithHeaders:headers body:body];
        }

        NSDictionary *response = [strongSelf invokeJSObfuscation:@"BugfenderObfuscateNetworkResponse"
                                                       arguments:@{
            @"headers": headers ?: @{},
            @"body": body ?: [NSNull null],
        }];
        if (response == nil) {
            return [[BFNetworkResponseData alloc] initWithHeaders:headers body:body];
        }

        NSDictionary<NSString *, NSString *> *obfuscatedHeaders = [strongSelf stringMapFrom:response[@"headers"]];
        NSString *obfuscatedBody = nil;
        if ([response[@"body"] isKindOfClass:[NSString class]]) {
            obfuscatedBody = response[@"body"];
        }
        return [[BFNetworkResponseData alloc] initWithHeaders:obfuscatedHeaders body:obfuscatedBody];
    }];
}

@end
