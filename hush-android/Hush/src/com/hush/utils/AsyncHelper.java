package com.hush.utils;

import java.util.List;

import com.hush.models.Chat;
import com.hush.models.Chatter;
import com.hush.models.Message;

public interface AsyncHelper {
	public void userAttributesFetched(String inName, String inFacebookId);
	public void chatSaved(Chat chat);
	public void chatsFetched(List<Chat> chats);
	public void chattersFetched(List<Chatter> chatters);
	public void messagesFetched(List<Message> messages);
}
