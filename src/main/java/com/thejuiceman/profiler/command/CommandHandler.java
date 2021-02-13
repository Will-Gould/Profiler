package com.thejuiceman.profiler.command;

import com.thejuiceman.profiler.Profiler;
import com.thejuiceman.profiler.command.commands.AddNote;
import com.thejuiceman.profiler.command.commands.Resolve;
import com.thejuiceman.profiler.command.commands.Status;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This plugin uses a Command handling system based off the Jail plugin written by graywolf336
 */

public class CommandHandler {

    private LinkedHashMap<String, Command> commands;
    private Profiler profiler;

    public CommandHandler(Profiler profiler){
        this.profiler = profiler;
        this.commands = new LinkedHashMap<String, Command>();
        loadCommands();
    }

    public void handleCommand(CommandSender sender, String label, String[] args){
        List<Command> matches = getMatches(label);

        //Check if no matching commands
        if(matches.isEmpty()){
            sender.sendMessage(ChatColor.RED + "Unknown command!");
            return;
        }

        //If more than one commands found send all command's help messages
        if(matches.size() > 1){
            for(Command c : matches){
                showUsage(sender, c);
            }
            return;
        }

        Command c = matches.get(0);
        CommandInfo i = c.getClass().getAnnotation(CommandInfo.class);

        //Check if sender has permissions
        if(!sender.hasPermission(i.permission())){
            sender.sendMessage("You do not have permission to use this command");
            return;
        }

        //Check if sender is player
        if(i.needsPlayer() && !(sender instanceof Player)){
            sender.sendMessage("You must be a player to use this command");
            return;
        }

        // Now, let's check the size of the arguments passed for min value
        if(args.length < i.minimumArgs()) {
            showUsage(sender, c);
            return;
        }

        // Then, if the maximumArgs doesn't equal -1, we need to check if the size of the arguments passed is greater than the maximum args.
        if(i.maxArgs() != -1 && i.maxArgs() < args.length) {
            showUsage(sender, c);
            return;
        }

        //All clear run the command
        try{
            if(!c.execute(this, sender, args)){
                showUsage(sender, c);
                return;
            }
            return;
        }catch(Exception e){
            //e.printStackTrace();
            this.profiler.getLogger().severe("An error occurred while executing this command");
        }
    }

    private void showUsage(CommandSender sender, Command command) {
        CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);
        if(!sender.hasPermission(info.permission())) return;
        sender.sendMessage(info.usage());
    }

    private void loadCommands(){
        load(AddNote.class);
        load(Status.class);
        load(Resolve.class);
    }

    private void load(Class<? extends Command> c){
        CommandInfo info = c.getAnnotation(CommandInfo.class);
        if(info == null){
            return;
        }
        try{
            commands.put(info.pattern(), c.getDeclaredConstructor().newInstance());
        }catch(Exception e){
            //TODO Error message system
            e.printStackTrace();
        }
    }

    private List<Command> getMatches(String command) {
        List<Command> result = new ArrayList<>();
        for(Map.Entry<String, Command> entry : commands.entrySet()) {
            if(command.matches(entry.getKey())) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    public Profiler getProfiler(){
        return this.profiler;
    }
}
