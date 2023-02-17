package be.cenzo.hermes.ui.chat;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import be.cenzo.hermes.R;

public class MessageAdapter extends ArrayAdapter<Messaggio> {

    private LayoutInflater inflater;

    public MessageAdapter(Context context, int resourceId, List<Messaggio> objects) {
        super(context, resourceId, objects);
        //resource = resourceId;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null) {
            Log.d("DEBUG","Inflating view");
            v = inflater.inflate(R.layout.chat_message, null);
        }

        Messaggio message = getItem(position);

        TextView contentTextView = (TextView) v.findViewById(R.id.chatMessageContent);
        TextView displayNameTextView = (TextView) v.findViewById(R.id.chatMessageDisplayName);
        LinearLayout messageContainer = (LinearLayout)  v.findViewById(R.id.messageContainer);
        RelativeLayout messageListAdapter = (RelativeLayout) v.findViewById(R.id.messageListAdapter);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) messageContainer.getLayoutParams();
        //params.a

        contentTextView.setText(message.getContenuto());
        displayNameTextView.setText(message.getDisplayName());
        if(message.isInviato())
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        else
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);


        messageContainer.setLayoutParams(params);
        Log.d("Messaggio", " " + message.isInviato());

        return v;
    }

}
