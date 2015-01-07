/*
 * RedstoneCraftUtils a custom plugin for the RedstoneCraft server.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) RedstoneCraftUtils contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer. 
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution,
 * 3.  Redistributions of source code, with or without modification, in any form 
 *     other then free of charge is not allowed,
 * 4.  Redistributions in binary form in any form other then free of charge is 
 *     not allowed.
 * 5.  Any derived work based on or containing parts of this software must reproduce 
 *     the above copyright notice, this list of conditions and the following 
 *     disclaimer in the documentation and/or other materials provided with the 
 *     derived work.
 * 6.  Any derived work based on or containing parts of this software must be released
 *     under the same license as the original work
 * 7.  The original author of the software is allowed to change the license 
 *     terms or the entire license of the software as he sees fit.
 * 8.  The original author of the software is allowed to sublicense the software 
 *     or its parts using any license terms he sees fit.
 * 9.  You are not permitted to disable or bypass the MCStats statistics gathering
 * 10. You are allowed to use this plugin on a server or server network that is 
 *     completely free:
 *     * no payed ranks,
 *     * no premium items,
 *     * no pay to play,
 *     * no pay to win,
 *     * no donations system in place
 * 11. If you want to use this plugin on a server that brings you money contact
 *     the plugin author for sublicense.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.redstoneCraftUtils;

//import com.iKeirNez.PluginMessageApiPlus.implementations.BukkitPacketManager;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.redstoneCraftUtils.mcstats.MetricsLite;

/**
 *
 * @author SBPrime
 */
public class RCUtilsMain extends JavaPlugin {
    private static final Logger s_log = Logger.getLogger("Minecraft.AWE");

    private static ConsoleCommandSender s_console;

    private static String s_prefix = null;

    private static String s_logFormat = "%s %s";

    private MetricsLite m_metrics;
//    private BukkitPacketManager m_packetManager;    

    public static void log(String msg) {
        if (s_log == null || msg == null || s_prefix == null) {
            return;
        }

        s_log.log(Level.INFO, String.format(s_logFormat, s_prefix, msg));
    }

    public static void say(Player player, String msg) {
        if (player == null) {
            s_console.sendRawMessage(msg);
        } else {
            player.sendRawMessage(msg);
        }
    }

    /*    public BukkitPacketManager getPacketManager()
     {
     return m_packetManager;
     }*/
    @Override
    public void onEnable() {
        PluginDescriptionFile desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());

        try {
            m_metrics = new MetricsLite(this);
            m_metrics.start();
        }
        catch (IOException e) {
            log("Error initializing MCStats: " + e.getMessage());
        }

        if (!ConfigProvider.load(this)) {
            log("Error loading config");
        }

