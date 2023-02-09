package be.cenzo.hermes.ui.translate;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Observable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RunnableRequestTask extends Observable implements Runnable {

    private String funcKey;
    private String speechKey;
    private String tranKey;

    public static int CANCELLAZIONE = 0;
    public static int CREATO = 1;
    public static int ESECUZIONE = 2;
    public static int SPEECH_TO_TEXT = 3;
    public static int TRANSLATE = 4;
    public static int TEXT_TO_SPEECH = 5;

    private int direction;
    private String srcLang;
    private String dstLang;
    private File audioFile;
    private int stato;
    private TranslateResults result;
    private boolean running;

    private String baseURL = "https://hermesapiapp.azurewebsites.net/api/";
    private String speechToTextEndpoint = baseURL + "speechtotext";
    private String translateEndpoint = baseURL + "translate";
    private String textToSpeechEndpoint = baseURL + "texttospeech";
    private final OkHttpClient client = new OkHttpClient();
    private Call call;

    private ArrayList<TranslateViewModel> subscribers = new ArrayList<TranslateViewModel>();
    private boolean isCancelled;

    public RunnableRequestTask(int direction, String srcLang, String dstLang, String funcKey, String speechKey, String tranKey, File outputFile){
        this.direction = direction;
        this.audioFile = outputFile;
        this.srcLang = srcLang;
        this.dstLang = dstLang;
        this.funcKey = funcKey;
        this.speechKey = speechKey;
        this.tranKey = tranKey;
        result = new TranslateResults();
        stato = CREATO;
    }

    public TranslateResults getResult(){
        return result;
    }


    @Override
    public void run() {
        isCancelled = false;
        running = true;
        stato = ESECUZIONE;
        Log.d("Running", "Running");
        SpeechToText();
        Translate();
        TextToSpeech();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void SpeechToText(){
        String requestBody = encodeFileToBase64(audioFile);
        Log.d("audiowav", requestBody);

        RequestBody requestBodys= new FormBody.Builder()
                .add("file", requestBody)
                .add("srcLang", srcLang)
                .add("dstLang", dstLang)
                .build();

        Request request = new Request.Builder()
                .url(speechToTextEndpoint)
                .addHeader("x-functions-key", funcKey)
                .addHeader("x-speech-key", speechKey)
                .post(requestBodys)
                .build();

        call = client.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(final Call call, final IOException e) {
                Log.d("Risposta", "errore nella richiesta " + isCancelled);
                running = false;
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d("Risposta", "problemi, problemi");
                }
                else {
                    String textValue = response.body().string();
                    stato = SPEECH_TO_TEXT;
                    setResultAndNotify(textValue);
                    Log.d("Risposta", "risposta: " + textValue);
                }
                running = false;
            }
        });
        while(running){
            if(isCancelled)
                call.cancel();
        }
        running = true;
    }

    private void Translate(){

        String queryString = "?text=" + result.getSrcText() + "&from=" + srcLang + "&to=" + dstLang;

        Request request = new Request.Builder()
                .url(translateEndpoint + queryString)
                .addHeader("x-functions-key", funcKey)
                .addHeader("x-translate-key", tranKey)
                .build();

        call = client.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(final Call call, final IOException e) {
                Log.d("Risposta", "errore nella richiesta " + isCancelled);
                running = false;
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d("Risposta", "problemi, problemi");
                }
                else {
                    String textValue = response.body().string();
                    stato = TRANSLATE;
                    setResultAndNotify(textValue);
                    Log.d("Risposta", "risposta: " + textValue);
                }
                running = false;
            }
        });
        while(running){
            if(isCancelled)
                call.cancel();
        }
        running = true;
    }

    private void TextToSpeech(){

        String queryString = "?text=" + result.getDstText() + "&lang=" + dstLang;

        Request request = new Request.Builder()
                .url(textToSpeechEndpoint + queryString)
                .addHeader("x-functions-key", funcKey)
                .addHeader("x-speech-key", speechKey)
                .build();

        call = client.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(final Call call, final IOException e) {
                Log.d("Risposta", "errore nella richiesta " + isCancelled);
                running = false;
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d("Risposta", "problemi, problemi");
                }
                else {
                    Audio audio = new Gson().fromJson(response.body().string(), Audio.class);
                    stato = TEXT_TO_SPEECH;
                    setResultAndNotify(audio.getBase64audio());
                    Log.d("Risposta", "risposta: " + result.getBase64DstAudio());
                }
                running = false;
            }
        });
        while(running){
            if(isCancelled)
                call.cancel();
        }
    }

    private void setResultAndNotify(String value){

        result.setResults(value);
        Log.d("IsCancelled", "" + isCancelled);
        if(isCancelled)
            return;
        setChanged();
        notifyObservers(result);
    }

    public void cancel(){
        isCancelled = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String encodeFileToBase64(File file) {
        byte[] fileContent = null;
        try {
            fileContent = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }

}
