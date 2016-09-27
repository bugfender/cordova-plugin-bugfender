package com.bugfender.sdk.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import android.app.Application;
import android.content.Context;

import com.bugfender.sdk.Bugfender;
import com.bugfender.sdk.LogLevel;

public class BugfenderPlugin extends CordovaPlugin {

		@Override
		protected void pluginInitialize() {
			int appResId = this.cordova.getActivity().getResources().getIdentifier("BUGFENDER_APP_KEY", "string", this.cordova.getActivity().getPackageName());
			String key = this.cordova.getActivity().getString(appResId);
		    if (key.length() == 0) {
				System.out.println("Please set BUGFENDER_APP_KEY in config.xml");
				return;
			}

			int enablesResId = this.cordova.getActivity().getResources().getIdentifier("BUGFENDER_AUTOMATIC", "string", this.cordova.getActivity().getPackageName());
			String enabled = this.cordova.getActivity().getString(enablesResId);
		    if (enabled.length() == 0 || "ALL".equals(enabled))
				enabled = "UI,LOG";
			List<String> enables = Arrays.asList(enabled.split(","));

			Context context=this.cordova.getActivity().getApplicationContext();
			Bugfender.init(context, key, false);

			if(enables.contains("LOG")) {
		    	Bugfender.enableLogcatLogging();
			}	
			if(enables.contains("UI")) {
				Application app=this.cordova.getActivity().getApplication();
				Bugfender.enableUIEventLogging(app);
			}	
		}

		@Override
		public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
			if (action.equals("log")) {
				int lineNumber = args.getInt(0);
				String method = args.getString(1);
				String fileName = args.getString(2);
				String levelString = args.getString(3);
				String tag = args.getString(4);
				String message = args.getString(5);

				LogLevel logLevel = LogLevel.Debug;
				if ("error".equals(levelString)) {
					logLevel = LogLevel.Error;
				} else if ("warn".equals(levelString)) {
					logLevel = LogLevel.Warning;
				}
				Bugfender.log(lineNumber, method, fileName, logLevel, tag, message);
				
				callbackContext.success();
				return true;
			} else if (action.equals("forceSendOnce")) {
				Bugfender.forceSendOnce();
				callbackContext.success();
				return true;
			} else if (action.equals("getDeviceIdentifier")) {
				String deviceId = Bugfender.getDeviceIdentifier();
				callbackContext.success(deviceId);
				return true;
			} else if (action.equals("removeDeviceKey")) {
				String key = args.getString(0);
				Bugfender.removeDeviceKey(key);
				callbackContext.success();
				return true;
			} else if (action.equals("sendIssue")) {
				String title = args.getString(0);
				String text = args.getString(1);
				Bugfender.sendIssue(title, text);
				callbackContext.success();
				return true;
			} else if (action.equals("setDeviceKey")) {
				String key = args.getString(0);
				Object value = args.get(1);
				if(value instanceof String) {
					Bugfender.setDeviceString(key, (String)value);
					callbackContext.success();
					return true;
				} else if(value instanceof Float) {
					Bugfender.setDeviceFloat(key, (Float)value);
					callbackContext.success();
					return true;
				} else if(value instanceof Integer) {
					Bugfender.setDeviceInteger(key, (Integer)value);
					callbackContext.success();
					return true;
				} else if(value instanceof Boolean) {
					Bugfender.setDeviceBoolean(key, (Boolean)value);
					callbackContext.success();
					return true;
				}
				return false;
			} else if (action.equals("setForceEnabled")) {
				Boolean enabled = args.getBoolean(0);
				Bugfender.setForceEnabled(enabled);
				callbackContext.success();
				return true;
			} else if (action.equals("setMaximumLocalStorageSize")) {
				long size = args.getLong(0);
				Bugfender.setMaximumLocalStorageSize(size);
				callbackContext.success();
				return true;
			}
			return false;
		}
}
