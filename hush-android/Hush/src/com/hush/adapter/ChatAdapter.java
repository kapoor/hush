package com.hush.adapter;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eevoskos.robotoviews.Roboto;
import com.hush.HushApp;
import com.hush.R;
import com.hush.activities.ChatWindowActivity;
import com.hush.models.Chat;

public class ChatAdapter extends ArrayAdapter<Chat> {

	private static final long MILLISEC_PER_DAY = 24 * 60 * 60 * 1000;
	private static final long SEC_PER_HOUR = 60 * 60;
	private String expirationTimeStr;
	private int color;
	private Resources resources;

	// View lookup cache
	private static class ViewHolder {
		TextView topic;
		TextView createdAt;
	}

	public ChatAdapter(Context context, ArrayList<Chat> chats) {
		super(context, 0, chats);
		resources = context.getResources();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final ViewHolder viewHolder; // view lookup cache stored in tag
		if (convertView == null) {
			viewHolder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.chat_item, null);
			viewHolder.topic = (TextView) convertView.findViewById(R.id.tvChatItemChatTopic);
			viewHolder.createdAt = (TextView) convertView.findViewById(R.id.tvExpirationTime);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final View view = convertView;
		final Chat chat = getItem(position);
		
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				HushApp.getCurrentUser().setCurrentChat(chat);
				chat.setRead(true);
				Intent intent = new Intent(getContext(), ChatWindowActivity.class);
				
				Activity parentActivity = (Activity) getContext();
				parentActivity.startActivity(intent);
				parentActivity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
			}
		});

		
		viewHolder.topic.setText(chat.getTopic());

		//if the chat is not read, make it gray and bold the text
		 if(!chat.isRead()) {
			view.setBackgroundColor(resources.getColor(R.color.light_purple)); 
			viewHolder.topic.setTypeface(Roboto.getInstance().getTypeface(Roboto.ROBOTO_REGULAR));
		 }

		
		if(chat.getCreatedAt().getTime() + MILLISEC_PER_DAY < (new Date()).getTime()) {
			//this chat has expired.. so no need to add countdown timer
			//ideally this should be removed from the db
			setExpiredChatDisabled(viewHolder, view);	
		}
		else {
			long expirationTimeInMillisec = chat.getCreatedAt().getTime() + MILLISEC_PER_DAY  - (new Date()).getTime();
			
			//for testing use values like 70000(70s) or 15000(15s)
			new CountDownTimer(expirationTimeInMillisec, 1000) {

				public void onTick(long millisUntilFinished) {

					long secondsUntilFinished = millisUntilFinished/1000;

					if(secondsUntilFinished > 0 && secondsUntilFinished < 60){
						color = resources.getColor(R.color.red);
						expirationTimeStr =  String.valueOf(secondsUntilFinished) + "s";
					}
					else if (secondsUntilFinished >= 60 && secondsUntilFinished < (SEC_PER_HOUR)) {
						long minUntilFinished = secondsUntilFinished / 60;
						color = resources.getColor(R.color.orange);
						expirationTimeStr = String.valueOf(minUntilFinished) + "m";
					}
					else if(secondsUntilFinished >= (SEC_PER_HOUR)) {
						long hourUntilFinished = secondsUntilFinished/ SEC_PER_HOUR;
						color = resources.getColor(R.color.green);
						expirationTimeStr = String.valueOf(hourUntilFinished) + "h";
					}

					viewHolder.createdAt.setText(expirationTimeStr);
					viewHolder.createdAt.setTextColor(color);

				}

				public void onFinish() {				
					//the user should refresh and this chat should be moved to the bottom eventually. 
					setExpiredChatDisabled(viewHolder, view);
				}
			}.start();
		}
		
		

		return convertView;
	}
	
	/**
	 * For now just make the background color light purple and make it non-clickable
	 * @param holder
	 * @param view
	 */
	private void setExpiredChatDisabled(ViewHolder holder, View view){
		holder.createdAt.setText("0s");
		holder.createdAt.setTextColor(color);
		view.setBackgroundColor(resources.getColor(R.color.gray));
		view.setOnClickListener(null);
		view.setClickable(false);
	}

}
