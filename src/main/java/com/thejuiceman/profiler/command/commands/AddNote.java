package com.thejuiceman.profiler.command.commands;

import com.google.common.base.Joiner;
import com.thejuiceman.profiler.MySQL;
import com.thejuiceman.profiler.PlayerData;
import com.thejuiceman.profiler.command.Command;
import com.thejuiceman.profiler.command.CommandHandler;
import com.thejuiceman.profiler.command.CommandInfo;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@CommandInfo(
        minimumArgs = 2,
        maxArgs = -1,
        needsPlayer = true,
        pattern = "addnote",
        permission = "profiler.addnote",
        usage = "/addnote <targetplayer> <note>"
)

public class AddNote implements Command {

    @Override
    public boolean execute(CommandHandler cmdHandler, CommandSender sender, String... args) throws Exception {

        MySQL mysql = cmdHandler.getProfiler().getMysql();
        Player player = (Player) sender;
        String playerName = args[0];
        //Set the note as all args after the target player's name
        String note = Joiner.on(' ').join(ArrayUtils.subarray(args, 1, args.length));
        String playerNameFormat = playerName;
        String uuid;

        //Check if player is adding note to self
        if(playerName.equals(sender.getName())){
            player.sendMessage("You cannot add a note to yourself!");
            return true;
        }

        //Get array of possible target players
        ArrayList<PlayerData> profiles = mysql.getProfilesByName(playerName);

        //Check if there are any matches
        if(profiles== null){
            sender.sendMessage("Could not find a player by that name! Names are case sensitive");
            return true;
        }

        //Check which matches are current profiles and which are old profiles
        ArrayList<PlayerData> currentProfiles = new ArrayList<>();
        ArrayList<PlayerData> oldProfiles = new ArrayList<>();
        for(PlayerData p : profiles) {
            if (mysql.isCurrentName(p.getName(), p.getUuid())) {
                currentProfiles.add(p);
            } else {
                oldProfiles.add(p);
            }
        }

        //If no current names then loop through suggested targets (if any)
        if(currentProfiles.size() < 1){
            sender.sendMessage("No players are currently using that name!");
            if(oldProfiles.size() > 0){
                sender.sendMessage("Did you mean...");
                for(PlayerData p : oldProfiles){
                    sender.sendMessage(" - " + mysql.getCurrentProfile(p.getUuid()).getName());
                }
            }
            return true;
        }

        if(currentProfiles.size() > 1){
            sender.sendMessage(ChatColor.RED + "WARNING:" + ChatColor.GRAY + "It appears that multiple users currently use this name in our database use:" + ChatColor.AQUA + "/resolve <name>" + ChatColor.GRAY + "to resolve conflicts");
            return true;
        }

        //There is only one possible target add the note
        uuid = currentProfiles.get(0).getUuid();
        try{
            playerNameFormat = ChatColor.translateAlternateColorCodes('&', cmdHandler.getProfiler().util.getPrefix(cmdHandler.getProfiler(), uuid) + playerName);
        }catch(Exception e){

        }
        mysql.addNote(note, uuid, sender.getName());
        sender.sendMessage(ChatColor.GOLD + "Succesfully added note to player: " + ChatColor.WHITE + playerNameFormat);
        return true;

    }
}
