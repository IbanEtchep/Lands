package fr.iban.lands.utils;

import fr.iban.lands.land.SChunk;

public class ChunkClaimSyncMessage {

    private int id;
    private SChunk sChunk;
    private boolean unclaim;

    public ChunkClaimSyncMessage(int id, SChunk sChunk, boolean unclaim) {
        this.id = id;
        this.sChunk = sChunk;
        this.unclaim = unclaim;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SChunk getsChunk() {
        return sChunk;
    }

    public boolean isUnclaim() {
        return unclaim;
    }
}
