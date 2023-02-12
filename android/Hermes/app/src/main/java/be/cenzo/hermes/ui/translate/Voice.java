package be.cenzo.hermes.ui.translate;

public class Voice {

    private String voiceLabel;
    private String voiceCode;
    private String voiceGender;

    public Voice(String voiceLabel, String voiceCode, String voiceGender) {
        this.voiceLabel = voiceLabel;
        this.voiceCode = voiceCode;
        this.voiceGender = voiceGender;
    }

    public String getVoiceLabel() {
        return voiceLabel;
    }

    public void setVoiceLabel(String voiceLabel) {
        this.voiceLabel = voiceLabel;
    }

    public String getVoiceCode() {
        return voiceCode;
    }

    public void setVoiceCode(String voiceCode) {
        this.voiceCode = voiceCode;
    }

    public String getVoiceGender() {
        return voiceGender;
    }

    public void setVoiceGender(String voiceGender) {
        this.voiceGender = voiceGender;
    }
}
