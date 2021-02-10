package com.thejuiceman.profiler;

import java.sql.Timestamp;

public class Note {

    private String playerUuid;
    private String noter;
    private String note;
    private Timestamp timeStamp;

    public Note(String playerUuid, String noter, String note, Timestamp timeStamp){
        this.playerUuid = playerUuid;
        this.noter = noter;
        this.note = note;
        this.timeStamp = timeStamp;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public String getNoter() {
        return noter;
    }

    public String getNote() {
        return note;
    }

    public Timestamp getTimeStamp(){
        return this.timeStamp;
    }
}
