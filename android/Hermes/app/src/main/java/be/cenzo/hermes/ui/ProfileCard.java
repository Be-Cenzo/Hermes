package be.cenzo.hermes.ui;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import com.azure.android.maps.control.source.DataSource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Observable;
import java.util.Observer;

import be.cenzo.hermes.KeyHandler;
import be.cenzo.hermes.R;
import be.cenzo.hermes.ui.rooms.MapViewModel;
import okhttp3.OkHttpClient;

public class ProfileCard implements Observer {


    private TextInputEditText nome;
    private TextInputEditText favlang;
    private Profile profile;
    private String funcKey;
    private String chatString;
    private KeyHandler keyHandler;
    private ProfileController profileController;
    private ImageView loading;
    private RelativeLayout profileLoadingContainer;

    private PopupWindow popupWindow;

    private final OkHttpClient client = new OkHttpClient();

    public ProfileCard() {
        keyHandler = KeyHandler.getKeyHandler();
    }

    public void showPopupWindow(final View view) {

        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.profile_popup, null);

        File saveDir = popupView.getContext().getFilesDir();

        profileController = ProfileController.getProfileController(saveDir);
        profile = profileController.getProfile();
        profileController.addObserver(this);

        nome = (TextInputEditText) popupView.findViewById(R.id.inputusername);
        favlang = (TextInputEditText) popupView.findViewById(R.id.inputfavlang);
        SeekBar radiusBar = popupView.findViewById(R.id.radiusBar);
        radiusBar.setProgress(30);
        TextView radiusText = popupView.findViewById(R.id.radiusText);

        if(profile != null){
            nome.setText(profile.getNome());
            favlang.setText(profile.getFavLang());
            int radius = profile.getRadiusValue();
            if(radius >= 10 && radius <= 30) {
                radiusBar.setProgress(radius);
                radiusText.setText(radius + "km");
            }
            else
                radiusBar.setProgress(30);
        }

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.setAnimationStyle(R.style.Animation);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        //Initialize the elements of our window, install the handler

        MaterialButton buttonEdit = popupView.findViewById(R.id.salvaProfiloButton);

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AnimatedVectorDrawable caricamento = (AnimatedVectorDrawable) AppCompatResources.getDrawable(v.getContext(), R.drawable.ic_loading_animated);

                profileLoadingContainer = popupView.findViewById(R.id.profileLoadingContainer);
                profileLoadingContainer.setVisibility(View.VISIBLE);
                loading = popupView.findViewById(R.id.profileLoading);
                loading.setImageDrawable(caricamento);
                caricamento.start();
                loading.setVisibility(View.VISIBLE);

                String nomeTextValue = nome.getText().toString();
                String favlangTextValue = favlang.getText().toString();
                int radiusValue = radiusBar.getProgress();

                if(profile.getRadiusValue() != radiusValue){
                    MapViewModel mapViewModel = profileController.getMapViewModel();
                    if(mapViewModel != null) {
                        DataSource roomsSource = mapViewModel.getRoomsSource();
                        Location lastLocation = mapViewModel.getLastLocation().getValue();
                        double longitude = lastLocation.getLongitude();
                        double latitude = lastLocation.getLatitude();
                        int km = radiusValue;
                        Log.d("RefreshRooms", "aggiorno le room con km: " + km + " long: " + longitude + " lat: " + latitude);

                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    roomsSource.importDataFromUrl("https://hermesapiapp.azurewebsites.net/api/GetRooms?code=" + KeyHandler.getFuncKey() + "&str=" + KeyHandler.getConnString() + "&km=" + km + "&long=" + longitude + "&lat=" + latitude);
                                } catch (URISyntaxException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                }

                if(!profileController.isValid()) {
                    profile = profileController.createProfile(nomeTextValue, favlangTextValue, radiusValue);
                    Log.d("profilo", "il profilo non è valido");
                }
                else{
                    profileController.updateProfile(nomeTextValue, favlangTextValue, radiusValue);
                    Log.d("profilo", "il profilo è valido");
                }

            }
        });


        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusText.setText(progress + "km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Handler for clicking on the inactive zone of the window



        popupView.setOnTouchListener((v, event) -> {

            //Close the window when clicked
            popupWindow.dismiss();
            return true;
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        Log.d("caricamento", "rimuovo l'icona");
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                popupWindow.dismiss();
            }
        });
        profileController.deleteObserver(this);
    }
}
