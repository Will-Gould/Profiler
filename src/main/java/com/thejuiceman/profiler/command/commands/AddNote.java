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
        ArrayList<PlayerData> players = mysql.getProfilesByName(playerName);

        //Check if there are any matches
        if(players == null){
            sender.sendMessage("Could not find a player by that name! Names are case sensitive");
            return true;
        }

        //Check if there are multiple possible targets
        if(players.size() > 1){
            //Check if duplicate name entries are old usernames of different players
            //Create an array of playerdata that contains all the profile entries of player with that
            int currentNames = players.size();
            for(PlayerData p : players){
                if(!mysql.isCurrentName(p.getName(), p.getUuid())){
                    currentNames--;
                }
            }

            //If counter > 1 that means there are multiple people which this new note could apply too and
            //an edge case has been reached. Tell the command sender to use command to resolve conflicts
            if(currentNames > 1){
                sender.sendMessage("It appears that multiple users currently use this name in our database use /profiler resolve <uuid> to resolve conflicts");
                sender.sendMessage("Possible uuids:");
                for(PlayerData p : players){
                    if(mysql.isCurrentName(p.getName(), p.getUuid())){
                        sender.sendMessage(" - " + p.getUuid());
                    }
                }
                return true;
            }

            //If counter == 0 that means someone's old username is trying to be used
            //List to the user possible player name(s)
            if(currentNames == 0){
                sender.sendMessage("No players are currently using that name! Did you mean...");
                for(PlayerData p : players){
                    sender.sendMessage(" - " + mysql.getCurrentProfile(p.getUuid()).getName());
                }
                return true;
            }

        }else{
            //Check if the only match is an old name (and therefore invalid)
            if(!mysql.isCurrentName(players.get(0).getName(), players.get(0).getUuid())){
                sender.sendMessage("No players are currently using that name! Did you mean...");
                for(PlayerData p : players){
                    sender.sendMessage(" - " + mysql.getCurrentProfile(p.getUuid()).getName());
                }
                return true;
            }
            //There is only one possible target add the note
            uuid = players.get(0).getUuid();
            try{
                playerNameFormat = ChatColor.translateAlternateColorCodes('&', cmdHandler.getProfiler().util.getPrefix(cmdHandler.getProfiler(), uuid) + playerName);
            }catch(Exception e){

            }

            mysql.addNote(note, uuid, sender.getName());
            sender.sendMessage(ChatColor.GOLD + "Succesfully added note to player: " + ChatColor.WHITE + playerNameFormat);
            return true;
        }
        return true;
    }
}
