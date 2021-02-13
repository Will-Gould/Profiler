package com.thejuiceman.profiler.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {
    /**
     * Gets the maximum amount of arguments required, -1 if no maximum.
     *
     * @return The maximum number of arguments required, -1 if no maximum.
     */
    public int maxArgs();

    /**
     * Gets the minimum amount of arguments required.
     *
     * @return The minimum number of arguments required.
     */
    public int minimumArgs();

    /**
     * Whether the command needs a player context or not.
     *
     * @return True if requires a player, false if not.
     */
    public boolean needsPlayer();

    /**
     * A regex pattern that allows for alternatives to the command.
     *
     * @return The regex pattern to match.
     */
    public String pattern();

    /**
     * Gets the permission required to execute this command.
     *
     * @return The permission required.
     */
    public String permission();

    /**
     * Gets the usage message for this command.
     *
     * @return The usage message.
     */
    public String usage();

}