        s_console = getServer().getConsoleSender();
        log("Enabled");
    }

    @Override
    public void onDisable() {
        log("Disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label,
                             String[] args) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        BlockCommandSender block = (sender instanceof BlockCommandSender) ? (BlockCommandSender) sender : null;
        Location location = null;
        if (player != null) {
            location = player.getLocation();
        } else if (block != null) {
            location = block.getBlock().getLocation();
        }

        String cmd = command.getName();
        if (cmd.equalsIgnoreCase(Commands.COMMAND_GETCOMMANDBLOCK) && PermissionManager.isAllowed(player, PermissionManager.Perms.GetCommandBlock)) {
            return doGetCommandBlock(player);
        } else if (cmd.equalsIgnoreCase(Commands.COMMAND_GETBLOCKNAME) && PermissionManager.isAllowed(player, PermissionManager.Perms.GetName)) {
            return doGetName(player);
        } else if (cmd.equalsIgnoreCase(Commands.COMMAND_SETBLOCKNAME) && PermissionManager.isAllowed(player, PermissionManager.Perms.SetName)) {
            return doSetName(player, args);
        } else if (cmd.equalsIgnoreCase(Commands.COMMAND_SETBLOCKCOMMAND) && PermissionManager.isAllowed(player, PermissionManager.Perms.SetCommand)) {
            return doSetCommand(player, args);
        } else if (cmd.equalsIgnoreCase(Commands.COMMAND_GETBLOCKCOMMAND) && PermissionManager.isAllowed(player, PermissionManager.Perms.GetCommand)) {
            return doGetCommand(player);
        } else if (cmd.equalsIgnoreCase(Commands.COMMAND_RELOADCOMMAND) && PermissionManager.isAllowed(player, PermissionManager.Perms.Reload)) {
            doReloadConfig(player);
            return true;
        } else if (cmd.equalsIgnoreCase(Commands.COMMAND_TESTCOMMAND) && player.isOp()) {
            doTestCommand(player);
            return true;
        }
        return false;
    }

    private boolean doGetCommandBlock(Player player) {
        if (player == null) {
            say(player, "Command available only ingame");
            return true;
        }

        final ItemStack is = new ItemStack(Material.COMMAND, 64);
        player.setItemInHand(is);

        return true;
    }

    /**
     * Do reload configuration command
     *
     * @param player
     */
    private void doReloadConfig(Player player) {
        log(player != null ? player.getName() : "console " + " reloading config...");

        reloadConfig();

        if (!ConfigProvider.load(this)) {
            say(player, "Error loading config");
            return;
        }

        say(player, "Config reloaded");
    }

    private CommandBlock getComandBlock(Player player) {
        Block b = player.getTargetBlock(null, 200);
        if (b == null) {
            return null;
        }
        BlockState bs = b.getState();
        if (bs == null || !(bs instanceof CommandBlock)) {
            return null;
        }

        return (CommandBlock) bs;
    }

    private boolean doGetName(Player player) {
        if (player == null) {
            say(player, "Command available only ingame");
            return true;
        }

        CommandBlock cb = getComandBlock(player);
        if (cb == null) {
            say(player, "You need to look at a command block");
            return true;
        }

        say(player, ChatColor.BLUE + "Block name: " + ChatColor.WHITE + cb.getName());

        return true;
    }

    private boolean doSetName(Player player, String[] args) {
        if (player == null) {
            say(player, "Command available only ingame");
            return true;
        }

        if (args.length < 1) {
            say(player, "Usage: " + Commands.COMMAND_SETBLOCKNAME + " <name>");
            return true;
        }

        CommandBlock cb = getComandBlock(player);
        if (cb == null) {
            say(player, "You need to look at a command block");
            return true;
        }

        StringBuilder sb = new StringBuilder();
        for (String s : args) {
            sb.append(s);
            sb.append(" ");
        }

        String name = sb.toString().trim();
        cb.setName(name);
        cb.update(true, true);
        say(player, ChatColor.BLUE + "New name: " + ChatColor.WHITE + name);

        return true;
    }

    private boolean doSetCommand(Player player, String[] args) {
        if (player == null) {
            say(player, "Command available only ingame");
            return true;
        }

        if (args.length < 1) {
            say(player, "Usage: " + Commands.COMMAND_SETBLOCKCOMMAND + " <name>");
            return true;
        }

        CommandBlock cb = getComandBlock(player);
        if (cb == null) {
            say(player, "You need to look at a command block");
            return true;
        }

        HashSet<String> allowed = ConfigProvider.getAllowedCommand();
        String cmd = args[0].toLowerCase();
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }

        if (!allowed.contains(cmd) && !player.isOp()) {
            say(player, ChatColor.RED + "Command " + ChatColor.WHITE + cmd + ChatColor.RED + " not allowed.");
            return true;
        }

        StringBuilder sb = new StringBuilder();
        for (String s : args) {
            sb.append(s);
            sb.append(" ");
        }

        cmd = sb.toString().trim();
        cb.setCommand(cmd);
        cb.update(true, true);
        say(player, ChatColor.BLUE + "New command: " + ChatColor.WHITE + cmd);
        return true;
    }

    private boolean doGetCommand(Player player) {
        if (player == null) {
            say(player, "Command available only ingame");
            return true;
        }

        CommandBlock cb = getComandBlock(player);
        if (cb == null) {
            say(player, "You need to look at a command block");
            return true;
        }

        say(player, ChatColor.BLUE + "Block command: " + ChatColor.WHITE + cb.getCommand());

        return true;
    }

    private void doTestCommand(Player player) {
        if (player == null) {
            say(player, "Command available only ingame");
        }

        
    }
}
