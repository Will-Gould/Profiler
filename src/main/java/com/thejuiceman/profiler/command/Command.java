package com.thejuiceman.profiler.command;

import org.bukkit.command.CommandSender;

public interface Command {
    public boolean execute(CommandHandler cmdHandler, CommandSender sender, String... args) throws Exception;
}
