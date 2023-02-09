package be.cenzo.hermes.ui.translate;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public class TranslateViewModel extends ViewModel implements Observer{

    private RecordWaveTask recordTask;

    private String funcKey;
    private String speechKey;
    private String tranKey;

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private MutableLiveData<String> editText_1_value;
    private MutableLiveData<String> editText_2_value;
    private Language menu_1_selected;
    private Language menu_2_selected;

    private RunnableRequestTask runnableRequestTask;

    // direzione della traduzione
    // 1 traduzione da 1 a 2, va quindi prima cambiato il text di editText1 e poi quello di editText2
    // 2 traduzione da 2 ad 1
    private int dir;


    private AudioRecord audioRecorder = null;
    private DataOutputStream dos = null;
    private MediaRecorder recorder = null;
    private MediaPlayer mediaPlayer = null;
    private File outputFile;
    private String outputFilePath;
    private File outputDir;

    private final OkHttpClient client = new OkHttpClient();
    private Call call;

    public TranslateViewModel() {
        editText_1_value = new MutableLiveData<>();
        editText_1_value.setValue("Let's Start the Conversation");
        editText_2_value = new MutableLiveData<>();
        editText_2_value.setValue("Let's Start the Conversation");
        recordTask = new RecordWaveTask();
        //req = new RequestTask(dir, srcLang, dstLang, funcKey, speechKey);
        dir = 0;
    }

    public void setOutputDir(File outputDir){
        this.outputDir = outputDir;
    }

    public void setKeys(String speech, String func, String tran){
        speechKey = speech;
        funcKey = func;
        tranKey = tran;
    }

    public LiveData<String> getEditText_1_Value() {
        return editText_1_value;
    }

    public LiveData<String> getEditText_2_Value() {
        return editText_2_value;
    }

    private void updateEditText(TranslateResults results){
        if(results.getStato() == RunnableRequestTask.TEXT_TO_SPEECH){
            //riproduci audio
            try{
                byte[] audioData = android.util.Base64.decode(results.getBase64DstAudio(), android.util.Base64.DEFAULT);
                InputStream inputStream = new ByteArrayInputStream(audioData);
                Log.d("elaboro", "Ho decodificato");

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    MediaDataSource dataSource = new MediaDataSource() {
                        @Override
                        public int readAt(long position, byte[] buffer, int offset, int size) {
                                    /*int i = 0;
                                    for(; i<size && i+position<audioData.length; i++){
                                        buffer[offset+i] = audioData[(int) (position+i-1)];
                                    }
                                    if(i<size)
                                        return -1;
                                    return i;*/
                            if (position + size > audioData.length) {
                                return -1;
                            } else {
                                System.arraycopy(audioData, (int) position, buffer, offset, size);
                                return size;
                            }
                        }

                        @Override
                        public long getSize() {
                            return audioData.length;
                        }

                        @Override
                        public void close() {
                        }
                    };


                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(dataSource);
                    mediaPlayer.prepare();
                    Log.d("elaboro", "faccio play");
                    mediaPlayer.start();
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        else if(dir == 1){
            if(results.getStato() == RunnableRequestTask.SPEECH_TO_TEXT)
                editText_1_value.postValue(results.getSrcText());
            else if(results.getStato() == RunnableRequestTask.TRANSLATE)
                editText_2_value.postValue(results.getDstText());
        }
        else if(dir == 2){
            if(results.getStato() == RunnableRequestTask.SPEECH_TO_TEXT)
                editText_2_value.postValue(results.getSrcText());
            else if(results.getStato() == RunnableRequestTask.TRANSLATE)
                editText_1_value.postValue(results.getDstText());
        }
    }

    public void setMenu_1_selected(Language l){
        Log.d("Lang","Che sta succedendo?" + l.getCode());
        menu_1_selected = l;
    }

    public void setMenu_2_selected(Language l){
        menu_2_selected = l;
    }

    public void startRecording(int dir){
        this.dir = dir;
        //outputFilePath = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/audio" + new Random().nextInt(100000) + ".wav";
        //outputFile = new File(outputFilePath);//File.createTempFile("audio", ".wav", outputDir);
        try {
            outputFile = File.createTempFile("audio" + new Random().nextInt(100000), ".wav", outputDir);
            switch (recordTask.getStatus()) {
                case RUNNING:
                    //Toast.makeText(this, "Task already running...", Toast.LENGTH_SHORT).show();
                    return;
                case FINISHED:
                    recordTask = new RecordWaveTask();
                    break;
                case PENDING:
                    if (recordTask.isCancelled()) {
                        recordTask = new RecordWaveTask();
                    }
            }
            //File wavFile = new File(getFilesDir(), "recording_" + System.currentTimeMillis() / 1000 + ".wav");
            //Toast.makeText(this, outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            recordTask.execute(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void stopRecording(){
        if (!recordTask.isCancelled() && recordTask.getStatus() == AsyncTask.Status.RUNNING) {
            recordTask.cancel(false);

            String endpoint = "https://hermesapiapp.azurewebsites.net/api/speechtotext";
            uploadFile(endpoint, outputFile);
        } else {
            Log.d("Stop", "non era in esecuzione");
        }
    }

    public void cancelExecution(){
        if (!recordTask.isCancelled() && recordTask.getStatus() == AsyncTask.Status.RUNNING) {
            recordTask.cancel(false);
        } else {
            Log.d("Stop", "non era in esecuzione");
        }
        /*if (req != null){
            req.cancel(true);
            req.unsubscribe(this);
        }
        req = null;*/
        //req = new RequestTask(dir, srcLang, dstLang, funcKey, speechKey);
       if (runnableRequestTask != null) {
           runnableRequestTask.cancel();
           runnableRequestTask.deleteObserver(this);
           runnableRequestTask = null;

       }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String encodeFileToBase64(File file) {
        byte[] fileContent = null;// getWavData(file.getPath());
        try {
            fileContent = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Boolean uploadFile(String serverURL, File file) {
        /*try {

            /*RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("audio", file.getName(),
                            RequestBody.create(file, MediaType.parse("audio/wav")))
                    .build();
            String srcLang = menu_1_selected.getCode();
            String dstLang = menu_2_selected.getCode();
            if(dir == 2)
                srcLang = menu_2_selected.getCode();
                dstLang = menu_1_selected.getCode();


            String requestBody = encodeFileToBase64(file);
            Log.d("audiowav", requestBody);

            RequestBody requestBodys= new FormBody.Builder()
                    .add("file", requestBody)
                    .add("srcLang", srcLang)
                    .add("dstLang", dstLang)
                    .build();

            Request request = new Request.Builder()
                    .url(serverURL)
                    .addHeader("x-functions-key", funcKey)
                    .addHeader("x-speech-key", speechKey)
                    .post(requestBodys)
                    .build();

            call = client.newCall(request);
            call.enqueue(new Callback() {

                @Override
                public void onFailure(final Call call, final IOException e) {
                    Log.d("Risposta", "errore nella richiesta");
                }

                @Override
                public void onResponse(final Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.d("Risposta", "problemi, problemi");
                    }
                    else {
                        String textValue = response.body().string();
                        updateEditText(textValue);
                        Log.d("Risposta", "risposta: " + textValue);
                    }
                }
            });

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/


        /*req.subscribe(this);
        req.setFile(outputFile);
        req.execute();*/
        createAndExecuteRunnable();
        return false;
    }

    public void createAndExecuteRunnable(){
        String srcLang = menu_1_selected.getCode();
        String dstLang = menu_2_selected.getCode();
        Log.d("Lang", "src:" + srcLang + " dst:" + dstLang);
        if(dir == 2) {
            srcLang = menu_2_selected.getCode();
            dstLang = menu_1_selected.getCode();
        }
        runnableRequestTask = new RunnableRequestTask(dir, srcLang, dstLang, funcKey, speechKey, tranKey, outputFile);
        runnableRequestTask.addObserver(this);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(runnableRequestTask);
    }

    /*public void getUpdates(){
        updateEditText(req.getResult());
    }*/

    @Override
    public void update(Observable o, Object arg) {
        Log.d("Debug", "non funziona");
        updateEditText((TranslateResults) arg);
    }
}