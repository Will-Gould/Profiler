package com.thejuiceman.profiler;

import com.thejuiceman.profiler.command.CommandHandler;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

//TODO I may change the way notes are stored. Instead of storing the name of the staff member who added the note perhaps I would be better off using their UUID
//TODO It would add a bit more complexity to the way notes are viewed by the player as to get name of the staff member who added it I would have to look up their
//TODO UUID in the database to get their name

public class Profiler extends JavaPlugin implements Listener{

    private CommandHandler cmdHandler;
    private MySQL mysql;
    private FileConfiguration config;
    private Permission perms;
    private Chat chat;
    public Util util;

    public void onEnable(){

        //Create config and initialise most important variables
        Util util = new Util();
        createConfig();
        config = getConfig();
        cmdHandler = new CommandHandler(this);
        mysql = new MySQL(this);

        //Test mysql connection
        if(!mysql.testConnection()){
            //TODO Error message system
            System.out.println("Unable to connect to database!");
            //TODO disable plugin
        }

        mysql.createDatabase();

        //Hook into permissions and chat
        setupPermission();
        setupChat();
        if(perms != null) {
            System.out.println("[" + this.getName() + "] " + perms.getName() +  " hooked!");
        }else {
            System.out.println("No permissions found");
        }

        if(chat != null) {
            System.out.println("[" + this.getName() + "] " + chat.getName() +  " hooked!");
        }else {
            System.out.println("[" + this.getName() + "] " + "Not chat plugin found!");
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    public void onDisable(){
        System.out.println(this.getDescription().getVersion() + " has been disabled");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

        this.cmdHandler.handleCommand(sender, label, args);
        return true;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event){
        this.mysql.updatePlayerLogin(event);
    }

    private boolean setupPermission(){
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    private boolean setupChat(){
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        try{
            chat = rsp.getProvider();
        }catch(Exception e){

        }
        return chat != null;
    }

    protected void createConfig(){
        if(!getDataFolder().exists()){
            getDataFolder().mkdirs();
        }

        File file = new File(getDataFolder(), "config.yml");
        if(!file.exists()){
            getLogger().info("Config.yml not found, creating!");
            saveDefaultConfig();
        }
    }

    public MySQL getMysql(){
        return this.mysql;
    }

    public Permission getPerms(){
        return this.perms;
    }

    public Chat getChat(){
        return this.chat;
    }

}
