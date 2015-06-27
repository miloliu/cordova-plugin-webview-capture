var exec    = require('cordova/exec'),
cordova = require('cordova');

module.exports = {
	capture : function(success, fail) {
		exec(success, fail, "WVCapture", "capture",[]);
	}
};