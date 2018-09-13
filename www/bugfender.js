var exec = require("cordova/exec"),
	util = require("./bf-util"),
	stacktrace = require("./stacktrace");

module.exports = {

forceSendOnce: function () {
	if(device.platform != "browser")
		exec(null, null, "Bugfender", "forceSendOnce", []);
},

getDeviceIdentifier: function (s) {
	if(device.platform != "browser")
		exec(s, null, "Bugfender", "getDeviceIdentifier", []);
},

removeDeviceKey: function (key) {
	if(device.platform != "browser")
		exec(null, null, "Bugfender", "removeDeviceKey", [key]);
},

sendIssue: function (title, text) {
	if(device.platform != "browser")
		exec(null, null, "Bugfender", "sendIssue", [title, text]);
},

setDeviceKey: function (key, value) {
	if(device.platform != "browser")
		exec(null, null, "Bugfender", "setDeviceKey", [key, value]);
},

setForceEnabled: function (enabled) {
	if(device.platform != "browser")
		exec(null, null, "Bugfender", "setForceEnabled", [enabled]);
},

setMaximumLocalStorageSize: function (bytes) {
	if(device.platform != "browser")
		exec(null, null, "Bugfender", "setMaximumLocalStorageSize", [bytes]);
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
	printToConsole = v;
},

getPrintToConsole: function() {
	return printToConsole;
}

};

var printToConsole = true;
var stacktraceLine = /(?:([^@]*)@)?(?:.*\/)?([^:]*)(?::(\d*))?/;
var logWithLevel = function(level, args) {
	var st = stacktrace();
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

	if(device.platform != "browser")
		exec(null, null, "Bugfender", "log", [line, func, file, level, tag, message]);
	if(printToConsole)
		console.log(message);
}
