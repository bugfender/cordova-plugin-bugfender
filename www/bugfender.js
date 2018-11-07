var	util = require("./bf-util");

module.exports = {

forceSendOnce: function () {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(null, null, "Bugfender", "forceSendOnce", []);
},

getDeviceIdentifier: function (s) {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(s, null, "Bugfender", "getDeviceIdentifier", []);
},

removeDeviceKey: function (key) {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(null, null, "Bugfender", "removeDeviceKey", [key]);
},

sendIssue: function (title, text) {
	checkLoaded();
	if(window["device"] && window["device"].platform != "browser")
		window["cordova"].exec(null, null, "Bugfender", "sendIssue", [title, text]);
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
	logWithLevel("info", arguments);
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
}

};
module.exports.Bugfender = module.exports;

var printToConsole = true;
var stacktraceLine = /(?:([^@]*)@)?(?:.*\/)?([^:]*)(?::(\d*))?/;
var logWithLevel = function(level, args) {
	checkLoaded();
	var st = new Error().stack;
	var match = stacktraceLine.exec(st[5]);
	var func = "<anonymous>";
	var file = "";
	var line = "";
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
