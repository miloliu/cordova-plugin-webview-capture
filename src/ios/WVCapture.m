#import "WVCapture.h"

@implementation WVCapture

- (CDVPlugin*)initWithWebView:(UIWebView*)theWebView {
    self.webviewHandle = theWebView;
    return self;
}
- (void)capture:(CDVInvokedUrlCommand *)command {
    if (!self.webviewHandle) {
        CDVPluginResult *pluginResult=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"webview cannot be assigned"];
        [[self commandDelegate] sendPluginResult:pluginResult callbackId:command.callbackId];
    }

    UIGraphicsBeginImageContext(self.webviewHandle.bounds.size);
    [[self.webviewHandle layer] renderInContext:UIGraphicsGetCurrentContext()];
    UIImage* image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    NSData* png = UIImagePNGRepresentation(image);
    NSString* pngStr = [png base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength];
    
    if (pngStr) {
        NSDictionary* dict = [NSDictionary dictionaryWithObjectsAndKeys:pngStr, @"pngStr", nil];
        CDVPluginResult *pluginResult=[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dict];
        [[self commandDelegate] sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    else {
        CDVPluginResult *pluginResult=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [[self commandDelegate] sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void)onReset {
    self.webviewHandle = NULL;
}
@end