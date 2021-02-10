package com.thejuiceman.profiler;

import java.sql.Timestamp;
import java.util.Calendar;

public class PlayerData {

    private String uuid;
    private String name;
    private String ip;
    private Timestamp lastOnline;

    public PlayerData(String uuid, String name, String ip, Timestamp lastOnline){
        this.uuid = uuid;
        this.name = name;
        this.ip = ip;
        setTimestamp(lastOnline);
    }

    public String getUuid(){
        return this.uuid;
    }

    public String getName(){
        return this.name;
    }

    public String getIp(){
        return this.ip;
    }

    public Timestamp getLastOnline(){
        return this.lastOnline;
    }

    public void setTimestamp(Timestamp timeStamp){
        /**
         * @param timestamp = null current timestamp will be used
         **/
        if(timeStamp == null){
            Timestamp time = new Timestamp(Calendar.getInstance().getTime().getTime());
            this.lastOnline = time;
        }else{
            this.lastOnline = timeStamp;
        }
    }
}
