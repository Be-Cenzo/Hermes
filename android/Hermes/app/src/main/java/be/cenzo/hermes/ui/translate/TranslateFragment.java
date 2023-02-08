package be.cenzo.hermes.ui.translate;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.azure.android.maps.control.AzureMaps;

import java.util.ArrayList;

import be.cenzo.hermes.MainActivity;
import be.cenzo.hermes.R;
import be.cenzo.hermes.databinding.FragmentTranslateBinding;

public class TranslateFragment extends Fragment {

    private String funcKey;
    private String speechKey;

    private TranslateViewModel translateViewModel;
    private FragmentTranslateBinding binding;

    private Spinner menu_1;
    private Spinner menu_2;

    private EditText editText_1;
    private EditText editText_2;

    private Button play_1;
    private Button play_2;
    private Button cancel_1;
    private Button cancel_2;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        translateViewModel =
                new ViewModelProvider(this).get(TranslateViewModel.class);

        binding = FragmentTranslateBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        translateViewModel.setOutputDir(root.getContext().getCacheDir());

        try {
            ApplicationInfo app = getActivity().getPackageManager().getApplicationInfo(root.getContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            speechKey = bundle.getString("speechKey");
            funcKey = bundle.getString("funcKey");
            translateViewModel.setKeys(speechKey, funcKey);
        } catch (Exception e) {
            Log.d("KEY", "Errore durante il retrieve della chiave");
            e.printStackTrace();
        }

        menu_1 = binding.menu1;
        menu_2 = binding.menu2;

        editText_1 = binding.editText1;
        editText_2 = binding.editText2;

        play_1 = binding.play1;
        play_2 = binding.play2;
        play_1.setOnClickListener((v) -> startRecording(v));
        play_2.setOnClickListener((v) -> startRecording(v));

        cancel_1 = binding.cancel1;
        cancel_2 = binding.cancel2;


        ArrayList<Language> languages = new ArrayList<Language>();
        String[] coll =  {"Italiano", "English"};
        languages.add(new Language("Italian", "it-IT"));
        languages.add(new Language("English", "en-US"));
        languages.add(new Language("Spanish", "es-ES"));


        ArrayAdapter langList = new ArrayAdapter(root.getContext(), android.R.layout.simple_spinner_item, languages);
        langList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter langListReversed = new ArrayAdapter(root.getContext(), android.R.layout.simple_spinner_item, languages);
        langListReversed.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_reversed);

        menu_1.setAdapter(langListReversed);
        menu_2.setAdapter(langList);

        translateViewModel.getEditText_1_Value().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                editText_1.setText(s);
            }
        });

        translateViewModel.getEditText_2_Value().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                editText_2.setText(s);
            }
        });
        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startRecording(View view) {
        translateViewModel.setMenu_1_selected((Language) menu_1.getSelectedItem());
        translateViewModel.setMenu_2_selected((Language) menu_2.getSelectedItem());
        int btn = 0;

        if(view.getId() == binding.play1.getId()) {
            Log.d("Play", "Play 1 traduco da 1 ad 2");
            btn = 1;
        }
        else if(view.getId() == binding.play2.getId()) {
            Log.d("Play", "Play 2 traduco da 2 ad 1");
            btn = 2;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!CheckPermissions()) {
                //getActivity().requestPermissions( new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                RequestPermissions();
                return;
            }
            else{
                translateViewModel.startRecording3();
            }
        }

        transformPlayButton(btn);

    }

    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        getActivity().requestPermissions( new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void stopRecording(View view){
        translateViewModel.stopRecording3();
        resetListeners();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void transformPlayButton(int btn){
        if(btn == 1){
            play_2.setOnClickListener(null);
            play_1.setOnClickListener((view) -> stopRecording(view));
            cancel_2.setOnClickListener(null);
            play_1.setText("S");
        }
        else if(btn == 2){
            play_2.setOnClickListener((view) -> stopRecording(view));
            play_1.setOnClickListener(null);
            cancel_1.setOnClickListener(null);
            play_2.setText("S");
        }
        else{
            Log.d("GestioneStati", "Errore");
        }
    }

    private void resetListeners(){
        play_1.setOnClickListener((view) -> startRecording(view));
        play_2.setOnClickListener((view) -> startRecording(view));
        cancel_2.setOnClickListener(null);
        cancel_2.setOnClickListener(null);
        play_1.setText("O");
        play_2.setText("O");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}