package com.hush.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Chatter")
public class Chatter extends ParseObject {

	// Default constructor needed by parse
	public Chatter() {
		super();
	}

	public Chatter(String facebookId, String name) {
		putFacebookId(facebookId);
		putName(name);
	}

	public void saveToParse() {
		saveEventually();
	}

	public String getFacebookId() {
		return getString("facebookId");
	}

	public void putFacebookId(String facebookId) {
		put("facebookId", facebookId);
	}

	public String getName() {
		return getString("name");
	}

	public void putName(String name) {
		put("name", name);
	}
}
