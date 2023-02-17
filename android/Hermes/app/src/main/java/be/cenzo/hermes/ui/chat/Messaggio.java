package be.cenzo.hermes.ui.chat;

public class Messaggio {

    private String contenuto;
    private String displayName;
    private boolean inviato;

    public Messaggio(String contenuto, String displayName, boolean inviato) {
        this.contenuto = contenuto;
        this.displayName = displayName;
        this.inviato = inviato;
    }

    public String getContenuto() {
        return contenuto;
    }

    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isInviato() {
        return inviato;
    }

    public void setInviato(boolean inviato) {
        this.inviato = inviato;
    }
}
