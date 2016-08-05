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
    NSLog(@"Initializing Bugfender with app key %@", key);
    [Bugfender enableAllWithToken:key];
}

@end
