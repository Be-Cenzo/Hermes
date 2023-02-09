package be.cenzo.hermes.ui.translate;

import android.util.Log;

public class TranslateResults {

    private int stato;
    private String srcText;
    private String dstText;
    private String base64DstAudio;

    public TranslateResults(){
        stato = RunnableRequestTask.CREATO;
    }

    public void setResults(String results){
        if(stato == RunnableRequestTask.CREATO){
            srcText = results;
            Log.d("Lang", "ok questa roba doveva funzionare " + srcText);
            stato = RunnableRequestTask.SPEECH_TO_TEXT;
        }
        else if(stato == RunnableRequestTask.SPEECH_TO_TEXT){
            dstText = results;
            stato = RunnableRequestTask.TRANSLATE;
        }
        else if(stato == RunnableRequestTask.TRANSLATE){
            base64DstAudio = results;
            stato = RunnableRequestTask.TEXT_TO_SPEECH;
        }
    }

    public int getStato() {
        return stato;
    }

    public void setStato(int stato) {
        this.stato = stato;
    }

    public String getSrcText() {
        return srcText;
    }

    public void setSrcText(String srcText) {
        this.srcText = srcText;
    }

    public String getDstText() {
        return dstText;
    }

    public void setDstText(String dstText) {
        this.dstText = dstText;
    }

    public String getBase64DstAudio() {
        return base64DstAudio;
    }

    public void setBase64DstAudio(String base64DstAudio) {
        this.base64DstAudio = base64DstAudio;
    }
}
