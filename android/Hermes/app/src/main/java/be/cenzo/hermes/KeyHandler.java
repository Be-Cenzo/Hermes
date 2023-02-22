package be.cenzo.hermes;

import android.os.Bundle;
import android.util.Log;

public class KeyHandler {

    private static String funcKey;
    private static String mapsKey;
    private static String connString;
    private static String chatString;
    private static String speechKey;
    private static String tranKey;
    private static String hermesId;
    private static KeyHandler keyHandler;

    public static KeyHandler getKeyHandler(){
        return keyHandler;
    };

    public static void createKeyHandler(Bundle bundle){
        try {
            String funcKey = bundle.getString("funcKey");
            String chatString = bundle.getString("chatString");
            String mapsKey = bundle.getString("mapsKey");
            String connString = bundle.getString("connString");
            String speechKey = bundle.getString("speechKey");
            String tranKey = bundle.getString("tranKey");
            String hermesId = bundle.getString("hermesId");
            keyHandler = new KeyHandler(funcKey, mapsKey, connString, chatString, speechKey, tranKey, hermesId);
        } catch (Exception e) {
            Log.d("KEY", "Errore durante il retrieve della chiave");
            e.printStackTrace();
        }
    }

    public KeyHandler(String funcKey, String mapsKey, String connString, String chatString, String speechKey, String tranKey, String hermesId) {
        KeyHandler.funcKey = funcKey;
        KeyHandler.mapsKey = mapsKey;
        KeyHandler.connString = connString;
        KeyHandler.chatString = chatString;
        KeyHandler.speechKey = speechKey;
        KeyHandler.tranKey = tranKey;
        KeyHandler.hermesId = hermesId;
    }

    public static String getFuncKey() {
        return funcKey;
    }

    public static String getMapsKey() {
        return mapsKey;
    }

    public static String getConnString() {
        return connString;
    }

    public static String getChatString() {
        return chatString;
    }

    public static String getSpeechKey() {
        return speechKey;
    }

    public static String getTranKey() {
        return tranKey;
    }

    public static String getHermesId() {
        return hermesId;
    }
}
