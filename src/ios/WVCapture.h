#import <Cordova/CDVPlugin.h>
#import <Cordova/CDVPluginResult.h>

/** id capture
    author: Milo Liu
    License: MIT
**
*/
@interface WVCapture : CDVPlugin

@property (strong) UIWebView* webviewHandle;

- (void)capture:(CDVInvokedUrlCommand*)command;

@end