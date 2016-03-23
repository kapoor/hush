package com.hush.models;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Message")
public class Message extends ParseObject {

	public Message() {
		super();
	}
	
	public Message(String content, String chatterFacebookId) {
		putContent(content);
		putChatterFacebookId(chatterFacebookId);
	}
	
	public void saveToParse() {
		saveEventually();
	}

	public String getContent() {
		return getString("content");
	}

	public void putContent(String content) {
		put("content", content);
	}

	public String getChatterFacebookId() {
		return getString("chatterFacebookId");
	}

	public void putChatterFacebookId(String chatterFacebookId) {
		put("chatterFacebookId", chatterFacebookId);
	}

	public String getFormattedTime(Context context) {
	    
	    // Since in the MessageAdapter, we we create new message objects and add them to the adapter before they are saved in the db, getCreatedAt()
	    // might return null for them and the adapter will then throw an NPE when showing their date. So create a new Date object for them  
	    Date createdAtDate = getCreatedAt();
        if(createdAtDate == null) {
            createdAtDate = new Date();
        }
        
	    return DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(createdAtDate);
	}
}

