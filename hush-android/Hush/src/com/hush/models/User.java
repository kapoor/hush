package com.hush.models;


import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.hush.clients.FacebookClient;
import com.hush.utils.AsyncHelper;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

@ParseClassName("User")
public class User extends ParseUser implements AsyncHelper {
	
	private final static String TAG = User.class.getSimpleName();
	
	private Chat currentChat;
	
	// Construct a new user
	public User() {
		super();
		FacebookClient.fetchAndSetUserAttributes(this);
	}
	
	public void saveToParse() {
		getCurrentUser().saveEventually();
	}
	
	public String getName() {
		return getCurrentUser().getString("name");
	}
	
	public void putName(String inName) {
		getCurrentUser().put("name", inName);
	}
	
	public String getFacebookId() {
		return getCurrentUser().getString("facebookId");
	}
	
	public void putFacebookId(String inFacebookId) {
		getCurrentUser().put("facebookId", inFacebookId);
	}
	
	public boolean getIsNew() {
		return getCurrentUser().isNew();
	}
	
	
	// Chat APIs
	public ParseRelation<Chat> getChatsRelation() {
		return getCurrentUser().getRelation("chats");
	}
	
	public void fetchChatsFromParse(final AsyncHelper ah) {
		
		final List<Chat> chats = new ArrayList<Chat>();

		ParseQuery<Chat> query = getChatsRelation().getQuery();
		
		// Show chats expiring sooner on top. Deleted chats will automatically be at the bottom
		query.addAscendingOrder("createdAt");
		
		query.findInBackground(new FindCallback<Chat>() {
		    public void done(List<Chat> chatsResults, ParseException e) {
				if (e != null) {
					Log.d(TAG, e.getMessage());
					return;
				}
				
	    		for (Chat chat : chatsResults) {
	    			chat.getString("topic");
	    			chat.getString("type");
	    			chats.add(chat);
	    		} 

				// Inform the caller that the operation was completed, so they can query the results back
				ah.chatsFetched(chats);
		    }
		});
	}
	
	public void addChat(Chat chat) {
		getChatsRelation().add(chat);
	}
	
	public void removeChat(Chat chat) {
		getChatsRelation().remove(chat);
	}

	public void sendMessage(Chat chat) {
		getChatsRelation().add(chat);
	}

	public Chat getCurrentChat() {
		return currentChat;
	}

	public void setCurrentChat(Chat inCurrentChat) {
		currentChat = inCurrentChat;
	}
	
	@Override
	public void userAttributesFetched(String inName, String inFacebookId) {
		putName(inName);
		putFacebookId(inFacebookId);
		saveToParse();
	}
	
	@Override
	public void chatsFetched(List<Chat> chats) { }

	@Override
	public void chattersFetched(List<Chatter> chatters) { }

	@Override
	public void messagesFetched(List<Message> messages) { }

	@Override
	public void chatSaved(Chat chat) { }

}
