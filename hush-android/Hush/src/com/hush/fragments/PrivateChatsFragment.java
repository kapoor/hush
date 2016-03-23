package com.hush.fragments;

import java.util.Locale;

import com.hush.utils.ConstantsAndUtils;

public class PrivateChatsFragment extends ChatListFragment {

	@Override
	protected String getChatListType() {
		return ConstantsAndUtils.ChatType.PRIVATE.toString().toLowerCase(Locale.ENGLISH);
	}
}
