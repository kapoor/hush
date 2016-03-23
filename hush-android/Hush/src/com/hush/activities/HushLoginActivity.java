package com.hush.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.hush.HushApp;
import com.hush.R;
import com.hush.models.Installation;
import com.hush.models.User;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;


public class HushLoginActivity extends Activity {
	
	private static final String TAG = HushLoginActivity.class.getSimpleName();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }


	@Override
	public void onResume() {
        super.onResume();

        // Auto login user: If there is an auth token for the user and the user is linked to a Facebook account then automatically
        // log them in
        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
            
            // Set the user globally in HushApp
            HushApp.setCurrentUser(new User());
            showChatsListActivity();
        }
	}
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}
	
    
    public void login(View v) {

        // List<String> permissions = Arrays.asList("basic_info", "user_about_me", "user_birthday", "user_location");
        // ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {

		ParseFacebookUtils.logIn(null, this, new LogInCallback() {
		    
			@Override
			public void done(ParseUser user, ParseException e) {

				if (e != null) {
					Log.d(TAG, e.getMessage());
					return;
				}
				
				if (user == null) {
					Log.d(TAG, "Login cancelled");
					return;
				} else if (user.isNew()) {
				    Log.d(TAG, "New user logged in successfully");
				    
                    // Save the user to parse installation
                    Installation.addUserToInstallation();
                    
                    // Set the user globally in HushApp
                    HushApp.setCurrentUser(new User());
                    
                    // Show the FTUE tutorial
                    
				}
				
				Log.d(TAG, "Logged in successfully");

				// Set the user globally in HushApp
				HushApp.setCurrentUser(new User());
                
                showChatsListActivity();
			}
		});
    }
    

    private void showChatsListActivity() {
		// Navigate user to chat lists
		Intent i = new Intent(HushLoginActivity.this, ChatsListActivity.class);
		startActivity(i);
    }
}
