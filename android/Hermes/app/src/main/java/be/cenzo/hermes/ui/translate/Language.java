package be.cenzo.hermes.ui.translate;

public class Language {
    private String label;
    private String code;

    public Language(String label, String code){
        this.label = label;
        this.code = code;
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

    public String toString(){
        return label;
    }
}
