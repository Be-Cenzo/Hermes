package be.cenzo.hermes.ui.rooms;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.Observable;

import be.cenzo.hermes.KeyHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RoomController extends Observable {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private String baseURL = "https://hermesapiapp.azurewebsites.net/api/";
    private String addPartecipantEndpoint = baseURL + "addpartecipanttoroom";
    private final OkHttpClient client = new OkHttpClient();
    private Call call;

    public RoomController(){

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addPartecipantToRoom(String userId, String displayName, String threadId){

        String jsonBody = "{\"connString\": \"" + KeyHandler.getChatString() + "\", \"hermesId\": \"" + KeyHandler.getHermesId() + "\" ,\"userId\": \"" + userId + "\" ,\"displayName\": \"" + displayName + "\" , \"threadId\": \"" + threadId + "\"}";

        RequestBody formBody = RequestBody.create(jsonBody, JSON );

        Request request = new Request.Builder()
                .url(addPartecipantEndpoint)
                .addHeader("x-functions-key", KeyHandler.getFuncKey())
                .post(formBody)
                .build();

        call = client.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(final Call call, final IOException e) {
                Log.d("Risposta", "errore nella richiesta ");
                setResultAndNotify(false);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d("Risposta", "problemi, problemi" + response.body().string());
                    setResultAndNotify(false);
                }
                else {
                    String textValue = response.body().string();
                    setResultAndNotify(true);
                    Log.d("Risposta", "adding partecipant results: " + textValue);
                }
            }
        });
    }

    private void setResultAndNotify(boolean result) {
        setChanged();
        notifyObservers(result);
    }

}
