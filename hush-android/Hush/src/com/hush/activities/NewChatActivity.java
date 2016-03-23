package com.hush.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.facebook.model.GraphUser;
import com.hush.HushApp;
import com.hush.R;
import com.hush.models.Chat;
import com.hush.models.Chatter;
import com.hush.models.Message;
import com.hush.utils.AsyncHelper;
import com.hush.utils.ConstantsAndUtils;
import com.hush.utils.PushNotifReceiver;

public class NewChatActivity extends Activity implements AsyncHelper {

	private EditText etChatTopic;
	private TextView tvFriendCount;
	private Switch swPublicPrivate;
	private MenuItem miDone;
	
	private boolean friendsPicked = false;
	private boolean chatTopicEntered = false;
	
	private Dialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Ensure that the user is logged in
        ConstantsAndUtils.ensureLogin(this);

		setContentView(R.layout.activity_new_chat);

		// Populate view variables
		etChatTopic = (EditText) findViewById(R.id.etChatTopic);
		tvFriendCount = (TextView) findViewById(R.id.tvFriendCount);
		swPublicPrivate = (Switch) findViewById(R.id.swPublicPrivate);

		setupTextViewListener();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_chat_topic, menu);
		miDone = menu.findItem(R.id.action_invite_friends);
		return true;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ConstantsAndUtils.ActivityName.PICK_FRIENDS_ACTIVITY.getValue()) {
			displayFriendCount();
		}
	}

	// actions
	public void onInviteFriendsClick(View view) {
		ConstantsAndUtils.startPickFriendsActivity(this);
	}

	public void onDoneClick(MenuItem mi) {
		mi.setEnabled(false);
		// Create chat and chatters objects in parse
		String chatType = swPublicPrivate.isChecked() ? ConstantsAndUtils.ChatType.PRIVATE.toString().toLowerCase(Locale.ENGLISH) : ConstantsAndUtils.ChatType.PUBLIC.toString().toLowerCase(Locale.ENGLISH);
		Chat chat = new Chat(etChatTopic.getText().toString(), chatType);
		chat.saveToParse();

		Chatter chatter;
		final ArrayList<String> fbChatterIds = new ArrayList<String>();
		Collection<GraphUser> selection = HushApp.getSelectedUsers();
		for (GraphUser user : selection) {
			chatter = new Chatter(user.getId(), user.getName());
			fbChatterIds.add(user.getId());
			chatter.saveToParse();
			chat.addChatter(chatter);
		}

		// Add the original user to the chat
		chatter = new Chatter(HushApp.getCurrentUser().getFacebookId(), HushApp.getCurrentUser().getName());
		chatter.saveToParse();
		chat.addChatter(chatter);

		// Save chat to parse and send push notification to the chatters
		progressDialog = ProgressDialog.show(NewChatActivity.this, "", getString(R.string.saving_chat), true);
    	chat.saveToParseWithPush(this, PushNotifReceiver.PushType.NEW_CHAT.toString(), getString(R.string.new_chat_push_notif_message), fbChatterIds);
	}

	private void displayFriendCount() {
		Collection<GraphUser> selectedFriends = HushApp.getSelectedUsers();

		if (selectedFriends == null || selectedFriends.size() == 0) {
		    friendsPicked = false;
	        tvFriendCount.setText("(0)");
		} else {
	        friendsPicked = true;
		    tvFriendCount.setText("(" + (selectedFriends.size() + 1) + ")");
		}
		
		tvFriendCount.setTextColor(getResources().getColor(R.color.light_olive_green));
		enableOrDisableDoneButton();
	}

	private void setupTextViewListener() {

		TextWatcher watcher = new TextWatcher() {		
			
			// Enable the done menu only when there is a topic AND at least one friend is selected 
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
                String topic = etChatTopic.getText().toString();
                String selectedFriendsCount = tvFriendCount.getText().toString();
                if( topic != null && !topic.trim().isEmpty() && selectedFriendsCount != null && Integer.valueOf(selectedFriendsCount.substring(1, 2)) > 0) {
                    chatTopicEntered = true;
                } else {
                    chatTopicEntered = false;
                }
                enableOrDisableDoneButton();
            }

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void afterTextChanged(Editable s) {
			}
		};
		
		etChatTopic.addTextChangedListener(watcher);
		tvFriendCount.addTextChangedListener(watcher);
	}
	
    private void enableOrDisableDoneButton() {
        miDone.setEnabled(friendsPicked && chatTopicEntered);
    }	    

	@Override
	public void userAttributesFetched(String inName, String inFacebookId) {	}

	@Override
	public void chatSaved(Chat chat) {
	    progressDialog.dismiss();
	    
		// Add the chat to user's chats 
		HushApp.getCurrentUser().addChat(chat);
		HushApp.getCurrentUser().saveToParse();

		// Set active chat and navigate to a chat window
		HushApp.getCurrentUser().setCurrentChat(chat);
		Intent i = new Intent(NewChatActivity.this, ChatWindowActivity.class);
		startActivity(i);
	}

	@Override
	public void chatsFetched(List<Chat> chats) { }

	@Override
	public void chattersFetched(List<Chatter> chatters) { }

	@Override
	public void messagesFetched(List<Message> messages) { }
}
