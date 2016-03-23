package com.hush.models;

import com.parse.ParseClassName;
import com.parse.ParseInstallation;
import com.parse.ParseUser;


@ParseClassName("Installation")
public class Installation extends ParseInstallation {
	
	//private final static String TAG = Installation.class.getSimpleName();
	
	// Default public constructor, needed by Parse
	public Installation() {
		super();
	}
	
	public static void addUserToInstallation() {
	    
	    // doing this on login, since an existing user can log in to multiple devices
        ParseInstallation installation = getCurrentInstallation();
        // Associate the device with a user
        installation.getRelation("user").add(ParseUser.getCurrentUser());
        installation.saveEventually();
	}

}

