package be.cenzo.hermes;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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
import be.cenzo.hermes.ui.ProfileController;
import be.cenzo.hermes.ui.rooms.CreateRoomCard;
import be.cenzo.hermes.ui.rooms.MapViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private ProfileController profileController;
    private KeyHandler keyHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //checkUserData();
        try {
            ApplicationInfo app = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            KeyHandler.createKeyHandler(bundle);

        } catch (Exception e) {
            Log.d("KEY", "Errore durante il retrieve della chiave");
            e.printStackTrace();
        }

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
                profileController = ProfileController.getProfileController(getApplicationContext().getFilesDir());

                ProfileCard profileCard = new ProfileCard();
                profileCard.showPopupWindow(findViewById(R.id.profile));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

}