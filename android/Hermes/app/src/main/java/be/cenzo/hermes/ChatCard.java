package be.cenzo.hermes;

import static be.cenzo.hermes.ui.rooms.ApplicationConstants.APPLICATION_ID;
import static be.cenzo.hermes.ui.rooms.ApplicationConstants.SDK_NAME;
import static be.cenzo.hermes.ui.rooms.ApplicationConstants.SDK_VERSION;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import com.azure.android.communication.chat.models.RealTimeNotificationCallback;
import com.azure.android.communication.chat.models.SendChatMessageOptions;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.core.http.policy.UserAgentPolicy;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import be.cenzo.hermes.ui.rooms.RoomController;

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
    private MessageListener messageListener;

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



        //Set the location of the window on the screen
        popupWindow.setAnimationStyle(R.style.chatAnimation);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        messageListView = popupView.findViewById(R.id.messageListView);
        messageListView.setDivider(null);
        messageListView.setDividerHeight(0);

        messageAdapter = new MessageAdapter(view.getContext(), R.layout.chat_message, new ArrayList<Messaggio>());


        messageListView.setAdapter(messageAdapter);


        chatAsyncClient = mapViewModel.getChatAsyncClient();

        chatAsyncClient.startRealtimeNotifications(profileController.getProfile().getToken(), view.getContext());

        messageListener = new MessageListener(messageAdapter);

        chatAsyncClient.addEventHandler(ChatEventType.CHAT_MESSAGE_RECEIVED, messageListener);

        String endpoint = "https://hermeschat.communication.azure.com/";

        chatThreadAsyncClient = new ChatThreadClientBuilder()
                .endpoint(endpoint)
                .credential(new CommunicationTokenCredential(profileController.getProfile().getToken()))
                .addPolicy(new UserAgentPolicy(APPLICATION_ID, SDK_NAME, SDK_VERSION))
                .chatThreadId(room.getThreadId())
                .buildAsyncClient();

        Handler handler = new Handler(Looper.getMainLooper());
        chatThreadAsyncClient.listMessages().forEach((message) -> {
            Log.d("VecchiMessaggi", "" + message.getSenderDisplayName() + ": " + message.getContent().getMessage() + " " + message.getType());
            if(message.getType().toString().equals("text")) {
                Log.d("VecchiMessaggi", "sono dentro");
                boolean inviato = false;
                if(message.getSenderCommunicationIdentifier().getRawId().equals(profileController.getProfile().getUserId()))
                    inviato = true;
                Log.d("VecchiMessaggi", "metadata: " + message.getMetadata());
                Messaggio msg = new Messaggio(message.getContent().getMessage(), message.getSenderDisplayName(), inviato);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        messageAdapter.insert(msg, 0);
                    }
                });

                Log.d("VecchiMessaggi", "ho aggiunto il messaggio all'adapter");
            }
        });

        //Initialize the elements of our window, install the handler
        TextView roomName = popupView.findViewById(R.id.roomName);
        roomName.setText(room.getNome());

        TextInputEditText inputText = popupView.findViewById(R.id.inputMessaggio);

        Button buttonInvia = popupView.findViewById(R.id.inviaMessaggio);
        Button backButton = popupView.findViewById(R.id.chatBackButton);

        buttonInvia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String content = inputText.getText().toString();
                if(content.isEmpty() || content.trim().isEmpty())
                    return;
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

        backButton.setOnClickListener((v) -> {
            popupWindow.dismiss();
        });

        //Handler for clicking on the inactive zone of the window

        popupView.setOnTouchListener((v, event) -> {

            //Close the window when clicked
            //popupWindow.dismiss();
            return true;
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                chatThreadAsyncClient.removeParticipant(new CommunicationUserIdentifier(profileController.getProfile().getUserId()));
                chatAsyncClient.removeEventHandler(ChatEventType.CHAT_MESSAGE_RECEIVED, messageListener);
                RoomController roomController = new RoomController();
                roomController.removePartecipantFromRoom(profileController.getProfile().getUserId(), room.getThreadId(), room.getRoomId());
                Log.d("OnDismiss", "Rimosso il partecipante");
            }
        });
    }

    public class MessageListener implements RealTimeNotificationCallback {

        private MessageAdapter messageAdapter;

        public MessageListener(MessageAdapter messageAdapter){
            this.messageAdapter = messageAdapter;
        }

        public void setMessageAdapter(MessageAdapter messageAdapter){
            this.messageAdapter = messageAdapter;
        }

        @Override
        public void onChatEvent(ChatEvent chatEvent) {
            ChatMessageReceivedEvent chatMessageReceivedEvent = (ChatMessageReceivedEvent) chatEvent;
            // You code to handle chatMessageReceived event
            String displayName = chatMessageReceivedEvent.getSenderDisplayName();
            String testo = chatMessageReceivedEvent.getContent();

            if(!chatMessageReceivedEvent.getChatThreadId().equals(room.getThreadId()))
                return;

            boolean inviato = false;
            if(chatMessageReceivedEvent.getSender().getRawId().equals(profileController.getProfile().getUserId()))
                inviato = true;

            Messaggio m = new Messaggio(testo, displayName, inviato);

            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    messageAdapter.add(m);
                }
            });
            Log.d("Ricevuto", testo);
            Log.d("Adapter", " messaggi nell'adapter: " + messageAdapter.getCount());
        }
    }
}
