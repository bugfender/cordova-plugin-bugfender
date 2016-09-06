#import "BFCDVSDKBugfender.h"
#import <BugfenderSDK/BugfenderSDK.h>

@implementation BFCDVSDKBugfender

- (void)pluginInitialize
{
    NSString *key = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"BUGFENDER_APP_KEY"];
    if(key == nil) {
        NSLog(@"Please set BUGFENDER_APP_KEY in config.xml");
        return;
    }
    [Bugfender activateLogger:key];

    NSString *enabled = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"BUGFENDER_AUTOMATIC"];
    if(enabled == nil || [enabled isEqualToString:@"ALL"])
        enabled == @"UI,LOG";

    NSArray* enables = [enabled componentsSeparatedByString:@","];
    if ([enables containsObject:@"UI"]) {
        [Bugfender enableUIEventLogging];
    }
    if ([enables containsObject:@"LOG"]) {
        [Bugfender enableNSLogLogging];
    }
}

- (void)forceSendOnce:(CDVInvokedUrlCommand*)command
{
    [Bugfender forceSendOnce];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getDeviceIdentifier:(CDVInvokedUrlCommand*)command
{
    NSString* deviceId = [Bugfender deviceIdentifier];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:deviceId];
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
    [Bugfender sendIssueWithTitle:title text:text];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
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
    NSInteger lineNumber = [[command.arguments objectAtIndex:0] unsignedLongLongValue];
    NSString* method = [command.arguments objectAtIndex:1];
    NSString* fileName = [command.arguments objectAtIndex:2];
    NSString* levelString = [command.arguments objectAtIndex:3];
    NSString* tag = [command.arguments objectAtIndex:4];
    NSString* message = [command.arguments objectAtIndex:5];
    
    BFLogLevel level = BFLogLevelDefault;
    if([levelString isEqualToString:@"error"]) {
        level = BFLogLevelError;
    } else if([levelString isEqualToString:@"warn"]) {
        level = BFLogLevelWarning;
    }
    //TODO trace level
    
    [Bugfender logLineNumber:lineNumber method:method file:fileName level:level tag:tag message:message];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
