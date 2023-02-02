package be.cenzo.hermes.ui.rooms;

import static com.azure.android.maps.control.options.PopupOptions.anchor;
import static com.azure.android.maps.control.options.PopupOptions.content;
import static com.azure.android.maps.control.options.PopupOptions.position;

import android.Manifest;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

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

import be.cenzo.hermes.R;
import be.cenzo.hermes.databinding.FragmentRoomsBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RoomsFragment extends Fragment {

    private String mapsKey;
    private String funcKey;
    private String connString;

    private FragmentRoomsBinding binding;

    private DataSource roomsSource;

    private MapControl mapControl;
    private Button add;
    private TextInputEditText nome;
    private TextInputEditText descrizione;

    private final OkHttpClient client = new OkHttpClient();

    private LocationManager locationManager;
    private HermesLocationListener locationListener;
    private Location lastLocation;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_rooms, container, false);
        binding = FragmentRoomsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Context context = root.getContext();

        try {
            ApplicationInfo app = getActivity().getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            mapsKey = bundle.getString("mapsKey");
            funcKey = bundle.getString("funcKey");
            connString = bundle.getString("connString");
            AzureMaps.setSubscriptionKey(mapsKey);
        } catch (Exception e) {
            Log.d("KEY", "Errore durante il retrieve della chiave");
            e.printStackTrace();
        }

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new HermesLocationListener();
        locationListener.subscribe(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                getActivity().requestPermissions( new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            }
        }


        mapControl = (MapControl) root.findViewById(R.id.mapcontrol);
        mapControl.onCreate(savedInstanceState);

        add = (Button) root.findViewById(R.id.addButton);
        nome = (TextInputEditText) root.findViewById(R.id.inputnome);
        descrizione = (TextInputEditText) root.findViewById(R.id.descrizione);

        add.setOnClickListener((view) -> {addRoom(view);});

        //getRooms();

        //Wait until the map resources are ready.
        mapControl.onReady(map -> {

            //Create a data source and add it to the map.
            roomsSource = new DataSource();

            //getRooms();

            //Import the geojson data and add it to the data source.
            try {
                roomsSource.importDataFromUrl("https://hermesdbapi.azurewebsites.net/api/GetRooms?code=" + funcKey + "&str=" + connString);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            //Add data source to the map.
            map.sources.add(roomsSource);

            // Posizione dell'utente
            DataSource userSource = new DataSource();
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

            //Create a popup and add it to the map.
            Popup popup = new Popup();
            map.popups.add(popup);

            //Close it initially.
            //popup.close();

            //Add a click event to the layer.
            map.events.add((OnFeatureClick) (feature) -> {
                //Get the first feature and it's properties.
                Feature f = feature.get(0);
                JsonObject props = f.properties();

                //Retrieve the custom layout for the popup.
                View customView = LayoutInflater.from(root.getContext()).inflate(R.layout.popup_text, null);

                //Display the name and entity type information of the feature into the text view of the popup layout.
                TextView tv = customView.findViewById(R.id.message);
                tv.setText("" +
                        f.getStringProperty("Name") + "\n" +
                        f.getStringProperty("Description")
                );

                //Get the position of the clicked feature.
                Position pos = MapMath.getPosition((Point) f.geometry());

                //Set the options on the popup.
                popup.setOptions(
                        //Set the popups position.
                        position(pos),

                        //Set the anchor point of the popup content.
                        anchor(AnchorType.BOTTOM),

                        //Set the content of the popup.
                        content(customView)
                );

                //Open the popup.
                popup.open();


                //Return a boolean indicating if event should be consumed or continue to bubble up.
                return false;
            }, symbolLayer);
        });

        return root;
    }

    private String getRooms(){
        String endpoint = "https://hermesdbapi.azurewebsites.net/api/GetRooms?";

        RequestBody formBody = new FormBody.Builder()
                .add("connString", connString)
                .build();

        Request request = new Request.Builder()
                .url(endpoint)
                .addHeader("x-functions-key", funcKey)
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
        String nomeValue = nome.getText().toString();
        String descrizioneValue = descrizione.getText().toString();
        String longitude = "" + lastLocation.getLongitude();
        String latitude = "" + lastLocation.getLatitude();

        Log.d("Bottone", "" + nomeValue);

        String endpoint = "https://hermesdbapi.azurewebsites.net/api/CreateRoom";

        RequestBody formBody = new FormBody.Builder()
                .add("connString", connString)
                .add("longitude", longitude)
                .add("latitude", latitude)
                .add("nome", nomeValue)
                .add("descrizione", descrizioneValue)
                .build();

        Request request = new Request.Builder()
                .url(endpoint)
                .addHeader("x-functions-key", funcKey)
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
                    Log.d("elaboro", "mi è arrivata la risposta: " + room);



                }
            }
        });
    }


    public void updateLocation(Location location){
        lastLocation = location;
        Log.d("Location:", "long: " + location.getLongitude() + " altitude: " + location.getAltitude());
        Toast.makeText(getActivity(), "long: " + location.getLongitude() + " altitude: " + location.getAltitude(), Toast.LENGTH_SHORT).show();
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