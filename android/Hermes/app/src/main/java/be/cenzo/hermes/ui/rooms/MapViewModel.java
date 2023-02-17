package be.cenzo.hermes.ui.rooms;

import static be.cenzo.hermes.ui.rooms.ApplicationConstants.APPLICATION_ID;
import static be.cenzo.hermes.ui.rooms.ApplicationConstants.SDK_NAME;
import static be.cenzo.hermes.ui.rooms.ApplicationConstants.SDK_VERSION;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.azure.android.communication.chat.ChatAsyncClient;
import com.azure.android.communication.chat.ChatClientBuilder;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.core.http.policy.UserAgentPolicy;
import com.azure.android.maps.control.MapControl;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import be.cenzo.hermes.KeyHandler;
import be.cenzo.hermes.ui.Profile;
import be.cenzo.hermes.ui.ProfileController;

public class MapViewModel extends ViewModel implements Observer {

    private String connString;
    private String funcKey;
    private String chatString;
    private KeyHandler keyHandler;
    private File dir;

    private MutableLiveData<MapControl> map;
    private MutableLiveData<Location> lastLocation;

    private HermesLocationListener locationListener;

    private Profile profile;
    private ProfileController profileController;
    private ChatAsyncClient chatAsyncClient;
    private String chatEndpoint = "https://hermeschat.communication.azure.com";

    public MapViewModel() {
        locationListener = new HermesLocationListener();
        locationListener.addObserver(this);
        map = new MutableLiveData<MapControl>();
        lastLocation = new MutableLiveData<Location>();
        keyHandler = KeyHandler.getKeyHandler();
    }

    public void initialize(){
        profileController = ProfileController.getProfileController(dir);
        if(profileController.isValid()) {
            profile = profileController.getProfile();
            chatAsyncClient = new ChatClientBuilder()
                    .endpoint(chatEndpoint)
                    .credential(new
                            CommunicationTokenCredential(profile.getToken()))
                    .addPolicy(new UserAgentPolicy(APPLICATION_ID, SDK_NAME, SDK_VERSION))
                    .buildAsyncClient();
        }
    }

    public LiveData<MapControl> getMap() {
        return map;
    }

    public LiveData<Location> getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation.postValue(lastLocation);
    }

    public HermesLocationListener getLocationListener(){
        return locationListener;
    }

    public void setLocationListener(HermesLocationListener locationListener) {
        this.locationListener = locationListener;
    }

    public String getConnString() {
        return connString;
    }

    public void setConnString(String connString) {
        this.connString = connString;
    }

    public String getFuncKey() {
        return funcKey;
    }

    public void setFuncKey(String funcKey) {
        this.funcKey = funcKey;
    }

    public String getChatString() {
        return chatString;
    }

    public void setChatString(String chatString) {
        this.chatString = chatString;
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public ProfileController getProfileController() {
        return profileController;
    }

    public void setProfileController(ProfileController profileController) {
        this.profileController = profileController;
    }

    public ChatAsyncClient getChatAsyncClient() {
        return chatAsyncClient;
    }

    public void setChatAsyncClient(ChatAsyncClient chatAsyncClient) {
        this.chatAsyncClient = chatAsyncClient;
    }

    @Override
    public void update(Observable o, Object arg) {
        if(arg.getClass().equals(Location.class))
            lastLocation.postValue((Location) arg);
        else
            Log.d("MapViewRoomUpdate", "è arrivato un oggetto diverso");
    }
}
