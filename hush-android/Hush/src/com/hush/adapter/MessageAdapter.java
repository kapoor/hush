package com.hush.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hush.HushApp;
import com.hush.R;
import com.hush.models.Message;

public class MessageAdapter extends ArrayAdapter<Message> {
	
	Context activityContext;
	
	private static enum MessageType {
	    MY_MESSAGE(0), OTHER_MESSAGE(1);
	    
	    private final int value;
        private MessageType(int inValue) {
            value = inValue;
        }

        public int getValue() {
            return value;
        }
	};
	
	// View lookup cache
	private static class ViewHolder {
		TextView content;
		TextView timePosted;
	}
	
	public MessageAdapter(Context context, ArrayList<Message> messages) {
		super(context, 0, messages);
		activityContext = context;
	}
	
    @Override
    public int getViewTypeCount() {
       // Returns the number of types of Views that will be created by getView(int, View, ViewGroup)
       // Each type represents a set of views that can be converted
    	return 2;
    }
    
    // Get the type of View that will be created by getView(int, View, ViewGroup) for the specified item.
    @Override
    public int getItemViewType(int position) {
       // Return an integer here representing the type of View.
       // Note: Integers must be in the range 0 to getViewTypeCount() - 1
    	Message message = getItem(position);
		if (message.getChatterFacebookId().equals(HushApp.getCurrentUser().getFacebookId())) {
			return MessageType.MY_MESSAGE.getValue();
		} else {
			return MessageType.OTHER_MESSAGE.getValue();
		}
    }
    
    @Override
    public boolean isEnabled(int position) {
    	return false;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Message message = getItem(position);

		// View lookup cache stored in tag
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			// Get the data item type for this position
			int type = getItemViewType(position);
			if(type == MessageType.MY_MESSAGE.getValue()) {
				convertView = inflater.inflate(R.layout.message_item, null);
			} else {
				convertView = inflater.inflate(R.layout.message_item_other, null);
			}
			
			viewHolder.content = (TextView) convertView.findViewById(R.id.tvContent);
			viewHolder.timePosted = (TextView) convertView.findViewById(R.id.tvTimePosted);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.content.setText(message.getContent());
		viewHolder.timePosted.setText(message.getFormattedTime(getContext()));	 

		return convertView;
	}
}
