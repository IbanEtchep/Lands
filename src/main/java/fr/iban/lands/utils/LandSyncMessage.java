package fr.iban.lands.utils;

public class LandSyncMessage {

    private String senderServer;
    private int id;

    public LandSyncMessage() {}


    public LandSyncMessage(int id, String senderServer) {
        this.senderServer = senderServer;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getSenderServer() {
        return senderServer;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSenderServer(String senderServer) {
        this.senderServer = senderServer;
    }
}
