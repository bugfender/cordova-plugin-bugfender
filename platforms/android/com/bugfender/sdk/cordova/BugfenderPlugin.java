package com.bugfender.sdk.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import java.net.URL;

import android.app.Application;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.bugfender.sdk.Bugfender;
import com.bugfender.sdk.LogLevel;

public class BugfenderPlugin extends CordovaPlugin {

		private CallbackContext callback = null;
		public static final int FEEDBACK_REQUEST_CODE = 2222;

		@Override
		protected void pluginInitialize() {
			int hideDeviceNameResId = this.cordova.getActivity().getResources().getIdentifier("BUGFENDER_HIDE_DEVICE_NAME", "string", this.cordova.getActivity().getPackageName());
			String hideDeviceName = this.cordova.getActivity().getString(hideDeviceNameResId);
		    if (!"unset".equals(hideDeviceName)) {
				Bugfender.overrideDeviceName("Unknown");
			}

			int baseURLResId = this.cordova.getActivity().getResources().getIdentifier("BUGFENDER_BASE_URL", "string", this.cordova.getActivity().getPackageName());
			String baseURL = this.cordova.getActivity().getString(baseURLResId);
		    if (!"unset".equals(baseURL)) {
				Bugfender.setBaseUrl(baseURL);
			}

			int apiURLResId = this.cordova.getActivity().getResources().getIdentifier("BUGFENDER_API_URL", "string", this.cordova.getActivity().getPackageName());
			String apiURL = this.cordova.getActivity().getString(apiURLResId);
		    if (!"unset".equals(apiURL)) {
				Bugfender.setApiUrl(apiURL);
			}

			int appResId = this.cordova.getActivity().getResources().getIdentifier("BUGFENDER_APP_KEY", "string", this.cordova.getActivity().getPackageName());
			String key = this.cordova.getActivity().getString(appResId);
		    if (key.length() == 0) {
				System.out.println("Please set BUGFENDER_APP_KEY in config.xml");
				return;
			}

			int enablesResId = this.cordova.getActivity().getResources().getIdentifier("BUGFENDER_AUTOMATIC", "string", this.cordova.getActivity().getPackageName());
			String enabled = this.cordova.getActivity().getString(enablesResId);
		    if (enabled.length() == 0 || "ALL".equals(enabled))
				enabled = "UI,LOG,CRASH";
			List<String> enables = Arrays.asList(enabled.split(","));

			Context context=this.cordova.getActivity().getApplicationContext();
			Bugfender.init(context, key, false);

			if(enables.contains("LOG")) {
		    	Bugfender.enableLogcatLogging();
			}	
			if(enables.contains("CRASH")) {
		    	Bugfender.enableCrashReporting();
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
				if ("fatal".equals(levelString)) {
					logLevel = LogLevel.Fatal;
				} else if ("error".equals(levelString)) {
					logLevel = LogLevel.Error;
				} else if ("warn".equals(levelString)) {
					logLevel = LogLevel.Warning;
				} else if ("info".equals(levelString)) {
					logLevel = LogLevel.Info;
				} else if ("trace".equals(levelString)) {
					logLevel = LogLevel.Trace;
				}
				Bugfender.log(lineNumber, method, fileName, logLevel, tag, message);
				
				callbackContext.success();
				return true;
			} else if (action.equals("forceSendOnce")) {
				Bugfender.forceSendOnce();
				callbackContext.success();
				return true;
			} else if (action.equals("getDeviceUrl")) {
				URL deviceURL = Bugfender.getDeviceUrl();
				callbackContext.success(deviceURL.toString());
				return true;
			} else if (action.equals("getSessionUrl")) {
				URL sessionURL = Bugfender.getSessionUrl();
				callbackContext.success(sessionURL.toString());
				return true;
			} else if (action.equals("removeDeviceKey")) {
				String key = args.getString(0);
				Bugfender.removeDeviceKey(key);
				callbackContext.success();
				return true;
			} else if (action.equals("sendIssue")) {
				String title = args.getString(0);
				String text = args.getString(1);
				URL issueURL = Bugfender.sendIssue(title, text);
				callbackContext.success(issueURL.toString());
				return true;
			} else if (action.equals("sendCrash")) {
				String title = args.getString(0);
				String text = args.getString(1);
				URL crashURL = Bugfender.sendCrash(title, text);
				callbackContext.success(crashURL.toString());
				return true;
			} else if (action.equals("sendUserFeedback")) {
				String title = args.getString(0);
				String text = args.getString(1);
				URL ufURL = Bugfender.sendUserFeedback(title, text);
				callbackContext.success(ufURL.toString());
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
			} else if (action.equals("showUserFeedbackUI")) {
				String title = args.getString(0);
				String hint = args.getString(1);
				String subjectHint = args.getString(2);
				String messageHint = args.getString(3);
				String sendButtonText = args.getString(4);;
				this.callback = callbackContext;
				cordova.setActivityResultCallback (this);

				Intent userFeedbackIntent = Bugfender.getUserFeedbackActivityIntent (this.cordova.getActivity(), title, hint, subjectHint, messageHint, sendButtonText);
				cordova.startActivityForResult (this, userFeedbackIntent, FEEDBACK_REQUEST_CODE);
				return true;
			}
			return false;
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) 
		{
			if (requestCode == FEEDBACK_REQUEST_CODE) {
				if (resultCode == Activity.RESULT_OK) {
					this.callback.success(data.getStringExtra ("result.feedback.url"));
				} else {
					this.callback.error("User cancelled");
				}
			} else {
				super.onActivityResult (requestCode, resultCode, data);
			}
		}
}
