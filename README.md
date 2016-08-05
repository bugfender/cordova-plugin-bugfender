# cordova-plugin-bugfender
This plugin adds Bugfender support for Cordova applications under iOS and Android.

## Installing
In the command line, run (replace `XXX` with your app key):

```
cordova plugin add https://github.com/bugfender/cordova-plugin-bugfender.git --variable BUGFENDER_APP_KEY=XXX --save
```

## Usage
This plugin has no Javascript API. Once installed, the plugin will fetch all application logs and send them to Bugfender.

### Development status
The implementation of this plugin is partial but stable.

The current implementation only allows for automatic logging. There is no possibility to configure the storage size, force send, send issues, etc. These will come in the future.
