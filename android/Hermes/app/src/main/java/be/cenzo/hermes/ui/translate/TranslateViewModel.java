package be.cenzo.hermes.ui.translate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TranslateViewModel extends ViewModel {

    private RecordWaveTask recordTask;

    private String funcKey;
    private String speechKey;

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private MutableLiveData<String> editText_1_value;
    private MutableLiveData<String> editText_2_value;
    private Language menu_1_selected;
    private Language menu_2_selected;

    private AudioRecord audioRecorder = null;
    private DataOutputStream dos = null;
    private MediaRecorder recorder = null;
    private MediaPlayer mp = null;
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
    }

    public void setOutputDir(File outputDir){
        this.outputDir = outputDir;
    }

    public void setKeys(String speech, String func){
        speechKey = speech;
        funcKey = func;
    }

    public LiveData<String> getEditText_1_Value() {
        return editText_1_value;
    }

    public LiveData<String> getEditText_2_Value() {
        return editText_2_value;
    }

    public void setMenu_1_selected(Language l){
        menu_1_selected = l;
    }

    public void setMenu_2_selected(Language l){
        menu_2_selected = l;
    }

    @SuppressLint("WrongConstant")
    public void startRecording(){
        Log.d("Translate", "Traduco da: " + menu_1_selected + " a " + menu_2_selected);
        // Registrazione dell'audio
        //outputDir = getCacheDir(); // context being the Activity pointer
        try{
            outputFile = File.createTempFile("audio" + new Random().nextInt(100000), ".wav", outputDir);
            //outputFilePath = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/audio.wav";
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                outputFilePath = Environment.getStorageDirectory().getAbsolutePath() + "/audio.wav";
            }*/
            //Log.d("FilePath" , outputFilePath);
            //outputFile.setReadable(true, false);
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(AudioFormat.ENCODING_PCM_16BIT);//MediaRecorder.OutputFormat.RAW_AMR  AudioFormat.ENCODING_PCM_16BIT
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//.AAC
            recorder.setAudioChannels(1);
            recorder.setAudioEncodingBitRate(128000);
            recorder.setAudioSamplingRate(16000);
            recorder.setOutputFile(outputFile.getPath());

            recorder.prepare();

            recorder.start();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void startRecording2(){
        Log.d("Translate", "Traduco da: " + menu_1_selected + " a " + menu_2_selected);
        // Registrazione dell'audio
        //outputDir = getCacheDir(); // context being the Activity pointer
        int buffSize =  AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, buffSize);
        try {
            outputFile = File.createTempFile("audio", ".wav", outputDir);
            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
            short[] buffer = new short[buffSize/4];
            audioRecorder.startRecording();
            int bufferReadResult = audioRecorder.read(buffer, 0, buffSize/4);
            while (bufferReadResult != AudioRecord.ERROR_INVALID_OPERATION && bufferReadResult != AudioRecord.ERROR && bufferReadResult != AudioRecord.ERROR_BAD_VALUE && bufferReadResult != AudioRecord.ERROR_DEAD_OBJECT ) {

                for(int i = 0; i< bufferReadResult; i++)
                    dos.writeShort(buffer[i]);
                audioRecorder.read(buffer, 0, buffSize);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startRecording3(){
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
    public void stopRecording3(){
        if (!recordTask.isCancelled() && recordTask.getStatus() == AsyncTask.Status.RUNNING) {
            recordTask.cancel(false);

            String endpoint = "https://hermesapiapp.azurewebsites.net/api/speechtotext";
            uploadFile(endpoint, outputFile);
        } else {
            Log.d("Stop", "non era in esecuzione");
        }
    }

    public void stopRecording2(){
        if(audioRecorder != null){
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
            try {
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private byte[] getWavData(String filePath) {
        final File file = new File(filePath);
        final byte[] data;
        try {
            data = Files.readAllBytes(file.toPath());
            /*final DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(data);
            dis.close();*/
            final int totalAudioLen = data.length;
            final int totalDataLen = totalAudioLen + 36;
            final byte[] header = new byte[44];

            header[0] = 'R';
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f';
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16;
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1;
            header[21] = 0;
            header[22] = (byte) CHANNEL_CONFIG;
            header[23] = 0;
            header[24] = (byte) (SAMPLE_RATE & 0xff);
            header[25] = (byte) ((SAMPLE_RATE >> 8) & 0xff);
            header[26] = (byte) ((SAMPLE_RATE >> 16) & 0xff);
            header[27] = (byte) ((SAMPLE_RATE >> 24) & 0xff);
            header[28] = (byte) (SAMPLE_RATE * 2 * CHANNEL_CONFIG / 8);
            header[29] = 0;
            header[30] = (byte) (AUDIO_FORMAT / 8);
            header[31] = 0;
            header[32] = (byte) (CHANNEL_CONFIG * AUDIO_FORMAT / 8);
            header[33] = 0;
            header[34] = 16;
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (totalAudioLen & 0xff);
            header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
            header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
            header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

            byte[] wavFile = new byte[header.length + data.length];
            System.arraycopy(header, 0 , wavFile, 0, header.length);
            System.arraycopy(data, 0, wavFile, header.length-1, data.length);
            return wavFile;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*public boolean downloadFile(final String path) {
        try {

            File file = new File(.getDir("filesdir", Context.MODE_PRIVATE) + "/yourfile.png");

            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            FileOutputStream outStream = new FileOutputStream(file);
            byte[] buff = new byte[5 * 1024];

            int len;
            while ((len = inStream.read(buff)) != -1) {
                outStream.write(buff, 0, len);
            }

            outStream.flush();
            outStream.close();
            inStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }*/

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void stopRecording(){
        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder = null;

        /*try {
            if (mp != null) {
                mp.stop();
                mp.reset();
                mp.release();
                mp = null;
            }
            mp = new MediaPlayer();
            Log.d("seguimi", "" + outputDir + "    file: " + outputFile);
            Log.d("boh", "" + outputFile.length());
            mp.setDataSource("" + outputFile);
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        Log.d("File", "" + outputFile);

        byte[] file = getWavData(outputFile.getPath());
        for(int i = 0; i<44; i++){
            Log.d("header wav file", file[i] + "\n");
        }

        String fPath = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/audio.wav";
        Log.d("filepath" , fPath);
        File f = new File(fPath);
        try {
            FileOutputStream fos = new FileOutputStream(fPath);
            fos.write(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        String endpoint = "https://hermesapiapp.azurewebsites.net/api/speechtotext";
        uploadFile(endpoint, outputFile);
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
        try {

            /*RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("audio", file.getName(),
                            RequestBody.create(file, MediaType.parse("audio/wav")))
                    .build();*/

            String requestBody = encodeFileToBase64(file);
            Log.d("audiowav", requestBody);

            RequestBody requestBodys= new FormBody.Builder()
                    .add("file", requestBody)
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
                    /*else
                        editText_1_value.setValue(response.body().string());*/
                    Log.d("Risposta", "risposta: " + response.body().string());
                }
            });

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}