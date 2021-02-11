package com.thejuiceman.profiler;

import org.bukkit.event.player.PlayerLoginEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class MySQL {

    protected String host;
    protected Integer port;
    protected String database;
    protected String username;
    protected String password;
    private String prefix;
    private Profiler profiler;

    public MySQL(Profiler profiler){
        host        = profiler.getConfig().getString("mysql.host");
        port        = profiler.getConfig().getInt("mysql.port");
        database    = profiler.getConfig().getString("mysql.database");
        username    = profiler.getConfig().getString("mysql.username");
        password    = profiler.getConfig().getString("mysql.password");
        prefix      = profiler.getConfig().getString("mysql.prefix");

        this.profiler = profiler;
    }

    public boolean testConnection(){
        Connection con = null;
        try{
            con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(con != null){
                try{con.close();}catch(Exception e){}
                return true;
            }else{
                return false;
            }
        }
    }

    public void createDatabase(){
        String notesTable = "CREATE TABLE IF NOT EXISTS " + prefix + "notes (" +
                "noteid int KEY NOT NULL AUTO_INCREMENT, " +
                "uuid varchar(255), " +
                "time timestamp DEFAULT CURRENT_TIMESTAMP, " +
                "staff_name varchar(20), " +
                "note varchar(255) " +
                ");";

        String profilesTable = "CREATE TABLE IF NOT EXISTS " + prefix + "profiles (" +
                "profileid int KEY NOT NULL AUTO_INCREMENT, " +
                "uuid varchar(255), " +
                "name varchar(20) COLLATE latin1_general_cs, " +
                "ip varchar(39), " +
                "last_online timestamp DEFAULT CURRENT_TIMESTAMP " +
                ");";

        update(notesTable);
        update(profilesTable);
    }

    public void update(String query){
        Connection con = null;
        PreparedStatement sql = null;
        try{
            con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            sql = con.prepareStatement(query);
            sql.execute();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(sql != null){
                try{sql.close();}catch(Exception e){}
            }
            if(con != null){
                try{con.close();}catch(Exception e){}
            }
        }
    }

    public void updatePlayerLogin(PlayerLoginEvent event){
        String address = event.getAddress().toString();
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        //Remove the '/' from start of address
        address = address.substring(1);
        String name = event.getPlayer().getName();
        String uuid = event.getPlayer().getUniqueId().toString();

        Connection con = null;
        PreparedStatement sql = null;
        try{
            con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);

            if(playerDataContainsPlayer(event.getPlayer().getUniqueId())){
                //Get a list of possible player names
                ArrayList<PlayerData> playerData = getPlayerProfiles(event.getPlayer().getUniqueId());

                //Check if player has new name (Requires looping through found profiles with their uuid and
                //finding the latest one and seeing if it matches the current one)
                if(getCurrentProfile(playerData).getName().equals(name)){
                    //User has not changed their name since last joining
                    //Only need to update last_online and ip
                    sql = con.prepareStatement("UPDATE `" + this.prefix + "profiles` SET ip=?, last_online=? WHERE uuid=? AND name=?;");
                    sql.setString(1, address);
                    sql.setTimestamp(2, currentTimestamp);
                    sql.setString(3, uuid);
                    sql.setString(4, name);
                    sql.execute();
                }else {
                    //Check if a player is using a name they have used in the past
                    if(playerDataContainsPlayer(event.getPlayer().getUniqueId(), name)){
                        //Player is using an old name, use the old entry instead of creating a new one
                        sql = con.prepareStatement("UPDATE `" + this.prefix + "profiles` SET ip=?, last_online=? WHERE uuid=? AND name=?;");
                        sql.setString(1, address);
                        sql.setTimestamp(2, currentTimestamp);
                        sql.setString(3, uuid);
                        sql.setString(4, name);
                        sql.execute();
                    }else{
                        //User has changed their name since last joining
                        //Create new profile entry
                        sql = con.prepareStatement("INSERT INTO `" + this.prefix + "profiles` (uuid, name, ip, last_online) values(?, ?, ?, ?);");
                        sql.setString(1, uuid);
                        sql.setString(2, name);
                        sql.setString(3, address);
                        sql.setTimestamp(4, currentTimestamp);
                        sql.execute();
                    }
                }
            }else{
                //Create new profile entry
                sql = con.prepareStatement("INSERT INTO `" + this.prefix + "profiles` (uuid, name, ip, last_online) values(?, ?, ?, ?);");
                sql.setString(1, uuid);
                sql.setString(2, name);
                sql.setString(3, address);
                sql.setTimestamp(4, currentTimestamp);
                sql.execute();
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(sql != null){
                try{sql.close();}catch(Exception e){}
            }
            if(con != null){
                try{con.close();}catch(Exception e){}
            }
        }
    }

    public void insertProfile(PlayerData player){
        Connection con = null;
        PreparedStatement sql = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());

        try{
            con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            //Create new profile entry
            sql = con.prepareStatement("INSERT INTO `" + this.prefix + "profiles` (uuid, name, ip, last_online) values(?, ?, ?, ?);");
            sql.setString(1, player.getUuid());
            sql.setString(2, player.getName());
            sql.setString(3, player.getIp());
            sql.setTimestamp(4, currentTimestamp);
            sql.execute();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(sql != null){
                try{sql.close();}catch(Exception e){}
            }
            if(con != null){
                try{con.close();}catch(Exception e){}
            }
        }
    }

    public void addNote(String note, String uuid, String senderName){
        Connection con = null;
        PreparedStatement sql = null;
        try{
            con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            sql = con.prepareStatement("INSERT INTO `" + prefix + "notes` (uuid, staff_name, note) VALUES(?, ?, ?);");
            sql.setString(1, uuid);
            sql.setString(2, senderName);
            sql.setString(3, note);
            sql.execute();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(sql != null){
                try{sql.close();}catch(Exception e){}
            }
            if(con != null){
                try{con.close();}catch(Exception e){}
            }
        }
    }

    public ArrayList<Note> getNotes(PlayerData player){
        Connection con = null;
        PreparedStatement sql = null;
        ResultSet results = null;
        ArrayList<Note> notes = new ArrayList<>();
        try{
            con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            sql = con.prepareStatement("SELECT * FROM " + prefix + "notes WHERE uuid=?;");
            sql.setString(1, player.getUuid());
            results = sql.executeQuery();

            Note n;
            while(results.next()){
                n = new Note(results.getString("uuid"), results.getString("staff_name"), results.getString("note"), results.getTimestamp("time"));
                notes.add(n);
            }
            return notes;
        }catch(Exception e){
            return null;
        }finally{
            if(results != null){
                try{results.close();}catch(Exception e){}
            }
            if(sql != null){
                try{sql.close();}catch(Exception e){}
            }
            if(con != null){
                try{con.close();}catch(Exception e){}
            }
        }
    }

    public boolean playerDataContainsPlayer(UUID uuid){
        Connection con = null;
        PreparedStatement sql = null;
        ResultSet results = null;

        try {
            con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            sql = con.prepareStatement("SELECT * FROM `" + prefix + "profiles` WHERE uuid=?;");
            sql.setString(1, uuid.toString());
            results = sql.executeQuery();
            return results.next();
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            if(results != null){
                try{results.close();}catch(Exception e){}
            }
            if(sql != null){
                try{sql.close();}catch(Exception e){}
            }
            if(con != null){
                try{con.close();}catch(Exception e){}
            }
        }
    }

    public boolean playerDataContainsPlayer(UUID uuid, String name){
        Connection con = null;
        PreparedStatement sql = null;
        ResultSet results = null;

        try {
            con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            sql = con.prepareStatement("SELECT * FROM `" + prefix + "profiles` WHERE uuid=? AND name=?;");
            sql.setString(1, uuid.toString());
            sql.setString(2, name);
            results = sql.executeQuery();
            boolean containsPlayer = results.next();
            return containsPlayer;
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            if(results != null){
                try{results.close();}catch(Exception e){}
            }
            if(sql != null){
                try{sql.close();}catch(Exception e){}
            }
            if(con != null){
                try{con.close();}catch(Exception e){}
            }
        }
    }

    public ArrayList<PlayerData> getPlayerProfiles(UUID uuid){
        Connection con = null;
        PreparedStatement sql = null;
        ResultSet results = null;
        ArrayList<PlayerData> profiles = new ArrayList<>();
        try{
            con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            sql = con.prepareStatement("SELECT * FROM " + prefix + "profiles WHERE uuid=?;");
            sql.setString(1, uuid.toString());
            results = sql.executeQuery();

            //Put results into array of profiles
            PlayerData p;
            while(results.next()){
                p = new PlayerData(results.getString("uuid"), results.getString("name"), results.getString("ip"), results.getTimestamp("last_online"));
                profiles.add(p);
            }

            return profiles;
        }catch(Exception e){
            return null;
        }finally{
            if(results != null){
                try{results.close();}catch(Exception e){}
            }
            if(sql != null){
                try{sql.close();}catch(Exception e){}
            }
            if(con != null){
                try{con.close();}catch(Exception e){}
            }
        }

    }

    public ArrayList<PlayerData> getProfilesByName(String name){
        Connection con = null;
        PreparedStatement sql = null;
        ResultSet results = null;
        ArrayList<PlayerData> profiles = new ArrayList<>();
        try{
            con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            sql = con.prepareStatement("SELECT * FROM " + prefix + "profiles WHERE name=?;");
            sql.setString(1, name);
            results = sql.executeQuery();

            //Put results into array of profiles
            PlayerData p;
            while(results.next()){
                p = new PlayerData(results.getString("uuid"), results.getString("name"), results.getString("ip"), results.getTimestamp("last_online"));
                profiles.add(p);
            }

            //Make sure function returns null if no players are found
            if(profiles.size() == 0){
                return null;
            }
            return profiles;
        }catch(Exception e){
            System.out.println("\n\n Exception in getPlayer(String)");
            return null;
        }finally{
            if(results != null){
                try{results.close();}catch(Exception e){}
            }
            if(sql != null){
                try{sql.close();}catch(Exception e){}
            }
            if(con != null){
                try{con.close();}catch(Exception e){}
            }
        }
    }

    public PlayerData getCurrentProfile(ArrayList<PlayerData> profiles){
        /**
         * @param profiles Must be an arraylist of the same players profile entries
         *
         **/
        PlayerData result = profiles.get(0);
        for(PlayerData p : profiles){
            if(result.getLastOnline().before(p.getLastOnline())){
                result = p;
            }
        }
        return result;
    }

    public PlayerData getCurrentProfile(String uuid){
        UUID u = UUID.fromString(uuid);
        PlayerData result = getCurrentProfile(getPlayerProfiles(u));
        return result;
    }

    public ArrayList<PlayerData> getOldProfiles(String uuid){
        ArrayList<PlayerData> result = new ArrayList<>();
        ArrayList<PlayerData> profiles = getPlayerProfiles(UUID.fromString(uuid));
        for(PlayerData p : profiles){
            if(!isCurrentName(p.getName(), p.getUuid())){
                result.add(p);
            }
        }
        return result;
    }

    public boolean isCurrentName(String name, String uuid){
        UUID u = UUID.fromString(uuid);
        PlayerData current = getCurrentProfile(getPlayerProfiles(u));
        if(name.equals(current.getName())){
            return true;
        }
        return false;
    }
}
