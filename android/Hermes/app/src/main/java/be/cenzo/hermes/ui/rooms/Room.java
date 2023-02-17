package be.cenzo.hermes.ui.rooms;

public class Room {

    private String nome;
    private String descrizione;
    private String threadId;

    public Room(String nome, String descrizione, String threadId) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.threadId = threadId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }
}
