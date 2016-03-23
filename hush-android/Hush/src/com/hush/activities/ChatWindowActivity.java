package com.hush.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.model.GraphUser;
import com.hush.HushApp;
import com.hush.R;
import com.hush.adapter.MessageAdapter;
import com.hush.fragments.SimpleAlertDialog;
import com.hush.fragments.SimpleAlertDialog.SimpleAlertListener;
import com.hush.models.Chat;
import com.hush.models.Chatter;
import com.hush.models.Message;
import com.hush.utils.AsyncHelper;
import com.hush.utils.ConstantsAndUtils;
import com.hush.utils.PushNotifReceiver;

public class ChatWindowActivity extends FragmentActivity implements AsyncHelper {
	
	private int numFriendsSelected;
	
	private TextView tvChatTopic;
	private ListView lvMessages;
	private EditText etChatWindowMessage;
	private MessageAdapter adapterMessages;
	private Button btnChatWindowSend;
	private Menu menu;
	
	private Dialog progressDialog;
	private int remainingRequests = 0;

	private Chat chat;
	private List<Chatter> chatters;
	private ArrayList<String> chatterFacebookIds;
	
	private BroadcastReceiver pushNotifReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Ensure that the user is logged in
        ConstantsAndUtils.ensureLogin(this);

		setContentView(R.layout.activity_chat_window);

		tvChatTopic = (TextView) findViewById(R.id.tvChatTopic);
		etChatWindowMessage = ((EditText) findViewById(R.id.etChatWindowMessage));
		lvMessages = (ListView) findViewById(R.id.lvChatWindowMessages);
		btnChatWindowSend = (Button) findViewById(R.id.btnChatWindowSend);
        adapterMessages = new MessageAdapter(this, new ArrayList<Message>());
        lvMessages.setAdapter(adapterMessages);
        
        numFriendsSelected = 0;
        btnChatWindowSend.setEnabled(false);
        
