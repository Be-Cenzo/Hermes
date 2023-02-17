package be.cenzo.hermes.ui.rooms;

import static be.cenzo.hermes.ui.rooms.ApplicationConstants.APPLICATION_ID;
import static be.cenzo.hermes.ui.rooms.ApplicationConstants.SDK_NAME;
import static be.cenzo.hermes.ui.rooms.ApplicationConstants.SDK_VERSION;

import android.location.Location;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.core.http.policy.UserAgentPolicy;
import com.azure.android.core.rest.util.paging.PagedAsyncStream;
import com.google.android.material.textfield.TextInputEditText;

import com.azure.android.communication.chat.*;
import com.azure.android.communication.chat.models.*;
import com.azure.android.communication.common.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

import be.cenzo.hermes.KeyHandler;
import be.cenzo.hermes.R;
import be.cenzo.hermes.ui.Profile;
import be.cenzo.hermes.ui.ProfileController;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CreateRoomCard {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private TextInputEditText nomeRoomInput;
    private TextInputEditText descrizioneRoomInput;
    private MapViewModel mapViewModel;
    private KeyHandler keyHandler;
    private ProfileController profileController;

    private final OkHttpClient client = new OkHttpClient();

    public CreateRoomCard(MapViewModel mapViewModel){
        this.mapViewModel = mapViewModel;
        keyHandler = KeyHandler.getKeyHandler();
        this.profileController = ProfileController.getProfileController(mapViewModel.getDir());
    }

    public void showPopupWindow(final View view) {

        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.create_room_popup, null);

        nomeRoomInput = (TextInputEditText) popupView.findViewById(R.id.inputnome);
        descrizioneRoomInput = (TextInputEditText) popupView.findViewById(R.id.descrizione);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        //Initialize the elements of our window, install the handler

        Button buttonEdit = popupView.findViewById(R.id.creaRoomButton);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // info sulla room da creare
                Location lastLocation = mapViewModel.getLastLocation().getValue();

                String nomeRoom = nomeRoomInput.getText().toString();
                String descrizioneRoom = descrizioneRoomInput.getText().toString();
                String longitude = "" + lastLocation.getLongitude();
                String latitude = "" + lastLocation.getLatitude();

                // creazione del thread della chat
                //profileController = mapViewModel.getProfileController();
                if(!profileController.isValid()){
                    Log.d("CreateRoomClick", "devi prima configurare il tuo profilo");
                    InvalidProfileCard invalidProfileCard = new InvalidProfileCard();
                    invalidProfileCard.showPopupWindow(view);
                    popupWindow.dismiss();
                    return;
                }
                if(mapViewModel.getChatAsyncClient() == null){
                    mapViewModel.initialize();
                }
                Profile currentProfile = profileController.getProfile();

                // A list of ChatParticipant to start the thread with.
                List<ChatParticipant> participants = new ArrayList<>();
                // The display name for the thread participant.
                participants.add(new ChatParticipant()
                        .setCommunicationIdentifier(new CommunicationUserIdentifier(KeyHandler.getHermesId()))
                        .setDisplayName("Hermes"));

                String repeatabilityRequestID = ""; //opzionale
                CreateChatThreadOptions createChatThreadOptions = new CreateChatThreadOptions()
                        .setTopic(nomeRoom)
                        .setParticipants(participants)
                        .setIdempotencyToken(repeatabilityRequestID);

                CreateChatThreadResult createChatThreadResult =
                        null;
                try {
                    createChatThreadResult = mapViewModel.getChatAsyncClient().createChatThread(createChatThreadOptions).get();
                    Log.d("creazioneThread", "ok");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                ChatThreadProperties chatThreadProperties = createChatThreadResult.getChatThreadProperties();
                String threadId = chatThreadProperties.getId();

                String chat = "https://hermeschat.communication.azure.com/";

                ChatThreadAsyncClient chatThreadAsyncClient = new ChatThreadClientBuilder()
                        .endpoint(chat)
                        .credential(new CommunicationTokenCredential(profileController.getProfile().getToken()))
                        .addPolicy(new UserAgentPolicy(APPLICATION_ID, SDK_NAME, SDK_VERSION))
                        .chatThreadId(threadId)
                        .buildAsyncClient();

                PagedAsyncStream<ChatParticipant> part = chatThreadAsyncClient.listParticipants();
                part.forEach(partecipant -> {
                   Log.d("Partecipanti", "Partecipanti: " + partecipant.getDisplayName() + " " + partecipant.getCommunicationIdentifier().getRawId() );
                });

                String endpoint = "https://hermesapiapp.azurewebsites.net/api/CreateRoom";

                String jsonBody = "{\"connString\": \"" + KeyHandler.getConnString() + "\", \"longitude\": \"" + longitude + "\" ,\"latitude\": \"" + latitude + "\" ,\"nome\": \"" + nomeRoom + "\" , \"descrizione\": \"" + descrizioneRoom + "\", \"threadId\": \"" + threadId + "\"}";

                RequestBody formBody = RequestBody.create(jsonBody, JSON );

                Request request = new Request.Builder()
                        .url(endpoint)
                        .addHeader("x-functions-key", KeyHandler.getFuncKey())
                        .post(formBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try (ResponseBody responseBody = response.body()) {
                            if (!response.isSuccessful())
                                throw new IOException("Unexpected code " + response);

                            Headers responseHeaders = response.headers();
                            for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                            }

                            String room = responseBody.string();
                            Log.d("elaboro", "mi è arrivata la risposta: " + room);



                        }
                    }
                });

                popupWindow.dismiss();

            }
        });



        //Handler for clicking on the inactive zone of the window

        popupView.setOnTouchListener((v, event) -> {

            //Close the window when clicked
            popupWindow.dismiss();
            return true;
        });
    }
}
