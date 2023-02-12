package be.cenzo.hermes.ui.translate;

public class Language {
    private String label;
    private String code;
    private Voice voice;

    public Language(String label, String code, Voice voices){
        this.label = label;
        this.code = code;
        this.voice = voice;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Voice getVoice() {
        return voice;
    }

    public void setVoices(Voice voice) {
        this.voice = voice;
    }

    public String toString(){
        return label;
    }
}