    	pushNotifReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            	
            	updateMessagesAdapterFromDisk();
            }
        };
        
        configureChatWindowMessage();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		chat = HushApp.getCurrentUser().getCurrentChat();
		
		// Fetch chatters from parse
		remainingRequests--;
		//progressDialog = ProgressDialog.show(ChatWindowActivity.this, "", getString(R.string.loading_chatters), true);
		chat.fetchChattersFromParse(this);

        // Fetch messages from parse
        remainingRequests--;
        //progressDialog = ProgressDialog.show(ChatWindowActivity.this, "", getString(R.string.loading_messages), true);
		chat.fetchMessagesFromParse(ConstantsAndUtils.maxMessagesToFetchAndShowInChat, this);
		
        tvChatTopic.setText(chat.getTopic());
        
        // Register as broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(pushNotifReceiver, new IntentFilter(ConstantsAndUtils.broadcastLocalMessageAction));

        updateMessagesAdapterFromDisk();
	}
	
	@Override
	public void onPause() {
		super.onPause();

		// Unregister as broadcast receiver
		LocalBroadcastManager.getInstance(this).unregisterReceiver(pushNotifReceiver);
	}
	
	@Override
    public void onBackPressed() {
		Intent i = new Intent(ChatWindowActivity.this, ChatsListActivity.class);
		startActivity(i);
		overridePendingTransition(R.anim.right_out, R.anim.left_in);
    }

	 @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case android.R.id.home:
                onBackPressed(); 
         }
         return true;
     }
	
	private void updateMessagesAdapterFromDisk() {
		// Read the unread items from disk
		ArrayList<String> notifs = ConstantsAndUtils.readFromFile(ChatWindowActivity.this);
		
		// There is no file to process, everything has been processed already
		if (notifs.size() == 0) {
			return;
		}

		ConstantsAndUtils.deleteFileAfterReading(ChatWindowActivity.this);
		
		for(String notifMsg : notifs) {
			if(notifMsg.startsWith(PushNotifReceiver.PushType.NEW_MESSAGE.toString())) {
				String[] notifParts = notifMsg.split("\\|");
				if(notifParts[2].equals(chat.getObjectId())) {
					adapterMessages.add(new Message(notifParts[3], "-1"));
				} else {
					ConstantsAndUtils.writeToFile(ChatWindowActivity.this, notifMsg);
				}
			} else {
				ConstantsAndUtils.writeToFile(ChatWindowActivity.this, notifMsg);
			}
		}
	}
	
	private void dismissProgressDialog() {
	    if (++remainingRequests == 0) {
	        progressDialog.dismiss();
	    }
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat_window, menu);
		
		this.menu = menu;
		
        if(chat.getType().equals("public")) {
        	MenuItem inviteFriendsItem = menu.getItem(1);
        	inviteFriendsItem.setVisible(true);
        }
		
		return true;
	}

	private void setChatterFacebookIds() {
        if(chatterFacebookIds == null) {
        	chatterFacebookIds = new ArrayList<String>();
        }
        
        for(Chatter chatter: chatters) {
        	// Send the notif to everyone except yourself
        	if (!chatter.getFacebookId().equals(HushApp.getCurrentUser().getFacebookId())) {
        		chatterFacebookIds.add(chatter.getFacebookId());
        	}
        }
	}

	private ArrayList<String> getChatterFacebookIds() {
        return chatterFacebookIds;
	}
	
	// menu actions
	public void onInviteFriendsClick(MenuItem mi) {
	    ConstantsAndUtils.startPickFriendsActivity(this);
	}

	public void onLeaveChatClick(MenuItem mi) {
		showLeaveChatConfirmationDialog();
	}

	public void onSendClicked(View v) {
	    
	    // Disable the send button
        btnChatWindowSend.setEnabled(false);
        
        // Play a sound
        MediaPlayer.create(ChatWindowActivity.this, R.raw.send_message).start();

		String content = etChatWindowMessage.getText().toString();
		
		Message message = new Message(content, HushApp.getCurrentUser().getFacebookId());
    	message.saveToParse();
    	
    	chat.addMessage(message);
    	
    	// Save to parse and send a push notification
        chat.saveToParseWithPush(this, PushNotifReceiver.PushType.NEW_MESSAGE.toString(), chat.getTopic() + "|" + message.getContent(), getChatterFacebookIds());
        
        adapterMessages.add(message);
        
    	etChatWindowMessage.setText("");
	}
	
	@Override
	public void chattersFetched(List<Chatter> inChatters) {
	    //dismissProgressDialog();
	    
		chatters = inChatters;
		
		int numParticipants = chatters.size();
		//configureNumParticipantsMenuItem(numParticipants + numFriendsSelected);
	    
		setChatterFacebookIds();
	}

	@Override
	public void messagesFetched(List<Message> inMessages) {
        //dismissProgressDialog();

		adapterMessages.addAll(inMessages);
	}

	@Override
	public void chatsFetched(List<Chat> chats) { }

	@Override
	public void userAttributesFetched(String inName, String inFacebookId) {	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == ConstantsAndUtils.ActivityName.PICK_FRIENDS_ACTIVITY.getValue()) {
			displayFriendCount();       
		}
	}
	
	private void displayFriendCount() {
		Collection<GraphUser> selectedFriends = HushApp.getSelectedUsers();
		
		if (selectedFriends == null || selectedFriends.size() == 0) { return; }
		numFriendsSelected = selectedFriends.size();
		
		Chatter chatter;
		final ArrayList<String> fbChatterIds = new ArrayList<String>();
		Collection<GraphUser> selection = HushApp.getSelectedUsers();
		for (GraphUser user : selection) {
			chatter = new Chatter(user.getId(), user.getName());
			fbChatterIds.add(user.getId());
			chatter.saveToParse();
			chat.addChatter(chatter);
		}
		
		// Add chatters to chat and send push notifs to them
    	chat.saveToParseWithPush(this, PushNotifReceiver.PushType.NEW_CHAT.toString(), getString(R.string.new_chat_push_notif_message), fbChatterIds);
	}
	
	private void showLeaveChatConfirmationDialog() {
        SimpleAlertDialog.build(this, 
            getString(R.string.chat_window_leave_prompt), getString(R.string.yes), getString(R.string.no), new SimpleAlertListener() {
            
                @Override
                public void onPositive() {
            		HushApp.getCurrentUser().removeChat(chat);
            		HushApp.getCurrentUser().saveToParse();
            		
            		Intent i = new Intent(ChatWindowActivity.this, ChatsListActivity.class);
            		startActivity(i);
            		overridePendingTransition(R.anim.right_out, R.anim.left_in);
                }

                @Override
                public void onNegative() {
                    // handle cancel
                }
        }).show();
	}
	
	private void configureChatWindowMessage() {
        etChatWindowMessage.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(etChatWindowMessage.getText().toString().trim().length() > 0) {
					btnChatWindowSend.setEnabled(true);
				} else {
				    btnChatWindowSend.setEnabled(false);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			
			@Override
			public void afterTextChanged(Editable s) { }
		});
	}
	
	private void configureNumParticipantsMenuItem(int chattersCount) {
		MenuItem numParticipantsItem = this.menu.getItem(0);
		String numParticipants = "(" + chattersCount + ")";				
		numParticipantsItem.setActionView(R.layout.num_participants);
		View numParticipantsActionView = numParticipantsItem.getActionView();
		TextView tvNumParticipants = (TextView) numParticipantsActionView.findViewById(R.id.tvNumParticipants);
		tvNumParticipants.setText(numParticipants);
		numParticipantsItem.setVisible(true);
	}
	
	@Override
	public void chatSaved(Chat chat) { }

}
