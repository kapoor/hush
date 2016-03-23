package com.hush.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.facebook.Session;
import com.hush.R;
import com.hush.activities.HushLoginActivity;
import com.hush.activities.PickFriendsActivity;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class ConstantsAndUtils {

    public static String externalPushNotifAction = "com.hush.HUSH_EXTERNAL_PUSH_NOTIF";
    public static String broadcastLocalMessageAction = "com.hush.HUSH_BBROADCAST_LOCAL_MESSAGE";
    public static final int maxMessagesToFetchAndShowInChat = 50;

    public static enum ChatType {PRIVATE, PUBLIC};
    
    public static String hushNotifsFile = "hush_notifs.txt";
    
    public static enum ActivityName {
        PICK_FRIENDS_ACTIVITY(1);
        
        private final int value;
        private ActivityName(int inValue) {
            value = inValue;
        }

        public int getValue() {
            return value;
        }
    };
    
    // Ensure that the user is logged in
    public static void ensureLogin(final Activity currentActivity) {
        ParseUser currentUser = ParseUser.getCurrentUser();

        if ((currentUser == null) || !ParseFacebookUtils.isLinked(currentUser) 
            || Session.getActiveSession() == null || !Session.getActiveSession().isOpened()) {

            // Navigate user to login
            Intent i =  new Intent(currentActivity, HushLoginActivity.class);
            currentActivity.startActivity(i);
        }
    }
    
	public static void writeToFile(Context context, String data) {
	    try {
	        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(hushNotifsFile, Context.MODE_PRIVATE));
	        outputStreamWriter.write(data);
	        outputStreamWriter.close();
	    }
	    catch (IOException e) {
	        Log.e("Exception", "File write failed: " + e.toString());
	    } 
	}

	public static void deleteFileAfterReading(Context context) {
        context.deleteFile(hushNotifsFile);
	}
	
	public static ArrayList<String> readFromFile(Context context) {

	    ArrayList<String> lines = new ArrayList<String>();

	    try {
	        InputStream inputStream = context.openFileInput(hushNotifsFile);

	        if ( inputStream != null ) {
	            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	            String receiveString = "";

	            while ( (receiveString = bufferedReader.readLine()) != null ) {
	                lines.add(receiveString);
	            }

	            inputStream.close();
	        }
	    }
	    catch (FileNotFoundException e) {
	        Log.e("TAG", "File not found: " + e.toString());
	    } catch (IOException e) {
	        Log.e("TAG", "Can not read file: " + e.toString());
	    }

	    return lines;
	}

	public static void startPickFriendsActivity(Activity activity) {
        Intent intent = new Intent(activity, PickFriendsActivity.class);

        // Note: The following line is optional, as multi-select behavior is the default for
        // FriendPickerFragment. It is here to demonstrate how parameters could be passed to the
        // friend picker if single-select functionality was desired, or if a different user ID was
        // desired (for instance, to see friends of a friend).
        PickFriendsActivity.populateParameters(intent, null, true, true);
        activity.startActivityForResult(intent, ActivityName.PICK_FRIENDS_ACTIVITY.getValue());
        activity.overridePendingTransition(R.anim.slide_in_bottom, android.R.anim.fade_out);
	}
}
