package com.hush.utils;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class PushNotifSender {

	public static void sendPushNotifToUsers(List<ParseUser> parseUsers, final String pushType, final String customData) {

		// The fetched Users are linked to the Installation table, so send
		// notifications to the devices of those users
		ParseQuery<ParseInstallation> userQuery = ParseInstallation.getQuery();
		userQuery.whereContainedIn("user", parseUsers);
		userQuery.whereEqualTo("deviceType", "android");

		JSONObject obj = null;
		try {
			obj = new JSONObject();
			obj.put("action", ConstantsAndUtils.externalPushNotifAction);
			obj.put("customData", pushType.toString() + "|" + customData);
		} catch (JSONException je) {
			je.printStackTrace();
		}

		ParsePush push = new ParsePush();
		push.setQuery(userQuery);
		push.setData(obj);
		push.sendInBackground();
	}
}
