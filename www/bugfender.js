var	util = require("./bf-util");

var OBFUSCATE_REQUEST_EVENT = "BugfenderObfuscateNetworkRequest";
var OBFUSCATE_RESPONSE_EVENT = "BugfenderObfuscateNetworkResponse";

var requestObfuscationHandler = null;
var responseObfuscationHandler = null;
var obfuscationListenerInstalled = false;

function isNativePlatform() {
	return window["device"] && window["device"].platform != "browser";
}

function execNative(success, error, action, args) {
	checkLoaded();
	if (isNativePlatform()) {
		window["cordova"].exec(success, error, "Bugfender", action, args || []);
	}
}

function ensureObfuscationListener() {
	if (obfuscationListenerInstalled || typeof document === "undefined") {
		return;
	}
	obfuscationListenerInstalled = true;

	document.addEventListener(OBFUSCATE_REQUEST_EVENT, function (event) {
		handleObfuscateRequest(event);
	}, false);
	document.addEventListener(OBFUSCATE_RESPONSE_EVENT, function (event) {
		handleObfuscateResponse(event);
	}, false);
}

function handleObfuscateRequest(event) {
	var url = event.url != null ? event.url : "";
	var headers = event.headers != null ? event.headers : {};
	var body = event.body != null ? event.body : null;
	var result = { url: url, headers: headers, body: body };

	try {
		if (requestObfuscationHandler) {
			result = requestObfuscationHandler(url, Object.assign({}, headers), body);
		}
	} catch (e) {
		result = { url: url, headers: {}, body: null };
	}

	execNative(null, null, "completeNetworkObfuscation", [event.requestId, result]);
}

function handleObfuscateResponse(event) {
	var headers = event.headers != null ? event.headers : {};
	var body = event.body != null ? event.body : null;
	var result = { headers: headers, body: body };

	try {
		if (responseObfuscationHandler) {
			result = responseObfuscationHandler(Object.assign({}, headers), body);
		}
	} catch (e) {
		result = { headers: {}, body: null };
	}

	execNative(null, null, "completeNetworkObfuscation", [event.requestId, result]);
}

module.exports = {

forceSendOnce: function () {
	execNative(null, null, "forceSendOnce", []);
},

removeDeviceKey: function (key) {
	execNative(null, null, "removeDeviceKey", [key]);
},

sendIssue: function (title, markdown, callback) {
	execNative(callback, null, "sendIssue", [title, markdown]);
},

setDeviceKey: function (key, value) {
	execNative(null, null, "setDeviceKey", [key, value]);
},

setForceEnabled: function (enabled) {
	execNative(null, null, "setForceEnabled", [enabled]);
},

setMaximumLocalStorageSize: function (bytes) {
	execNative(null, null, "setMaximumLocalStorageSize", [bytes]);
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
	execNative(callback, null, "getDeviceUrl", []);
},

getSessionUrl: function (callback) {
	execNative(callback, null, "getSessionUrl", []);
},

sendCrash: function(title, markdown, callback) {
	execNative(callback, null, "sendCrash", [title, markdown]);
},

sendUserFeedback: function(title, markdown, callback) {
	execNative(callback, null, "sendUserFeedback", [title, markdown]);
},

showUserFeedbackUI: function(title, hint, subjectHint, messageHint, sendButtonText, cancelButtonText, callback) {
	execNative(callback, callback, "showUserFeedbackUI", [title, hint, subjectHint, messageHint, sendButtonText, cancelButtonText]);
},

setNetworkLoggingEnabled: function (enabled) {
	execNative(null, null, "setNetworkLoggingEnabled", [!!enabled]);
},

setNetworkLoggingCaptureBodies: function (capture) {
	execNative(null, null, "setNetworkLoggingCaptureBodies", [!!capture]);
},

setNetworkLoggingCaptureErrorResponseBodies: function (capture) {
	execNative(null, null, "setNetworkLoggingCaptureErrorResponseBodies", [!!capture]);
},

setNetworkLoggingURLFilter: function (allowlist, denylist) {
	execNative(null, null, "setNetworkLoggingURLFilter", [allowlist, denylist]);
},

setNetworkLoggingMaxRequestsPerMinute: function (count) {
	execNative(null, null, "setNetworkLoggingMaxRequestsPerMinute", [count]);
},

setNetworkLoggingRequestObfuscationHandler: function (handler) {
	requestObfuscationHandler = handler;
	if (handler) {
		ensureObfuscationListener();
	}
	execNative(null, null, "setNetworkLoggingRequestObfuscationHandlerEnabled", [handler != null]);
},

setNetworkLoggingResponseObfuscationHandler: function (handler) {
	responseObfuscationHandler = handler;
	if (handler) {
		ensureObfuscationListener();
	}
	execNative(null, null, "setNetworkLoggingResponseObfuscationHandlerEnabled", [handler != null]);
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

	if(isNativePlatform())
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
