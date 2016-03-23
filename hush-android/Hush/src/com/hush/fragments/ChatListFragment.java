package com.hush.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hush.HushApp;
import com.hush.R;
import com.hush.activities.ChatWindowActivity;
import com.hush.adapter.ChatAdapter;
import com.hush.models.Chat;
import com.hush.models.Chatter;
import com.hush.models.Message;
import com.hush.utils.AsyncHelper;
import com.hush.utils.ConstantsAndUtils;
import com.hush.utils.PushNotifReceiver;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public abstract class ChatListFragment extends Fragment implements AsyncHelper { 

	private PullToRefreshListView lvChats;

    private String previousChatType;
    
	//protected ProgressBar progressBarLoadingChats;
    private int remainingRequests = 0;
	private Dialog progressDialog;

	private ChatAdapter adapter;
    private BroadcastReceiver pushNotifReceiver;
	
	private List<Chat> chats;
	
    protected abstract String getChatListType();

    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Create the broadcast receiver object
        pushNotifReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            	updateChatsAdapterFromDisk();
            }
        };
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Defines the xml file for the fragment
		View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

		// Setup handles to view objects here
		//progressBarLoadingChats = (ProgressBar) view.findViewById(R.id.pgbarChatListFragment);
		lvChats = (PullToRefreshListView) view.findViewById(R.id.lvChatListFragmentChatsList);	
		adapter = new ChatAdapter(getActivity(), new ArrayList<Chat>());
		lvChats.setAdapter(adapter);

		setupListeners();

		return view;
	}
	
	/**
	 * Add a scroll and pull to refresh listener to the chats list view
	 */
	private void setupListeners() {
		// Set a listener to be invoked when the list should be refreshed.
		lvChats.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				// Your code to refresh the list contents
				// Make sure you call listView.onRefreshComplete()
				// once the loading is done. This can be done from here or any
				// place such as when the network request has completed successfully.
			    
		        // Moved over from onActivityCreated()
		        remainingRequests--;
		        progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading_chats), true);
				HushApp.getCurrentUser().fetchChatsFromParse(ChatListFragment.this);
				
				// Now we call onRefreshComplete to signify refresh has finished
				lvChats.onRefreshComplete();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// Register as broadcast receiver
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(pushNotifReceiver, new IntentFilter(ConstantsAndUtils.broadcastLocalMessageAction));
        
        updateChatsAdapterFromDisk();
        
        // Moved over from onActivityCreated()
        remainingRequests--;
        progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading_chats), true);
        HushApp.getCurrentUser().fetchChatsFromParse(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		 // Unregister as broadcast receiver
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(pushNotifReceiver);
	}
	
	public ChatAdapter getAdapter() {
		return adapter;
	}
	
	protected void loadChatsIntoAdapter() {
		String chatType = getChatListType();

		// If we have switched from public to private or vice versa, clear out the adapter before adding chats to it
		if(!chatType.equals(previousChatType)) {
			adapter.clear();
		}
		
		//make a call to get the current user's chats based on the type
		if(chats != null && chats.size() > 0) {

			ArrayList<Chat> chatsToShow = new ArrayList<Chat>();
			for(Chat c : chats) {
				if(c.getType().equals(chatType))
					chatsToShow.add(c);
			}
			
			adapter.addAll(chatsToShow);
			
			//set the progress bar to invisible
			//progressBarLoadingChats.setVisibility(ProgressBar.INVISIBLE);
			progressDialog.dismiss();
		}
		/*
		else if(exception != null) {
			//we have a parse exception, alert the user
			Toast.makeText(getActivity(), getString(R.string.chat_list_parse_exception), Toast.LENGTH_SHORT).show();
		}
		*/
	}
	
	private void updateChatsAdapterFromDisk() {
		// Read the unread items from disk
		ArrayList<String> notifs = ConstantsAndUtils.readFromFile(getActivity());
		
		// There is no file to process, everything has been processed already
		if (notifs.size() == 0) {
			return;
		}
		
		ConstantsAndUtils.deleteFileAfterReading(getActivity());
		
		for(final String notifMsg : notifs) {
			
			//  "chatId" + "|" + getObjectId() + "|" + pushMessage;

			String[] notifParts = notifMsg.split("\\|");
			
			ParseQuery<Chat> query = ParseQuery.getQuery(Chat.class);
			query.getInBackground(notifParts[2], new GetCallback<Chat>() {
				public void done(Chat chat, ParseException e) {
					if (e != null) {
						Log.d("TAG", e.getMessage());
						return;
					}
					
					if(notifMsg.startsWith(PushNotifReceiver.PushType.NEW_CHAT.toString())) {
						chat.setRead(false);
						adapter.add(chat);
						// Mark that chat as unread
					}
					else if(notifMsg.startsWith(PushNotifReceiver.PushType.NEW_MESSAGE.toString())) {
						// Mark the chat as unread
						chat.setRead(false);
					} else {
						// This should not happen
						Log.d("TAG", "Found notifLine: " + notifMsg );
					}
				}
			});
		}
	}
	
    private void dismissProgressDialog() {
        if (++remainingRequests == 0) {
            progressDialog.dismiss();
        }
    }
	
	@Override
	public void chatsFetched(List<Chat> inChats) {
	    dismissProgressDialog();
		chats = inChats;
		loadChatsIntoAdapter();
	}
	
	@Override
	public void chattersFetched(List<Chatter> chatters) { }

	@Override
	public void messagesFetched(List<Message> messages) { }
	
	@Override
	public void userAttributesFetched(String inName, String inFacebookId) {}
	
	@Override
	public void chatSaved(Chat chat) { }

}
