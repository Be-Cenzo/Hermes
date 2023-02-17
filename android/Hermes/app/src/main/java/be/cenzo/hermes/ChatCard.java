package be.cenzo.hermes;

import static be.cenzo.hermes.ui.rooms.ApplicationConstants.APPLICATION_ID;
import static be.cenzo.hermes.ui.rooms.ApplicationConstants.SDK_NAME;
import static be.cenzo.hermes.ui.rooms.ApplicationConstants.SDK_VERSION;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.azure.android.communication.chat.ChatAsyncClient;
import com.azure.android.communication.chat.ChatThreadAsyncClient;
import com.azure.android.communication.chat.ChatThreadClientBuilder;
import com.azure.android.communication.chat.models.ChatEvent;
import com.azure.android.communication.chat.models.ChatEventType;
import com.azure.android.communication.chat.models.ChatMessageReceivedEvent;
import com.azure.android.communication.chat.models.ChatMessageType;
import com.azure.android.communication.chat.models.SendChatMessageOptions;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.core.http.policy.UserAgentPolicy;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

import be.cenzo.hermes.ui.ProfileController;
import be.cenzo.hermes.ui.chat.MessageAdapter;
import be.cenzo.hermes.ui.chat.Messaggio;
import be.cenzo.hermes.ui.rooms.MapViewModel;
import be.cenzo.hermes.ui.rooms.Room;

public class ChatCard {

    private MapViewModel mapViewModel;
    private ProfileController profileController;
    private Room room;

    private ListView messageListView;
    private MessageAdapter messageAdapter;

    private ChatAsyncClient chatAsyncClient;
    private ChatThreadAsyncClient chatThreadAsyncClient;

    public ChatCard(Room room, MapViewModel mapViewModel, View view) {
        this.room = room;
        this.mapViewModel = mapViewModel;
        profileController = ProfileController.getProfileController(mapViewModel.getDir());

    }

    public void showPopupWindow(final View view) {
        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.chat_container, null);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    v.setFocusable(false);
                    return true;
                }
                return false;
            }
        });

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        messageListView = popupView.findViewById(R.id.messageListView);
        messageListView.setDivider(null);
        messageListView.setDividerHeight(0);

        messageAdapter = new MessageAdapter(view.getContext(), R.layout.chat_message, new ArrayList<Messaggio>());


        messageListView.setAdapter(messageAdapter);


        chatAsyncClient = mapViewModel.getChatAsyncClient();

        chatAsyncClient.startRealtimeNotifications(profileController.getProfile().getToken(), view.getContext());

        chatAsyncClient.addEventHandler(ChatEventType.CHAT_MESSAGE_RECEIVED, (ChatEvent payload) -> {
            ChatMessageReceivedEvent chatMessageReceivedEvent = (ChatMessageReceivedEvent) payload;
            // You code to handle chatMessageReceived event
            String displayName = chatMessageReceivedEvent.getSenderDisplayName();
            String testo = chatMessageReceivedEvent.getContent();

            boolean inviato = false;
            if(chatMessageReceivedEvent.getSender().getRawId().equals(profileController.getProfile().getUserId()))
                inviato = true;

            Messaggio m = new Messaggio(testo, displayName, inviato);

            Handler mHandler = new Handler(Looper.getMainLooper());

            // anywhere else in your code
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    messageAdapter.add(m);
                }
            });
            Log.d("Ricevuto", testo);
        });

        String endpoint = "https://hermeschat.communication.azure.com/";

        chatThreadAsyncClient = new ChatThreadClientBuilder()
                .endpoint(endpoint)
                .credential(new CommunicationTokenCredential(profileController.getProfile().getToken()))
                .addPolicy(new UserAgentPolicy(APPLICATION_ID, SDK_NAME, SDK_VERSION))
                .chatThreadId(room.getThreadId())
                .buildAsyncClient();

        //Initialize the elements of our window, install the handler
        TextView roomName = popupView.findViewById(R.id.roomName);
        roomName.setText(room.getNome());

        TextInputEditText inputText = popupView.findViewById(R.id.inputMessaggio);

        Button buttonEdit = popupView.findViewById(R.id.inviaMessaggio);

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String content = inputText.getText().toString();
                inputText.setText("");

                // The display name of the sender, if null (i.e. not specified), an empty name will be set.
                final String senderDisplayName = profileController.getProfile().getNome();

                SendChatMessageOptions chatMessageOptions = new SendChatMessageOptions()
                        .setType(ChatMessageType.TEXT)
                        .setContent(content)
                        .setSenderDisplayName(senderDisplayName);

                // A string is the response returned from sending a message, it is an id, which is the unique ID
                // of the message.
                try {
                    Log.d("Inviato", "Provo ad inviare" + content + " name: " + profileController.getProfile().getNome());
                    String chatMessageId = chatThreadAsyncClient.sendMessage(chatMessageOptions).get().getId();
                    Log.d("Inviato", chatMessageId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


            }
        });



        //Handler for clicking on the inactive zone of the window

        popupView.setOnTouchListener((v, event) -> {

            //Close the window when clicked
            //popupWindow.dismiss();
            return true;
        });
    }
}
