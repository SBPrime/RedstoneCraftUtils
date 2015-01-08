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
package org.primesoft.redstoneCraftUtils.commands;

import org.primesoft.redstoneCraftUtils.commands.utils.CommandDescriptor;
import java.util.HashSet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.primesoft.redstoneCraftUtils.ConfigProvider;
import org.primesoft.redstoneCraftUtils.RCUtilsMain;

/**
 *
 * @author SBPrime
 */
public class CommandBlockCommands {

    private static void say(Player p, String msg) {
        RCUtilsMain.say(p, msg);
    }

    /**
     * Get the block that the player is looking at
     *
     * @param player
     * @return
     */
    private static CommandBlock getComandBlock(Player player) {
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

    @CommandDescriptor(
            command = "commandBlockGet",
            aliases = {"cbget"},
            usage = "/<command>",
            permission = "RCUtils.CommandBlock.GetBlock",
            description = "Get command block"
    )
    public static boolean commandBlockGet(Player player) {
        if (!ConfigProvider.isCbEnabled()) {
            return false;
        }

        final ItemStack is = new ItemStack(Material.COMMAND, 64);
        player.setItemInHand(is);
        return true;
    }

    @CommandDescriptor(
            command = "commandBlockGetName",
            aliases = {"cbgetname"},
            usage = "/<command>",
            description = "Get command block name",
            permission = "RCUtils.CommandBlock.GetName"
    )
    public static boolean commandBlockGetName(Player player) {
        if (!ConfigProvider.isCbEnabled()) {
            return false;
        }

        CommandBlock cb = getComandBlock(player);
        if (cb == null) {
            say(player, "You need to look at a command block");
            return true;
        }

        say(player, ChatColor.BLUE + "Block name: " + ChatColor.WHITE + cb.getName());

        return true;
    }

    @CommandDescriptor(
            command = "commandBlockSetName",
            aliases = {"cbsetname"},
            usage = "/<command> [name]",
            description = "Set command block name",
            permission = "RCUtils.CommandBlock.SetName"
    )
    public static boolean commandBlockSetName(Player player, String[] args) {
        if (!ConfigProvider.isCbEnabled()) {
            return false;
        }

        if (args.length < 1) {
            say(player, "Usage: commandBlockSetName <name>");
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

    @CommandDescriptor(
            command = "commandBlockGetCommand",
            aliases = {"cbgetcommand"},
            usage = "/<command>",
            description = "Get command block command",
            permission = "RCUtils.CommandBlock.GetCommand"
    )
    public static boolean commandBlockGetCommand(Player player) {
        if (!ConfigProvider.isCbEnabled()) {
            return false;
        }

        CommandBlock cb = getComandBlock(player);
        if (cb == null) {
            say(player, "You need to look at a command block");
            return true;
        }

        say(player, ChatColor.BLUE + "Block command: " + ChatColor.WHITE + cb.getCommand());

        return true;
    }

    @CommandDescriptor(
            command = "commandBlockSetCommand",
            aliases = "cbsetcommand",
            usage = "/<command> [newCommand]",
            description = "Set command block command",
            permission = "RCUtils.CommandBlock.SetCommand"
    )
    public static boolean commandBlockSetCommand(Player player, String[] args) {
        if (!ConfigProvider.isCbEnabled()) {
            return false;
        }

        if (args.length < 1) {
            say(player, "Usage: commandBlockSetCommand <name>");
            return true;
        }

        CommandBlock cb = getComandBlock(player);
        if (cb == null) {
            say(player, "You need to look at a command block");
            return true;
        }

        HashSet<String> allowed = ConfigProvider.getCbAllowedCommands();
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

}
