package com.thejuiceman.profiler.command.commands;

import com.thejuiceman.profiler.MySQL;
import com.thejuiceman.profiler.Note;
import com.thejuiceman.profiler.PlayerData;
import com.thejuiceman.profiler.Util;
import com.thejuiceman.profiler.command.Command;
import com.thejuiceman.profiler.command.CommandHandler;
import com.thejuiceman.profiler.command.CommandInfo;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@CommandInfo(
        maxArgs = 1,
        minimumArgs = 1,
        needsPlayer = true,
        pattern = "status",
        permission = "profiler.status",
        usage = "/status <player>"
)

public class Status implements Command {

    Util util = new Util();

    @Override
    public boolean execute(CommandHandler cmdHandler, CommandSender sender, String... args) throws Exception {

        MySQL mysql = cmdHandler.getProfiler().getMysql();
        String targetPlayer = args[0];

        //Get possible players and check if player exists in database
        ArrayList<PlayerData> profiles = mysql.getProfilesByName(targetPlayer);
        if(profiles == null){
            sender.sendMessage("Could not find a player by that name!");
            return true;
        }

        //Check which ones are current names
        ArrayList<PlayerData> currentProfiles = new ArrayList<>();
        ArrayList<PlayerData> oldProfiles = new ArrayList<>();
        for(PlayerData p : profiles) {
            if (mysql.isCurrentName(p.getName(), p.getUuid())) {
                currentProfiles.add(p);
            } else {
                oldProfiles.add(p);
            }
        }

        //There are no current users by that name but there are multiple old ones send possible targets
        //If there is only one old profile return the current profile of the old one
        if(currentProfiles.size() == 0 && oldProfiles.size() > 0){
            if(oldProfiles.size() == 1){
                //Use current profile of old profile
                sender.sendMessage("Did you mean...");
                sendStatus(mysql.getCurrentProfile(oldProfiles.get(0).getUuid()), sender, util, cmdHandler);
            }else{
                sender.sendMessage("No current users found by that name. Did you mean...");
                sendPossibleTargets(oldProfiles, sender, util, cmdHandler);
            }
            return true;
        }

        //Only current profiles to be displayed.
        if(currentProfiles.size() > 0){
            if(currentProfiles.size() > 1){
                sender.sendMessage(ChatColor.RED + "WARNING" + ChatColor.GRAY + ": There are currently 2 players stored with that username. Notify a staff member to resolve this conflict.");
            }
            for(PlayerData p : currentProfiles){
                sendStatus(p, sender, util, cmdHandler);
            }
            //Check and notify if there are user who have previously used this name
            if(oldProfiles.size() > 0){
                sender.sendMessage(ChatColor.AQUA + "The following players have also previously used this name:");
                sendPossibleTargets(oldProfiles, sender, util, cmdHandler);
            }
            return true;
        }

        return true;
    }

    private void sendStatus(PlayerData player, CommandSender sender, Util util, CommandHandler cmdHandler){
        sender.sendMessage(ChatColor.AQUA + "================" + ChatColor.GREEN + "Player Profile" + ChatColor.AQUA + "================");
        String playerNameFormat = util.formatName(player.getName(), player.getUuid(), cmdHandler.getProfiler());
        sender.sendMessage(playerNameFormat);
        sendPlayerData(player, sender, util, cmdHandler);
        sendNotes(player, sender, cmdHandler);
    }

    private void sendPossibleTargets(ArrayList<PlayerData> profiles, CommandSender sender, Util util, CommandHandler cmdHandler){
        String playerNameFormat;
        for(PlayerData p : profiles){
            PlayerData current = cmdHandler.getProfiler().getMysql().getCurrentProfile(p.getUuid());
            playerNameFormat = util.formatName(current.getName(), current.getUuid(), cmdHandler.getProfiler());
            sender.sendMessage(" - " + playerNameFormat);
        }
    }

    private void sendPlayerData(PlayerData player, CommandSender sender, Util util, CommandHandler cmdHandler){
        //Can only be used on current player PlayerData
        //Get old names if any
        ArrayList<PlayerData> oldProfiles = cmdHandler.getProfiler().getMysql().getOldProfiles(player.getUuid());
        if(oldProfiles.size() > 0){
            sender.sendMessage(ChatColor.AQUA + "Previously known as:");
            for(PlayerData p : oldProfiles){
                sender.sendMessage(ChatColor.AQUA + " - " + ChatColor.GRAY + p.getName());
            }
        }

        //Send last online date or if they're online now
        if(util.isOnline(player.getUuid())){
            sender.sendMessage(ChatColor.AQUA + "Last Online: " + ChatColor.GREEN + "Online Now");
        }else{
            sender.sendMessage(ChatColor.AQUA + "Last Online: " + ChatColor.GREEN + player.getLastOnline());
        }

        //check if player has permission to view player IPs
        if(sender.hasPermission("profiler.status.ip")){
            sender.sendMessage(ChatColor.AQUA + "Last used IP: " + ChatColor.GREEN + player.getIp());
        }
    }

    private void sendNotes(PlayerData p, CommandSender sender, CommandHandler cmdHandler){

        /*
         Currently this is set up so users cannot see their own notes unless they have special permission
         It also assumes that people who can view their own notes can also view the notes of others. This
         is how I like it but it may not be ideal for others. I may have to change this.
         */

        //Check if player is viewing their own status only let them see their own notes if they have permission
        Player player = (Player) sender;
        //If player is looking up their own status and they don't have permission to view their own notes
        if(player.getUniqueId().toString().equals(p.getUuid()) && !player.hasPermission("profiler.notes.self")){
            return;
        }

        //Check if sender has permission to view notes
        if(sender.hasPermission("profiler.notes")){
            ArrayList<Note> notes = cmdHandler.getProfiler().getMysql().getNotes(p);
            //Check if there was an error getting notes
            if(notes == null){
                sender.sendMessage("Error getting notes");
                return;
            }

            for(Note n : notes){
                String noterName = cmdHandler.getProfiler().getMysql().getCurrentProfile(n.getNoter()).getName();
                String noterNameFormat = noterName;
                try{
                    noterNameFormat = util.formatName(noterName, n.getNoter(), cmdHandler.getProfiler());
                }catch(Exception e){

                }
                sender.sendMessage(ChatColor.GOLD + " + " + ChatColor.GRAY + n.getNote() + ChatColor.LIGHT_PURPLE + " - added by: " + noterNameFormat);
            }
        }
    }

}
