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
			if (action.equals("log")) {
				String message = args.getString(0);
				System.out.println(message);
				return true;
			}
			return false;
		}
}
