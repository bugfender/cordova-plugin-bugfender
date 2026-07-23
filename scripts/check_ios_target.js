#!/usr/bin/env node
var path = require("path");
var semver = require('semver');

module.exports = function (context) {
    var rootPath = context.opts.projectRoot;
    var configXmlPath = path.join(rootPath, 'config.xml');
    var configParser = getConfigParser(context, configXmlPath);
    var iosMinVersion = configParser.getPreference('deployment-target', 'ios') ||
        configParser.getPreference('deployment-target') ||
        '7.0';

    var coerced = semver.coerce(iosMinVersion);
    if (!coerced || !semver.gte(coerced, '13.0.0')) {
        throw 'Deployment target version is required to be 13.0 at least. Detected version '+iosMinVersion+' instead.\n'+
              'Add something like this to your iOS target:\n'+
              '<preference name="deployment-target" value="13.0" />';
    }
}

function getConfigParser(context, config) {
	var ConfigParser;
	if (semver.lt(context.opts.cordova.version, '5.4.0')) {
	    ConfigParser = context.requireCordovaModule('cordova-lib/src/ConfigParser/ConfigParser');
	} else {
	    ConfigParser = context.requireCordovaModule('cordova-common/src/ConfigParser/ConfigParser');
	}

	return new ConfigParser(config);
}
