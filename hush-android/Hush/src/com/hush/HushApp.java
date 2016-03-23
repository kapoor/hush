package com.hush;

import java.util.List;

import android.app.Application;

import com.facebook.model.GraphUser;
import com.hush.activities.ChatsListActivity;
import com.hush.models.Chat;
import com.hush.models.Chatter;
import com.hush.models.Message;
import com.hush.models.User;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.PushService;

public class HushApp extends Application {
	
    private static User currentUser;
    private static List<GraphUser> selectedUsers;
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static void setCurrentUser(User inCurrentUser) {
        currentUser = inCurrentUser;
    }
    
    public static List<GraphUser> getSelectedUsers() {
        return selectedUsers;
    }
    
    public static void setSelectedUsers(List<GraphUser> inSelectedUsers) {
        selectedUsers = inSelectedUsers;
    }
    
    @Override
    public void onCreate() {
    	super.onCreate();
    	
    	ParseObject.registerSubclass(User.class);
    	ParseObject.registerSubclass(Chat.class);
    	ParseObject.registerSubclass(Chatter.class);
    	ParseObject.registerSubclass(Message.class);

    	Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
    	ParseFacebookUtils.initialize(getString(R.string.facebook_app_id));
    	PushService.setDefaultPushCallback(this, ChatsListActivity.class);
        //ParseInstallation.getCurrentInstallation().saveInBackground();  
    }
}
