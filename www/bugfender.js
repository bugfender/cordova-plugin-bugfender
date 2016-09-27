var exec = require("cordova/exec"),
	util = require("./bf-util"),
	stacktrace = require("./stacktrace");

module.exports = {

forceSendOnce: function () {
	exec(null, null, "Bugfender", "forceSendOnce", []);
},

getDeviceIdentifier: function (s) {
	exec(s, null, "Bugfender", "getDeviceIdentifier", []);
},

removeDeviceKey: function (key) {
	exec(null, null, "Bugfender", "removeDeviceKey", [key]);
},

sendIssue: function (title, text) {
	exec(null, null, "Bugfender", "sendIssue", [title, text]);
},

setDeviceKey: function (key, value) {
	exec(null, null, "Bugfender", "setDeviceKey", [key, value]);
},

setForceEnabled: function (enabled) {
	exec(null, null, "Bugfender", "setForceEnabled", [enabled]);
},

setMaximumLocalStorageSize: function (bytes) {
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

};

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

	exec(null, null, "Bugfender", "log", [line, func, file, level, tag, message]);
}
