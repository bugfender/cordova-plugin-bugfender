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
	//var text = util.format.apply(this, arguments);
	var regex = /([^@]*)@.*\/([^:]*):(\d*)/;
	var match = regex.exec(stacktrace()[4])
	var func = match[1];
	var file = match[2];
	var line = match[3];
	var text = "func: " + func + " file: " + file + " line: " + line + " message: " + util.format.apply(this, arguments);
	exec(null, null, "Bugfender", "info", [text]);
},

error: function () {
	exec(null, null, "Bugfender", "error", [util.format.apply(this, arguments)]);
},

warn: function () {
	exec(null, null, "Bugfender", "warn", [util.format.apply(this, arguments)]);
},

info: function () {
	exec(null, null, "Bugfender", "info", [util.format.apply(this, arguments)]);
},

trace: function () {
	exec(null, null, "Bugfender", "info", [util.format.apply(this, arguments)]); //TBD
},

};