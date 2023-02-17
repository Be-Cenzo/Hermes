package be.cenzo.hermes.ui;

import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Observable;

import be.cenzo.hermes.KeyHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ProfileController extends Observable {

    private static int SERIALIZZAZIONE = 1;
    private static int PRONTO = 2;

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private Profile profile;
    private File dir;
    private static ProfileController profileController;
    private KeyHandler keyHandler;
    private int stato;

    private final OkHttpClient client = new OkHttpClient();

    public static ProfileController getProfileController(File dir){
        if(profileController == null){
            profileController = new ProfileController(dir);
        }
        return profileController;
    }

    private ProfileController(File dir) {
        keyHandler = KeyHandler.getKeyHandler();
        try {
            this.profile = Profile.deserialize(dir);
        } catch (Exception e) {
            this.profile = new Profile(null, null);
            profile.serialize(dir);
        }
        this.stato = PRONTO;
        this.dir = dir;
    }

    public boolean isValid(){
        if(profile == null)
            return false;
        if(profile.getUserId() == null || profile.getNome().isEmpty())
            return false;
        if(profile.getUserId() == null || profile.getUserId().isEmpty()){
            Log.d("profilo", "userId: " + profile.getUserId());
            creaToken();
            return false;
        }
        if(profile.getToken() == null ||profile.getToken().isEmpty()){
            // TODO: refreshToken()
            return false;
        }
        return true;
    }

    public Profile createProfile(String name, String favLang){
        profile = new Profile(name, favLang);
        creaToken();
        return profile;
    }

    public void creaToken(){
        String endpoint = "https://hermesapiapp.azurewebsites.net/api/getToken";

        String jsonBody = "{\"connString\": \"" + keyHandler.getChatString() + "\"}";
        Log.d("CreazioneRichiesta", "connString:" + keyHandler.getChatString());

        RequestBody formBody = RequestBody.create(jsonBody, JSON );

        Request request = new Request.Builder()
                .url(endpoint)
                .addHeader("x-functions-key", keyHandler.getFuncKey())
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("Risposta", "Errore");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        Log.d("Risposta", "Errore nella risposta");
                        throw new IOException("Unexpected code " + response);
                    }

                    Map<String, String> res = new Gson().fromJson(responseBody.string(), Map.class);
                    profile.setUserId(res.get("userId"));
                    profile.setToken(res.get("userToken"));
                    saveProfile();
                    Log.d("Risposta", "userId: " + res.get("userId"));
                    Log.d("Risposta", "mi è arrivata la risposta: " + res);

                    serialize();

                }
            }
        });
    }

    public Profile getProfile(){
        return profile;
    }

    public void serialize(){
        profile.serialize(dir);
        Log.d("Serializzazione", "userId: " + profile.getUserId());
    }

    public void updateProfile(String nomeTextValue, String favlangTextValue) {
        profile.setNome(nomeTextValue);
        profile.setFavLang(favlangTextValue);
        saveProfile();
    }

    public void saveProfile(){
        if(stato == PRONTO){
            stato = SERIALIZZAZIONE;
            serialize();
            stato = PRONTO;
            setChanged();
            notifyObservers();
        }
    }
}
