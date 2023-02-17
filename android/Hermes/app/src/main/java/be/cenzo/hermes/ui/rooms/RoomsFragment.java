package be.cenzo.hermes.ui.rooms;

import static com.azure.android.maps.control.options.PopupOptions.anchor;
import static com.azure.android.maps.control.options.PopupOptions.content;
import static com.azure.android.maps.control.options.PopupOptions.position;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.azure.android.maps.control.AzureMaps;
import com.azure.android.maps.control.MapControl;
import com.azure.android.maps.control.MapMath;
import com.azure.android.maps.control.Popup;
import com.azure.android.maps.control.data.Position;
import com.azure.android.maps.control.events.OnFeatureClick;
import com.azure.android.maps.control.layer.BubbleLayer;
import com.azure.android.maps.control.layer.SymbolLayer;
import com.azure.android.maps.control.options.AnchorType;
import com.azure.android.maps.control.source.DataSource;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;

import java.io.IOException;
import java.net.URISyntaxException;

import be.cenzo.hermes.KeyHandler;
import be.cenzo.hermes.R;
import be.cenzo.hermes.databinding.FragmentRoomsBinding;
import be.cenzo.hermes.ui.translate.TranslateViewModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RoomsFragment extends Fragment {

    private static final int REQUEST_LOCATION = 1;

    private FragmentRoomsBinding binding;
    private MapViewModel mapViewModel;

    private DataSource roomsSource;
    private DataSource userSource;

    private MapControl mapControl;
    private Button add;
    private TextInputEditText nome;
    private TextInputEditText descrizione;

    private final OkHttpClient client = new OkHttpClient();

    private LocationManager locationManager;
    private Location lastLocation;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapViewModel =
                new ViewModelProvider(this).get(MapViewModel.class);

        View rootView = inflater.inflate(R.layout.fragment_rooms, container, false);
        binding = FragmentRoomsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Context context = root.getContext();
        userSource = new DataSource();

        AzureMaps.setSubscriptionKey(KeyHandler.getMapsKey());

        mapViewModel.setDir(context.getFilesDir());
        mapViewModel.initialize();

        mapViewModel.getLastLocation().observe(getViewLifecycleOwner(), new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location loc) {
                if(lastLocation != null)
                    userSource.remove(Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude()));
                lastLocation = loc;
                Log.d("LocationChanged", "long: " + mapViewModel.getLastLocation().getValue().getLongitude());
                userSource.add(Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude()));
            }
        });

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions( new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, mapViewModel.getLocationListener());
                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mapViewModel.setLastLocation(lastLocation);
                Log.d("lastlocation", "lastLocation: " + lastLocation);

            }
        }


        mapControl = (MapControl) root.findViewById(R.id.mapcontrol);
        mapControl.onCreate(savedInstanceState);
        add = (Button) root.findViewById(R.id.addButton);

        add.setOnClickListener((view) -> {addRoom(view);});

        //getRooms();

        //Wait until the map resources are ready.
        mapControl.onReady(map -> {

            //Create a data source and add it to the map.
            roomsSource = new DataSource();

            //getRooms();

            //Import the geojson data and add it to the data source.
            try {
                roomsSource.importDataFromUrl("https://hermesapiapp.azurewebsites.net/api/GetRooms?code=" + KeyHandler.getFuncKey() + "&str=" + KeyHandler.getConnString());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            //Add data source to the map.
            map.sources.add(roomsSource);

            // Posizione dell'utente
            map.sources.add(userSource);

            if(lastLocation != null){
                userSource.add(Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude()));
                Log.d("Coordinate", "long: " + lastLocation.getLongitude() + " altitude: " + lastLocation.getLatitude());
            }

            //Create a layer and add it to the map.
            SymbolLayer symbolLayer = new SymbolLayer(roomsSource);
            map.layers.add(symbolLayer);

            // layer dell'utente
            BubbleLayer layer = new BubbleLayer(userSource);
            map.layers.add(layer);

            //Add a click event to the layer.
            map.events.add((OnFeatureClick) (feature) -> {

                Feature f = feature.get(0);
                JsonObject props = f.properties();

                Room room = new Room(f.getStringProperty("Name"), f.getStringProperty("Description"), f.getStringProperty("threadId"));

                RoomCard roomCard = new RoomCard(room, mapViewModel);
                roomCard.showPopupWindow(rootView);
                return false;
            }, symbolLayer);
        });

        return root;
    }

    private String getRooms(){
        String endpoint = "https://hermesapiapp.azurewebsites.net/api/GetRooms?";

        RequestBody formBody = new FormBody.Builder()
                .add("connString", KeyHandler.getConnString())
                .build();

        Request request = new Request.Builder()
                .url(endpoint)
                .addHeader("x-functions-key", KeyHandler.getFuncKey())
                .post(formBody)
                .build();
        String rooms = "";
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

                    Log.d("Lista Rooms", "mi è arrivata la risposta: " + room);
                }
            }
        });
        return rooms;
    }

    private void addRoom(View view) {
        CreateRoomCard crc = new CreateRoomCard(mapViewModel);
        crc.showPopupWindow(view);
    }


    public void updateLocation(Location location){
        lastLocation = location;
        Log.d("Location:", "long: " + location.getLongitude() + " altitude: " + location.getAltitude());
        Toast.makeText(getActivity(), "long: " + location.getLongitude() + " altitude: " + location.getAltitude(), Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, mapViewModel.getLocationListener());
                    lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    mapViewModel.setLastLocation(lastLocation);
                } else {

                    Log.d("Permissions", "devi garantire i permessi per farla funzionare");

                }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapControl.onResume();
    }

    @Override
    public void onStart(){
        super.onStart();
        mapControl.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapControl.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapControl.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapControl.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapControl.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapControl.onSaveInstanceState(outState);
    }
}