package be.cenzo.hermes.ui.rooms;

import android.location.Location;
import android.location.LocationListener;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Observable;

public class HermesLocationListener extends Observable implements LocationListener {

    private Location lastLocation;
    private ArrayList<RoomsFragment> subscribers;

    public HermesLocationListener(){
        subscribers = new ArrayList<RoomsFragment>();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lastLocation = location;
        setChanged();
        notifyObservers(lastLocation);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.d("HermesLocationListener", "GPS disabilitato");
    }
}
