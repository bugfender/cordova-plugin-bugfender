var	util = require("./bf-util");

module.exports = {

forceSendOnce: function () {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(null, null, "Bugfender", "forceSendOnce", []);
},

removeDeviceKey: function (key) {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(null, null, "Bugfender", "removeDeviceKey", [key]);
},

sendIssue: function (title, markdown, callback) {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(callback, null, "Bugfender", "sendIssue", [title, markdown]);
},

setDeviceKey: function (key, value) {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(null, null, "Bugfender", "setDeviceKey", [key, value]);
},

setForceEnabled: function (enabled) {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(null, null, "Bugfender", "setForceEnabled", [enabled]);
},

setMaximumLocalStorageSize: function (bytes) {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(null, null, "Bugfender", "setMaximumLocalStorageSize", [bytes]);
},

log: function () {
	logWithLevel("debug", arguments);
},

fatal: function () {
	logWithLevel("fatal", arguments);
},

error: function () {
	logWithLevel("error", arguments);
},

warn: function () {
	logWithLevel("warn", arguments);
},

info: function () {
	logWithLevel("info", arguments);
},

debug: function () {
	logWithLevel("debug", arguments);
},

trace: function () {
	logWithLevel("trace", arguments);
},

setPrintToConsole: function(v) {
	checkLoaded();
	printToConsole = v;
},

getPrintToConsole: function() {
	checkLoaded();
	return printToConsole;
},

getDeviceUrl: function(callback) {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(callback, null, "Bugfender", "getDeviceUrl", []);
},

getSessionUrl: function (callback) {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(callback, null, "Bugfender", "getSessionUrl", []);
},

sendCrash: function(title, markdown, callback) {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(callback, null, "Bugfender", "sendCrash", [title, markdown]);
},

sendUserFeedback: function(title, markdown, callback) {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(callback, null, "Bugfender", "sendUserFeedback", [title, markdown]);
},

showUserFeedbackUI: function(title, hint, subjectHint, messageHint, sendButtonText, cancelButtonText, callback) {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(callback, callback, "Bugfender", "showUserFeedbackUI", [title, hint, subjectHint, messageHint, sendButtonText, cancelButtonText]);
},

};
module.exports.Bugfender = module.exports;

/* example on iOS:
logWithLevel@file:///Users/x/Library/Developer/CoreSimulator/Devices/E7784688-3697-4254-9B15-D0EB21E7927E/data/Containers/Bundle/Application/C101081E-FB05-438F-AF1F-6CC1F64AF220/HelloCordova.app/www/plugins/cordova-plugin-bugfender/www/bugfender.js:85:20
log@file:///Users/x/Library/Developer/CoreSimulator/Devices/E7784688-3697-4254-9B15-D0EB21E7927E/data/Containers/Bundle/Application/C101081E-FB05-438F-AF1F-6CC1F64AF220/HelloCordova.app/www/plugins/cordova-plugin-bugfender/www/bugfender.js:49:14
onDeviceReady@file:///Users/x/Library/Developer/CoreSimulator/Devices/E7784688-3697-4254-9B15-D0EB21E7927E/data/Containers/Bundle/Application/C101081E-FB05-438F-AF1F-6CC1F64AF220/HelloCordova.app/www/js/index.js:31:16

* example on Android:
Error
	at logWithLevel (file:///android_asset/www/plugins/cordova-plugin-bugfender/www/bugfender.js:85:11)
	at Object.log (file:///android_asset/www/plugins/cordova-plugin-bugfender/www/bugfender.js:49:2)
	at Object.onDeviceReady (file:///android_asset/www/js/index.js:31:13)
*/

var printToConsole = true;
var stacktraceLine = /(?:\W*at )?(?:(?:[^.]+\.)?([^@ ]*)[ @])?(?:.*\/)?([^:]*)(?::(\d*))?/;
var logWithLevel = function(level, args) {
	checkLoaded();
	var st = new Error().stack.split('\n');
	var caller = st[st[0].indexOf('Error') == 0 ? 3 : 2];
	var match = stacktraceLine.exec(caller);
	var func = "<anonymous>";
	var file = "";
	var line = 0;
	if(match != null) {
		if(match.length >= 1 && match[1] != null)
			func = match[1];
		if(match.length >= 2 && match[2] != null)
			file = match[2];
		if(match.length >= 3 && match[3] != null)
			line = Number(match[3]);
	}
	var tag = "";
	var message = util.format.apply(this, args);

	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(null, null, "Bugfender", "log", [line, func, file, level, tag, message]);
	if(printToConsole)
		console.log(message);
}

var notLoadedWarningShown = false;
var checkLoaded = function() {
  if(!window["cordova"] && !notLoadedWarningShown) {
	console.warn("Bugfender: Cordova is not loaded, probably because running on an unsupported platform (eg. browser) or Cordova plugins not loaded yet, did you include cordova.js? Logs will not be sent")
	notLoadedWarningShown = true
  }
  if(window["device"] && window["device"].platform == "browser" && !notLoadedWarningShown) {
	console.warn("Bugfender: Browser environment unsupported. Logs will not be sent")
	notLoadedWarningShown = true
  }
}
