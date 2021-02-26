package com.thejuiceman.profiler.command.commands;

import com.thejuiceman.profiler.MySQL;
import com.thejuiceman.profiler.PlayerData;
import com.thejuiceman.profiler.command.Command;
import com.thejuiceman.profiler.command.CommandHandler;
import com.thejuiceman.profiler.command.CommandInfo;
import me.kbrewster.mojangapi.MojangAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.UUID;

@CommandInfo(
        minimumArgs = 1,
        maxArgs = 1,
        needsPlayer = true,
        pattern = "resolve",
        permission = "profiler.resolve",
        usage = "/resolve <name>"
)

public class Resolve implements Command {

    @Override
    public boolean execute(CommandHandler cmdHandler, CommandSender sender, String... args) throws Exception {
        MySQL mysql = cmdHandler.getProfiler().getMysql();

        String name = args[0];

        Bukkit.getScheduler().runTaskAsynchronously(cmdHandler.getProfiler(), new Runnable() {
            @Override
            public void run() {

                try{
                    //Connect to database
                    mysql.openConnection();

                    //Check  if there is a case to resolve
                    ArrayList<PlayerData> matchingProfiles = mysql.getProfilesByName(name);
                    ArrayList<PlayerData> currentProfiles = new ArrayList<>();

                    //Check if there are matching profiles
                    if(matchingProfiles == null){
                        sender.sendMessage("There are no players with that name");
                        return;
                    }

                    for(PlayerData p : matchingProfiles){
                        if(mysql.isCurrentName(p.getName(), p.getUuid())){
                            currentProfiles.add(p);
                        }
                    }

                    //Check if there are any matches
                    if(currentProfiles.size() < 1){
                        sender.sendMessage("There are no players with that name");
                        return;
                    }

                    //Check if there is no resolution required
                    if(currentProfiles.size() == 1){
                        sender.sendMessage("There is no conflict associated with that name");
                        return;
                    }

                    //All other cases have been handle now we can resolve the username conflict
                    for(PlayerData p : currentProfiles){
                        String newName = MojangAPI.getName(UUID.fromString(p.getUuid()));
                        if(!p.getName().equals(newName)){
                            PlayerData newProfile = new PlayerData(p.getUuid(), newName, p.getIp(), null);
                            mysql.insertProfile(newProfile);
                        }
                    }

                    sender.sendMessage("Successfully resolved conflict over username: " + name);

                    return;

                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    try{mysql.closeConnection();}catch(Exception e){}
                }

            }
        });
        return true;
    }
}
