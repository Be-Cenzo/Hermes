package be.cenzo.hermes.ui.rooms;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.azure.android.maps.control.MapControl;

import java.util.Observable;
import java.util.Observer;

public class MapViewModel extends ViewModel implements Observer {

    private String connString;
    private String funcKey;

    private MutableLiveData<MapControl> map;
    private MutableLiveData<Location> lastLocation;

    private HermesLocationListener locationListener;

    public MapViewModel() {
        locationListener = new HermesLocationListener();
        locationListener.addObserver(this);
        map = new MutableLiveData<MapControl>();
        lastLocation = new MutableLiveData<Location>();

    }

    public LiveData<MapControl> getMap() {
        return map;
    }

    public LiveData<Location> getLastLocation() {
        return lastLocation;
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

    @Override
    public void update(Observable o, Object arg) {
        lastLocation.postValue((Location) arg);
    }
}
