package be.cenzo.hermes.ui;

import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Observable;

import be.cenzo.hermes.KeyHandler;
import be.cenzo.hermes.ui.rooms.MapViewModel;
import okhttp3.Call;
import okhttp3.Callback;
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
    private MapViewModel mapViewModel;

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
            this.profile = new Profile(null, null, 30);
            profile.serialize(dir);
        }
        this.stato = PRONTO;
        this.dir = dir;
    }

    public boolean isValid(){
        if(profile == null)
            return false;
        if(profile.getNome() == null || profile.getNome().isEmpty() || profile.getNome().trim().isEmpty())
            return false;
        if(profile.getUserId() == null || profile.getUserId().isEmpty()){
            Log.d("profilo", "userId: " + profile.getUserId());
            creaToken();
            return false;
        }
        if(!getProfile().tokenIsValid()){
            Log.d("Token", "Il token non è valido");
            creaToken();
            return false;
        }
        Log.d("Token", "Il token è valido");
        return true;
    }

    public Profile createProfile(String name, String favLang, int radiusValue){
        profile = new Profile(name, favLang, radiusValue);
        creaToken();
        return profile;
    }

    public void creaToken(){
        profile.setCreatedToken(new Date());
        String endpoint = "https://hermesapiapp.azurewebsites.net/api/getToken";
        String userId = profile.getUserId();

        String jsonBody = "{\"connString\": \"" + keyHandler.getChatString() + "\"}";
        if(userId != null && !userId.isEmpty())
            jsonBody = "{\"connString\": \"" + keyHandler.getChatString() + "\", \"userId\": \"" + userId + "\"}";

        Log.d("CreazioneRichiesta", "jsonBody:" + jsonBody);

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
                profile.setCreatedToken(null);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        Log.d("Risposta", "Errore nella risposta");
                        profile.setCreatedToken(null);
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

    public MapViewModel getMapViewModel() {
        return mapViewModel;
    }

    public void setMapViewModel(MapViewModel mapViewModel) {
        this.mapViewModel = mapViewModel;
    }

    public void serialize(){
        profile.serialize(dir);
        Log.d("Serializzazione", "userId: " + profile.getUserId());
    }

    public void updateProfile(String nomeTextValue, String favlangTextValue, int radiusValue){
        profile.setNome(nomeTextValue);
        profile.setFavLang(favlangTextValue);
        profile.setRadiusValue(radiusValue);
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
