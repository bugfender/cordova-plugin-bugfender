package com.bugfender.sdk.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Application;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.bugfender.sdk.Bugfender;
import com.bugfender.sdk.LogLevel;

public class BugfenderPlugin extends CordovaPlugin {

	private static final String OBFUSCATE_REQUEST_EVENT = "BugfenderObfuscateNetworkRequest";
	private static final String OBFUSCATE_RESPONSE_EVENT = "BugfenderObfuscateNetworkResponse";

	private static class PendingObfuscation {
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<JSONObject> result = new AtomicReference<>();
	}

	private final ConcurrentHashMap<String, PendingObfuscation> pendingObfuscations =
		new ConcurrentHashMap<>();

	private CallbackContext callback = null;
	public static final int FEEDBACK_REQUEST_CODE = 2222;

	private static boolean preferenceEnabled(String value) {
		if (value == null || value.length() == 0 || "unset".equals(value) || "false".equalsIgnoreCase(value)) {
			return false;
		}
		return "true".equalsIgnoreCase(value) || "YES".equalsIgnoreCase(value) || "1".equals(value);
	}

	private String getPreferenceString(String name) {
		if (this.preferences != null) {
			String fromPrefs = this.preferences.getString(name, null);
			if (fromPrefs != null && fromPrefs.length() > 0) {
				return fromPrefs;
			}
		}
		int resId = this.cordova.getActivity().getResources().getIdentifier(
			name,
			"string",
			this.cordova.getActivity().getPackageName()
		);
		if (resId == 0) {
			return null;
		}
		return this.cordova.getActivity().getString(resId);
	}

