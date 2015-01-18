/*v
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

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.redstoneCraftUtils.configuration.ConfigProvider;
import org.primesoft.redstoneCraftUtils.RCUtilsMain;
import static org.primesoft.redstoneCraftUtils.RCUtilsMain.log;
import org.primesoft.redstoneCraftUtils.commands.utils.CommandDescriptor;
import org.primesoft.redstoneCraftUtils.configuration.TeleportLocation;

/**
 *
 * @author SBPrime
 */
public class GlobalCommands {

    private static void say(Player p, String msg) {
        RCUtilsMain.say(p, msg);
    }

    @CommandDescriptor(
            command = "rcreload",
            aliases = {},
            usage = "/<command>",
            description = "Reload the redstone craft utils configuration",
            permission = "RCUtils.Reload"
    )
    public static boolean reloadConfig(JavaPlugin plugin, CommandSender cs) {
        Player player = cs instanceof Player ? (Player) cs : null;
        log(player != null ? player.getName() : "console " + " reloading config...");

        plugin.reloadConfig();

        if (!ConfigProvider.load(plugin)) {
            say(player, "Error loading config");
            return false;
        }

        say(player, "Config reloaded");
        
        RCUtilsMain.getInstance().getServerStop().sheduleTest();
        return true;
    }

    @CommandDescriptor(
            command = "spawn",
            aliases = {},
            usage = "/<command>",
            description = "Teleport to spawn",
            permission = "RCUtils.spawn"
    )
    public static boolean spawn(JavaPlugin plugin, Player player) {
        TeleportLocation spawn = ConfigProvider.getTeleportConfig().getSpawn();

        if (spawn == null || plugin == null || player == null) {
            return false;
        }

        spawn.teleport(player.getServer(), player);
        return true;
    }
}
