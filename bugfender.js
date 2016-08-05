var exec = require("cordova/exec");

module.exports = {

forceSendOnce: function (s, f) {
	exec(s, f, "BugfenderPlugin", "forceSendOnce", []);
},

getDeviceIdentifier: function (s, f) {
	exec(s, f, "BugfenderPlugin", "getDeviceIdentifier", []);
},

removeDeviceKey: function (key, s, f) {
	exec(s, f, "BugfenderPlugin", "removeDeviceKey", [key]);
},

sendIssue: function (title, text, s, f) {
	exec(s, f, "BugfenderPlugin", "sendIssue", [title, text]);
},

setDeviceKey: function (key, value, s, f) {
	exec(s, f, "BugfenderPlugin", "setDeviceKey", [key, value]);
},

setForceEnabled: function (enabled, s, f) {
	exec(s, f, "BugfenderPlugin", "setForceEnabled", [enabled]);
},

setMaximumLocalStorageSize: function (bytes, s, f) {
	exec(s, f, "BugfenderPlugin", "setMaximumLocalStorageSize", [bytes]);
}

};
