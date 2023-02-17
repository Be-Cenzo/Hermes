package be.cenzo.hermes.ui.rooms;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.google.android.material.textfield.TextInputEditText;

import be.cenzo.hermes.R;
import be.cenzo.hermes.ui.Profile;
import be.cenzo.hermes.ui.ProfileCard;

public class InvalidProfileCard {

        public InvalidProfileCard() {
        }

        public void showPopupWindow(final View view) {

            //Create a View object yourself through inflater
            LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.invalid_profile_popup, null);

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

            Button buttonEdit = popupView.findViewById(R.id.vaiAlProfilo);

            buttonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ProfileCard profileCard = new ProfileCard();
                    profileCard.showPopupWindow(view);
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

}
