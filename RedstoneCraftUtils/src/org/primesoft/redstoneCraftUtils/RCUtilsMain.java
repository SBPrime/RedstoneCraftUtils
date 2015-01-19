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

import java.io.BufferedReader;
import org.primesoft.redstoneCraftUtils.configuration.ConfigProvider;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.primesoft.redstoneCraftUtils.commands.CommandBlockCommands;
import org.primesoft.redstoneCraftUtils.commands.GlobalCommands;
import org.primesoft.redstoneCraftUtils.commands.utils.CommandManager;
import org.primesoft.redstoneCraftUtils.mcstats.MetricsLite;

/**
 *
 * @author SBPrime
 */
public class RCUtilsMain extends JavaPlugin {

    private static final int TPS = 20;

    private static final Logger s_log = Logger.getLogger("Minecraft.AWE");

    private static ConsoleCommandSender s_console;

    private static String s_prefix = null;

    private static final String s_logFormat = "%s %s";

    private static RCUtilsMain s_instance;

    private MetricsLite m_metrics;

    private CommandManager m_commandManager;

    /**
     * The player listener
     */
    private PlayerListener m_listener;
    
    /**
     * The server ping listener
     */
    private PingListener m_pingListener;

    /**
     * THe ping task
     */
    private BukkitTask m_pingTask;

    /**
     * The server stop
     */
    private ServerStop m_serverStop;

    public static RCUtilsMain getInstance() {
        return s_instance;
    }

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

    /**
     * The server stop service
     *
     * @return
     */
    public ServerStop getServerStop() {
        return m_serverStop;
    }

    @Override
    public void onEnable() {
        PluginDescriptionFile desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());
        s_console = getServer().getConsoleSender();
        s_instance = this;
        try {
            m_metrics = new MetricsLite(this);
            m_metrics.start();
        } catch (IOException e) {
            log("Error initializing MCStats: " + e.getMessage());
        }

        if (!ConfigProvider.load(this)) {
            log("Error loading config");
        }

        m_commandManager = new CommandManager(this);
        m_commandManager.initializeCommands(GlobalCommands.class);
        m_commandManager.initializeCommands(CommandBlockCommands.class);

        final PluginManager pm = getServer().getPluginManager();
        m_listener = new PlayerListener(this);
        m_pingListener = new PingListener();

        pm.registerEvents(m_listener, this);
        final RCUtilsMain main = this;
        
        getServer().getScheduler().runTaskLater(this, new Runnable() {

            @Override
            public void run() {
                pm.registerEvents(m_pingListener, main);
            }
        }, TPS * 2);

        executeStartup();

        m_serverStop = new ServerStop(this);
        m_serverStop.sheduleTest();

        log("Enabled");
    }

    private void executeStartup() {
        Runtime rt = Runtime.getRuntime();
        for (String cmd : ConfigProvider.getStartup()) {
            try {
                log("Executing: " + cmd);
                Process pr = rt.exec(cmd);
                pr.waitFor();
                BufferedReader reader = new BufferedReader(new InputStreamReader(pr.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    log("Out: " + line);
                }
            } catch (IOException ex) {
                log("Error executing startup command");
            } catch (InterruptedException ex) {
                log("Error executing startup command");
            }
        }
    }

    @Override
    public void onDisable() {
        m_serverStop.stop();
        log("Disabled");
    }
}
