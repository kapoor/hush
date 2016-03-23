package com.hush.fragments;

import java.util.Locale;

import com.hush.utils.ConstantsAndUtils;

public class PublicChatsFragment extends ChatListFragment{
	
	@Override
	protected String getChatListType() {
		return ConstantsAndUtils.ChatType.PUBLIC.toString().toLowerCase(Locale.ENGLISH);
	}

}
