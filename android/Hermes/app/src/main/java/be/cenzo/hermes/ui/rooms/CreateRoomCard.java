package be.cenzo.hermes.ui.rooms;

import android.location.Location;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import be.cenzo.hermes.R;
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

    private TextInputEditText nome;
    private TextInputEditText descrizione;
    private MapViewModel mapViewModel;

    private final OkHttpClient client = new OkHttpClient();

    public CreateRoomCard(MapViewModel mapViewModel){
        this.mapViewModel = mapViewModel;
    }

    public void showPopupWindow(final View view) {

        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.create_room_popup, null);

        nome = (TextInputEditText) popupView.findViewById(R.id.inputnome);
        descrizione = (TextInputEditText) popupView.findViewById(R.id.descrizione);

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
                Location lastLocation = mapViewModel.getLastLocation().getValue();

                String nomeValue = nome.getText().toString();
                String descrizioneValue = descrizione.getText().toString();
                String longitude = "" + lastLocation.getLongitude();
                String latitude = "" + lastLocation.getLatitude();

                Log.d("Bottone", "" + nomeValue);

                String endpoint = "https://hermesapiapp.azurewebsites.net/api/CreateRoom";

                String jsonBody = "{\"connString\": \"" + mapViewModel.getConnString() + "\", \"longitude\": \"" + longitude + "\" ,\"latitude\": \"" + latitude + "\" ,\"nome\": \"" + nomeValue + "\" , \"descrizione\": \"" + descrizioneValue + "\"}";

                RequestBody formBody = RequestBody.create(jsonBody, JSON );

                Request request = new Request.Builder()
                        .url(endpoint)
                        .addHeader("x-functions-key", mapViewModel.getFuncKey())
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
