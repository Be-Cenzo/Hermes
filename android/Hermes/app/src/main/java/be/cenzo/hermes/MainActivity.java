package be.cenzo.hermes;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import be.cenzo.hermes.databinding.ActivityMainBinding;
import be.cenzo.hermes.ui.Profile;
import be.cenzo.hermes.ui.ProfileCard;
import be.cenzo.hermes.ui.rooms.CreateRoomCard;
import be.cenzo.hermes.ui.rooms.MapViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //checkUserData();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_rooms)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    public void checkUserData(){
        profile = Profile.deserialize(getApplicationContext().getFilesDir());
        if(profile != null)
            Log.d("deserialization", "" + profile.getNome());
        else
            Log.d("deserialization", "fallimento");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                profile = Profile.deserialize(getApplicationContext().getFilesDir());
                ProfileCard profileCard = new ProfileCard(profile);
                profileCard.showPopupWindow(findViewById(R.id.profile));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


}