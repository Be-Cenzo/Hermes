package be.cenzo.hermes.ui.rooms;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.azure.android.maps.control.MapControl;

public class MapViewModel extends ViewModel {

    private MutableLiveData<MapControl> map;

    public MapViewModel() {
        map = new MutableLiveData<MapControl>();
    }

    public LiveData<MapControl> getMap() {
        return map;
    }
}
