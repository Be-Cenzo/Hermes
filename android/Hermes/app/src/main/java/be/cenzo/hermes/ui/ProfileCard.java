package be.cenzo.hermes.ui;

import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.google.android.material.textfield.TextInputEditText;

import be.cenzo.hermes.R;
import okhttp3.OkHttpClient;

public class ProfileCard {


    private TextInputEditText nome;
    private TextInputEditText favlang;
    private Profile profile;

    private final OkHttpClient client = new OkHttpClient();

    public ProfileCard(Profile profile){
        this.profile = profile;
    }

    public void showPopupWindow(final View view) {

        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.profile_popup, null);

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
                /*Editable nomeText = null;
                Editable favlangText = null;

                if(nome != null)
                    nomeText = nome.getText();

                if(favlang != null)
                    favlangText = favlang.getText();

                String nomeTextValue = null;
                String favlangTextValue = null;
                if(nomeText != null)
                    nomeTextValue = nomeText.toString();

                if(favlangText != null)
                    favlangTextValue = favlangText.toString();*/

                String nomeTextValue = nome.getText().toString();
                String favlangTextValue = favlang.getText().toString();

                profile = new Profile(nomeTextValue, favlangTextValue);
                profile.serialize(popupView.getContext().getFilesDir());
                popupWindow.dismiss();

            }
        });



        //Handler for clicking on the inactive zone of the window

        popupView.setOnTouchListener((v, event) -> {

            //Close the window when clicked
            popupWindow.dismiss();
            return true;
        });
    }

}
