package be.cenzo.hermes.ui.rooms;

import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.azure.android.communication.chat.ChatAsyncClient;
import com.azure.android.communication.chat.ChatThreadAsyncClient;

import java.util.Observable;
import java.util.Observer;

import be.cenzo.hermes.ChatCard;
import be.cenzo.hermes.R;
import be.cenzo.hermes.ui.ProfileController;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class RoomCard implements Observer {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private PopupWindow popupWindow;
    private View contextView;
    private boolean addeddFlag;
    private boolean success;

    private TextView nome;
    private TextView descrizione;
    private MapViewModel mapViewModel;
    private ProfileController profileController;

    private Room room;
    private RoomController roomController;

    private ChatAsyncClient chatAsyncClient;
    private ChatThreadAsyncClient chatThreadAsyncClient;

    private final OkHttpClient client = new OkHttpClient();

    public RoomCard(Room room, MapViewModel mapViewModel){
        this.room = room;
        this.mapViewModel = mapViewModel;
        profileController = ProfileController.getProfileController(mapViewModel.getDir());
        roomController = new RoomController();
    }

    public void showPopupWindow(View view) {

        roomController.addObserver(this);
        contextView = view;

        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.room_popup, null);

        nome = (TextView) popupView.findViewById(R.id.nomeroom);
        descrizione = (TextView) popupView.findViewById(R.id.descrizioneroom);
        nome.setText(room.getNome());
        descrizione.setText(room.getDescrizione());


        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.setAnimationStyle(R.style.Animation);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        //Initialize the elements of our window, install the handler

        Button buttonEdit = popupView.findViewById(R.id.entraRoomButton);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                addeddFlag = false;
                if(!profileController.isValid()){
                Log.d("RoomCardClick", "devi prima configurare il tuo profilo");
                InvalidProfileCard invalidProfileCard = new InvalidProfileCard();
                invalidProfileCard.showPopupWindow(view);
                popupWindow.dismiss();
                return;
                }
                if(mapViewModel.getChatAsyncClient() == null){
                    mapViewModel.initialize();
                }
                chatAsyncClient = mapViewModel.getChatAsyncClient();


                roomController.addPartecipantToRoom(profileController.getProfile().getUserId(), profileController.getProfile().getNome(), room.getThreadId());

                while(!addeddFlag);
                if(!success){
                    //TODO: utente non aggiunto card
                    Log.d("Risultato", "Utente non aggiunto");
                    popupWindow.dismiss();
                    return;
                }

                ChatCard chat = new ChatCard(room, mapViewModel, view);
                chat.showPopupWindow(view);

                popupWindow.dismiss();

            }
        });



        //Handler for clicking on the inactive zone of the window

        popupView.setOnTouchListener((v, event) -> {

            //Close the window when clicked
            //popupWindow.dismiss();
            return true;
        });
    }


    @Override
    public void update(Observable o, Object arg) {
        success = (boolean) arg;
        addeddFlag = true;
    }
}
