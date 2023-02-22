package be.cenzo.hermes.ui.rooms;

public class Room {

    private String roomId;
    private String nome;
    private String descrizione;
    private String threadId;

    public Room(String nome, String descrizione, String threadId, String roomId) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.threadId = threadId;
        this.roomId = roomId;
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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
