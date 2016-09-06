package com.bugfender.sdk.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;

import com.bugfender.sdk.Bugfender;

public class BugfenderPlugin extends CordovaPlugin {

		@Override
		protected void pluginInitialize() {
			int appResId = this.cordova.getActivity().getResources().getIdentifier("BUGFENDER_APP_KEY", "string", this.cordova.getActivity().getPackageName());
			String key = this.cordova.getActivity().getString(appResId);
		    if (key == "") {
				System.out.println("Please set BUGFENDER_APP_KEY in config.xml");
				return;
			}
		    System.out.println("Initializing Bugfender with app key " + key);

			Context context=this.cordova.getActivity().getApplicationContext();
			Application app=this.cordova.getActivity().getApplication();
			Bugfender.init(context, key, false);
		    Bugfender.enableLogcatLogging();
			Bugfender.enableUIEventLogging(app);
		}

		@Override
		public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
			if (action.equals("info")) {
				String message = args.getString(0);
				Bugfender.d("", message);
				callbackContext.success();
				return true;
			} else if (action.equals("warn")) {
				String message = args.getString(0);
				Bugfender.w("", message);
				callbackContext.success();
				return true;
			} else if (action.equals("error")) {
				String message = args.getString(0);
				Bugfender.e("", message);
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
