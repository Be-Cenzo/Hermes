package be.cenzo.hermes.ui.rooms;

import android.location.Location;
import android.location.LocationListener;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class HermesLocationListener implements LocationListener {

    private Location lastLocation;
    private ArrayList<RoomsFragment> subscribers;

    public HermesLocationListener(){
        subscribers = new ArrayList<RoomsFragment>();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lastLocation = location;
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.d("HermesLocationListener", "GPS disabilitato");
    }

    public void subscribe(RoomsFragment subscriber){
        subscribers.add(subscriber);
    }

    public void notifySubscribers(){
        for(RoomsFragment x : subscribers){
            x.updateLocation(lastLocation);
        }
    }
}
