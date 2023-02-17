package be.cenzo.hermes.ui;

import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import be.cenzo.hermes.KeyHandler;
import be.cenzo.hermes.R;
import okhttp3.OkHttpClient;

public class ProfileCard implements Observer {


    private TextInputEditText nome;
    private TextInputEditText favlang;
    private Profile profile;
    private String funcKey;
    private String chatString;
    private KeyHandler keyHandler;
    private ProfileController profileController;
    private boolean dismissFlag;

    private final OkHttpClient client = new OkHttpClient();

    public ProfileCard() {
        keyHandler = KeyHandler.getKeyHandler();
        dismissFlag = true;
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

        if(profile != null){
            nome.setText(profile.getNome());
            favlang.setText(profile.getFavLang());
        }

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        //Initialize the elements of our window, install the handler

        Button buttonEdit = popupView.findViewById(R.id.salvaProfiloButton);

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nomeTextValue = nome.getText().toString();
                String favlangTextValue = favlang.getText().toString();

                if(!profileController.isValid()) {
                    profile = profileController.createProfile(nomeTextValue, favlangTextValue);
                    Log.d("profilo", "il profilo non è valido");
                }
                else{
                    profileController.updateProfile(nomeTextValue, favlangTextValue);
                    Log.d("profilo", "il profilo è valido");
                }

                while(dismissFlag);
                popupWindow.dismiss();

            }
        });



        //Handler for clicking on the inactive zone of the window

        popupView.setOnTouchListener((v, event) -> {

            //Close the window when clicked
            //popupWindow.dismiss();
            return true;
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        dismissFlag = false;
        profileController.deleteObserver(this);
    }
}