	@Override
	protected void pluginInitialize() {
		String hideDeviceName = getPreferenceString("BUGFENDER_HIDE_DEVICE_NAME");
		if (hideDeviceName != null && !"unset".equals(hideDeviceName)) {
			Bugfender.overrideDeviceName("Unknown");
		}

		String baseURL = getPreferenceString("BUGFENDER_BASE_URL");
		if (baseURL != null && !"unset".equals(baseURL)) {
			Bugfender.setBaseUrl(baseURL);
		}

		String apiURL = getPreferenceString("BUGFENDER_API_URL");
		if (apiURL != null && !"unset".equals(apiURL)) {
			Bugfender.setApiUrl(apiURL);
		}

		String key = getPreferenceString("BUGFENDER_APP_KEY");
		if (key == null || key.length() == 0) {
			System.out.println("Please set BUGFENDER_APP_KEY in config.xml");
			return;
		}

		String enabled = getPreferenceString("BUGFENDER_AUTOMATIC");
		if (enabled == null || enabled.length() == 0 || "ALL".equals(enabled)) {
			enabled = "UI,LOG,CRASH";
		}
		List<String> enables = Arrays.asList(enabled.split(","));

		Context context = this.cordova.getActivity().getApplicationContext();
		Bugfender.init(context, key, false);

		if (enables.contains("LOG")) {
			Bugfender.enableLogcatLogging();
		}
		if (enables.contains("CRASH")) {
			Bugfender.enableCrashReporting();
		}
		if (enables.contains("UI")) {
			Application app = this.cordova.getActivity().getApplication();
			Bugfender.enableUIEventLogging(app);
		}

		if (preferenceEnabled(getPreferenceString("BUGFENDER_NETWORK_LOGGING"))) {
			Bugfender.setNetworkLoggingEnabled(true);
		}
		if (preferenceEnabled(getPreferenceString("BUGFENDER_NETWORK_CAPTURE_BODIES"))) {
			Bugfender.setNetworkLoggingCaptureBodies(true);
		}
		if (preferenceEnabled(getPreferenceString("BUGFENDER_NETWORK_CAPTURE_ERROR_BODIES"))) {
			Bugfender.setNetworkLoggingCaptureErrorResponseBodies(true);
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
			if (value instanceof String) {
				Bugfender.setDeviceString(key, (String) value);
				callbackContext.success();
				return true;
			} else if (value instanceof Float) {
				Bugfender.setDeviceFloat(key, (Float) value);
				callbackContext.success();
				return true;
			} else if (value instanceof Integer) {
				Bugfender.setDeviceInteger(key, (Integer) value);
				callbackContext.success();
				return true;
			} else if (value instanceof Boolean) {
				Bugfender.setDeviceBoolean(key, (Boolean) value);
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
			String sendButtonText = args.getString(4);
			this.callback = callbackContext;
			cordova.setActivityResultCallback(this);

			Intent userFeedbackIntent = Bugfender.getUserFeedbackActivityIntent(
				this.cordova.getActivity(),
				title,
				hint,
				subjectHint,
				messageHint,
				sendButtonText
			);
			cordova.startActivityForResult(this, userFeedbackIntent, FEEDBACK_REQUEST_CODE);
			return true;
		} else if (action.equals("setNetworkLoggingEnabled")) {
			Bugfender.setNetworkLoggingEnabled(args.getBoolean(0));
			callbackContext.success();
			return true;
		} else if (action.equals("setNetworkLoggingCaptureBodies")) {
			Bugfender.setNetworkLoggingCaptureBodies(args.getBoolean(0));
			callbackContext.success();
			return true;
		} else if (action.equals("setNetworkLoggingCaptureErrorResponseBodies")) {
			Bugfender.setNetworkLoggingCaptureErrorResponseBodies(args.getBoolean(0));
			callbackContext.success();
			return true;
		} else if (action.equals("setNetworkLoggingURLFilter")) {
			Bugfender.setNetworkLoggingURLFilter(
				toStringList(args.isNull(0) ? null : args.opt(0)),
				toStringList(args.isNull(1) ? null : args.opt(1))
			);
			callbackContext.success();
			return true;
		} else if (action.equals("setNetworkLoggingMaxRequestsPerMinute")) {
			if (args.isNull(0)) {
				Bugfender.setNetworkLoggingMaxRequestsPerMinute(null);
			} else {
				Bugfender.setNetworkLoggingMaxRequestsPerMinute(args.getInt(0));
			}
			callbackContext.success();
			return true;
		} else if (action.equals("setNetworkLoggingRequestObfuscationHandlerEnabled")) {
			setObfuscationHandler(
				"setNetworkLoggingRequestObfuscationHandler",
				args.getBoolean(0) ? createRequestObfuscationHandler() : null
			);
			callbackContext.success();
			return true;
		} else if (action.equals("setNetworkLoggingResponseObfuscationHandlerEnabled")) {
			setObfuscationHandler(
				"setNetworkLoggingResponseObfuscationHandler",
				args.getBoolean(0) ? createResponseObfuscationHandler() : null
			);
			callbackContext.success();
			return true;
		} else if (action.equals("completeNetworkObfuscation")) {
			String requestId = args.optString(0, null);
			if (requestId != null && requestId.length() > 0) {
				PendingObfuscation pending = pendingObfuscations.get(requestId);
				if (pending != null) {
					pending.result.set(args.optJSONObject(1));
					pending.latch.countDown();
				}
			}
			callbackContext.success();
			return true;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FEEDBACK_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				this.callback.success(data.getStringExtra("result.feedback.url"));
			} else {
				this.callback.error("User cancelled");
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * Android SDK 4.0.1 ships R8-obfuscated handler types. Resolve them via
	 * reflection so this plugin compiles against the published Maven artifact.
	 */
	private void setObfuscationHandler(String methodName, Object handler) {
		try {
			Method setter = findBugfenderMethod(methodName, 1);
			if (setter == null) {
				return;
			}
			setter.invoke(null, handler);
		} catch (Exception ignored) {
			// Optional API; ignore if unavailable.
		}
	}

	private static Method findBugfenderMethod(String name, int paramCount) {
		for (Method method : Bugfender.class.getMethods()) {
			if (name.equals(method.getName()) && method.getParameterTypes().length == paramCount) {
				return method;
			}
		}
		return null;
	}

	private Object createRequestObfuscationHandler() {
		Method setter = findBugfenderMethod("setNetworkLoggingRequestObfuscationHandler", 1);
		if (setter == null) {
			return null;
		}
		Class<?> handlerType = setter.getParameterTypes()[0];
		return Proxy.newProxyInstance(
			handlerType.getClassLoader(),
			new Class<?>[] { handlerType },
			(proxy, method, methodArgs) -> {
				if (method.getDeclaringClass() == Object.class) {
					return invokeObjectMethod(proxy, method, methodArgs);
				}
				if (methodArgs == null || methodArgs.length < 3) {
					return null;
				}
				String url = methodArgs[0] instanceof String ? (String) methodArgs[0] : "";
				@SuppressWarnings("unchecked")
				Map<String, String> headers = methodArgs[1] instanceof Map
					? (Map<String, String>) methodArgs[1]
					: new HashMap<>();
				String body = methodArgs[2] instanceof String ? (String) methodArgs[2] : null;

				JSONObject payload = new JSONObject();
				payload.put("url", url != null ? url : "");
				payload.put("headers", toJSONObject(headers));
				payload.put("body", body != null ? body : JSONObject.NULL);

				JSONObject response = invokeJsObfuscation(OBFUSCATE_REQUEST_EVENT, payload);
				String obfuscatedUrl = url;
				Map<String, String> obfuscatedHeaders = headers;
				String obfuscatedBody = body;
				if (response != null) {
					if (response.has("url") && !response.isNull("url")) {
						obfuscatedUrl = response.getString("url");
					}
					obfuscatedHeaders = headersFromJSONObject(response.optJSONObject("headers"));
					obfuscatedBody = null;
					if (response.has("body") && !response.isNull("body")) {
						obfuscatedBody = response.getString("body");
					}
				}
				return newNetworkData(
					method.getReturnType(),
					obfuscatedUrl,
					obfuscatedHeaders,
					obfuscatedBody
				);
			}
		);
	}

	private Object createResponseObfuscationHandler() {
		Method setter = findBugfenderMethod("setNetworkLoggingResponseObfuscationHandler", 1);
		if (setter == null) {
			return null;
		}
		Class<?> handlerType = setter.getParameterTypes()[0];
		return Proxy.newProxyInstance(
			handlerType.getClassLoader(),
			new Class<?>[] { handlerType },
			(proxy, method, methodArgs) -> {
				if (method.getDeclaringClass() == Object.class) {
					return invokeObjectMethod(proxy, method, methodArgs);
				}
				if (methodArgs == null || methodArgs.length < 2) {
					return null;
				}
				@SuppressWarnings("unchecked")
				Map<String, String> headers = methodArgs[0] instanceof Map
					? (Map<String, String>) methodArgs[0]
					: new HashMap<>();
				String body = methodArgs[1] instanceof String ? (String) methodArgs[1] : null;

				JSONObject payload = new JSONObject();
				payload.put("headers", toJSONObject(headers));
				payload.put("body", body != null ? body : JSONObject.NULL);

				JSONObject response = invokeJsObfuscation(OBFUSCATE_RESPONSE_EVENT, payload);
				Map<String, String> obfuscatedHeaders = headers;
				String obfuscatedBody = body;
				if (response != null) {
					obfuscatedHeaders = headersFromJSONObject(response.optJSONObject("headers"));
					obfuscatedBody = null;
					if (response.has("body") && !response.isNull("body")) {
						obfuscatedBody = response.getString("body");
					}
				}
				return newNetworkData(
					method.getReturnType(),
					null,
					obfuscatedHeaders,
					obfuscatedBody
				);
			}
		);
	}

	private static Object newNetworkData(
		Class<?> type,
		String url,
		Map<String, String> headers,
		String body
	) throws Exception {
		for (Constructor<?> constructor : type.getConstructors()) {
			Class<?>[] params = constructor.getParameterTypes();
			if (
				params.length == 3 &&
				params[0] == String.class &&
				Map.class.isAssignableFrom(params[1]) &&
				params[2] == String.class
			) {
				return constructor.newInstance(url, headers, body);
			}
			if (
				params.length == 2 &&
				Map.class.isAssignableFrom(params[0]) &&
				params[1] == String.class
			) {
				return constructor.newInstance(headers, body);
			}
		}
		return null;
	}

	private static Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
		String name = method.getName();
		if ("toString".equals(name)) {
			return "BugfenderNetworkObfuscationHandlerProxy";
		}
		if ("hashCode".equals(name)) {
			return System.identityHashCode(proxy);
		}
		if ("equals".equals(name)) {
			return proxy == (args != null && args.length > 0 ? args[0] : null);
		}
		return null;
	}

	private JSONObject invokeJsObfuscation(String eventName, JSONObject body) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			return null;
		}

		String requestId = UUID.randomUUID().toString();
		PendingObfuscation pending = new PendingObfuscation();
		pendingObfuscations.put(requestId, pending);
		try {
			body.put("requestId", requestId);
		} catch (JSONException ignored) {
			pendingObfuscations.remove(requestId);
			return null;
		}

		new Handler(Looper.getMainLooper()).post(() -> fireDocumentEvent(eventName, body));

		try {
			if (!pending.latch.await(3, TimeUnit.SECONDS)) {
				pendingObfuscations.remove(requestId);
				return null;
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			pendingObfuscations.remove(requestId);
			return null;
		}

		pendingObfuscations.remove(requestId);
		return pending.result.get();
	}

	private void fireDocumentEvent(String eventName, JSONObject data) {
		final String js = String.format(
			Locale.US,
			"cordova.fireDocumentEvent('%s', %s);",
			eventName,
			data.toString()
		);
		CordovaWebView view = this.webView;
		if (view == null) {
			return;
		}
		view.getEngine().evaluateJavascript(js, null);
	}

	private static JSONObject toJSONObject(Map<String, String> headers) {
		JSONObject map = new JSONObject();
		if (headers == null) {
			return map;
		}
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			if (entry.getKey() != null) {
				try {
					map.put(entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
				} catch (JSONException ignored) {
					// Skip invalid entries.
				}
			}
		}
		return map;
	}

	private static Map<String, String> headersFromJSONObject(JSONObject map) {
		Map<String, String> result = new HashMap<>();
		if (map == null) {
			return result;
		}
		Iterator<String> keys = map.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			try {
				if (map.isNull(key)) {
					result.put(key, "");
				} else {
					Object value = map.get(key);
					result.put(key, value != null ? String.valueOf(value) : "");
				}
			} catch (JSONException ignored) {
				result.put(key, "");
			}
		}
		return result;
	}

	private static List<String> toStringList(Object value) {
		if (value == null || value == JSONObject.NULL) {
			return null;
		}
		if (!(value instanceof JSONArray)) {
			return null;
		}
		JSONArray array = (JSONArray) value;
		List<String> list = new ArrayList<>();
		for (int i = 0; i < array.length(); i++) {
			Object entry = array.opt(i);
			if (entry != null && entry != JSONObject.NULL) {
				list.add(String.valueOf(entry));
			}
		}
		return list;
	}
}
