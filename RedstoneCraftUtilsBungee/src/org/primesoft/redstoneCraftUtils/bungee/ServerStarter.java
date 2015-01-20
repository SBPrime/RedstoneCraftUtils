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
package org.primesoft.redstoneCraftUtils.bungee;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.craftminecraft.bungee.bungeeyaml.bukkitapi.ConfigurationSection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import org.primesoft.redstoneCraftUtils.bungee.utils.ExceptionHelper;
import org.primesoft.redstoneCraftUtils.bungee.utils.InOutParam;
import org.primesoft.redstoneCraftUtils.bungee.utils.Ping;

/**
 *
 * @author SBPrime
 */
public class ServerStarter {

    private final static int WAIT_TIME = 5000;
    private final static int RETRY = 60;

    private void log(String log) {
        RCUtilsMain.log(log);
    }

    private void say(Iterable<ProxiedPlayer> players, String msg) {
        for (ProxiedPlayer player : players) {
            say(player, msg);
        }
    }

    private void say(ProxiedPlayer player, String msg) {
        RCUtilsMain.say(player, msg);
    }

    /**
     * The mta access mutex
     */
    private final Object m_mutex;

    /**
     * The startup commands for servers
     */
    private final HashMap<String, String> m_startupCommand;

    /**
     * PLayers waiting for server startup
     */
    private final HashMap<String, HashMap<UUID, ProxiedPlayer>> m_waitingPlayers;

    /**
     * The plugin main
     */
    private final RCUtilsMain m_plugin;
    private final TaskScheduler m_scheduler;

    public ServerStarter(RCUtilsMain plugin, ConfigurationSection configuration) {
        m_plugin = plugin;
        m_mutex = new Object();
        m_startupCommand = new HashMap<String, String>();
        m_waitingPlayers = new HashMap<String, HashMap<UUID, ProxiedPlayer>>();

        RCUtilsMain.log("Server startup commands:");
        if (configuration != null) {
            for (String key : configuration.getKeys(false)) {
                String command = configuration.getString(key);
                log(" * " + key + " -> " + command);

                m_startupCommand.put(key.toLowerCase(), command);
            }
        }

        m_scheduler = m_plugin.getProxy().getScheduler();
    }

    public void waitForServer(final ServerInfo server, 
            final ProxiedPlayer player) {
        if (server == null || player == null) {
            return;
        }

        final String serverName = server.getName().toLowerCase();
        final UUID uuid = player.getUniqueId();
        synchronized (m_mutex) {
            if (!m_startupCommand.containsKey(serverName)) {
                log("Server " + serverName + " not configured for auto start.");
                say(player, ChatColor.YELLOW + "Server "
                        + ChatColor.WHITE + serverName + ChatColor.YELLOW + " is offline. Not configured for auto start.");
                return;
            }

            final HashMap<UUID, ProxiedPlayer> playerList;
            if (m_waitingPlayers.containsKey(serverName)) {
                playerList = m_waitingPlayers.get(serverName);

                synchronized (playerList) {
                    if (playerList.containsKey(uuid)) {
                        say(player, ChatColor.YELLOW + "You are already waiting for "
                                + ChatColor.WHITE + serverName + ChatColor.YELLOW + " to start.");

                        playerList.remove(uuid);
                        playerList.put(uuid, player);
                    } else {
                        playerList.put(uuid, player);
                        say(player, ChatColor.YELLOW + "Waiting for server "
                                + ChatColor.WHITE + serverName + ChatColor.YELLOW + " to start.");
                    }
                }
                return;
            }

            playerList = new HashMap<UUID, ProxiedPlayer>();

            synchronized (playerList) {
                playerList.put(uuid, player);
                m_waitingPlayers.put(serverName, playerList);
            }
            say(player, ChatColor.YELLOW + "Starting server "
                    + ChatColor.WHITE + serverName + ChatColor.YELLOW + "...");
            final String command = m_startupCommand.get(serverName);
            log("Starting server " + serverName + "..." + command);

            m_scheduler.runAsync(m_plugin, new Runnable() {
                @Override
                public void run() {
                    startServer(server, serverName, command, playerList);
                }
            });
        }
    }

    /**
     * Start the server
     *
     * @param serverName
     * @param command
     * @param player
     */
    private void startServer(final ServerInfo server, final String serverName, 
            String command,
            final HashMap<UUID, ProxiedPlayer> playerList) {

        try {
            Runtime.getRuntime().exec(command);
        } catch (final IOException ex) {
            invoke(new Runnable() {
                @Override
                public void run() {
                    synchronized (m_mutex) {
                        m_waitingPlayers.remove(serverName);

                        ExceptionHelper.printException(ex, "Unable to start server " + serverName);

                        synchronized (playerList) {
                            filterPlayers(playerList);
                            say(playerList.values(), ChatColor.YELLOW + "Error starting server "
                                    + ChatColor.WHITE + serverName + ChatColor.YELLOW + ", server is offline.");
                        }
                    }
                }
            });

            return;
        }

        final InOutParam<Boolean> isOnline = InOutParam.Ref(false);

        for (int i = 0; i < RETRY && !isOnline.getValue(); i++) {
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException ex) {
            }

            final int run = i + 1;
            invoke(new Runnable() {
                @Override
                public void run() {
                    testConnection(server, serverName, isOnline);

                    synchronized (playerList) {
                        filterPlayers(playerList);

                        if (!isOnline.getValue()) {
                            say(playerList.values(), ChatColor.YELLOW + "Waiting for server "
                                    + ChatColor.WHITE + serverName + ChatColor.YELLOW + " to start "
                                    + ChatColor.WHITE + "(" + run + "/" + RETRY + ")");
                        }
                    }
                }
            });
        }

        invoke(new Runnable() {
            @Override
            public void run() {
                synchronized (m_mutex) {
                    m_waitingPlayers.remove(serverName);

                    synchronized (playerList) {
                        filterPlayers(playerList);

                        if (isOnline.getValue()) {
                            log("Server " + serverName + " started");
                            connect(playerList.values(), server, serverName);
                        } else {
                            log("Server " + serverName + " did not start in the required time");
                            say(playerList.values(), ChatColor.YELLOW + "Unable to start server "
                                    + ChatColor.WHITE + serverName + ChatColor.YELLOW + ", server is offline.");
                        }
                    }
                }
            }
        });
    }

    /**
     * Connect all players from list to server
     *
     * @param playerList
     * @param server
     */
    private void connect(Iterable<ProxiedPlayer> playerList, ServerInfo server,
            final String serverName) {        
        for (ProxiedPlayer player : playerList) {
            say(player, ChatColor.YELLOW + "Connectiong to " + 
                    ChatColor.WHITE + serverName + ChatColor.YELLOW + " server.");
            try {
                player.connect(server);
            } catch (Exception ex) {
                //Ignore error
            }
        }
    }

    /**
     * Filter player list
     *
     * @param playerList
     */
    private void filterPlayers(HashMap<UUID, ProxiedPlayer> playerList) {
        final ProxiedPlayer[] players = m_plugin.getProxy().getPlayers().toArray(new ProxiedPlayer[0]);
        final HashMap<UUID, ProxiedPlayer> currentPlayers = new HashMap<UUID, ProxiedPlayer>();

        for (ProxiedPlayer player : players) {
            currentPlayers.put(player.getUniqueId(), player);
        }

        UUID[] uuids = playerList.keySet().toArray(new UUID[0]);
        playerList.clear();
        for (UUID uuid : uuids) {
            if (currentPlayers.containsKey(uuid)) {
                playerList.put(uuid, currentPlayers.get(uuid));
            }
        }
    }

    /**
     * Test the server connection
     *
     * @param server
     * @param isOnline
     */
    private void testConnection(ServerInfo server, String serverName,
            final InOutParam<Boolean> isOnline) {
        if (Ping.ping(server)) {
            RCUtilsMain.log("Got ping to " + serverName);
            isOnline.setValue(true);
        }
    }

    /**
     * Invoke the runnable in the MTA
     *
     * @param runnable
     */
    private void invoke(final Runnable runnable) {
        final Object mutex = new Object();
        final InOutParam<Boolean> done = InOutParam.Out();

        m_scheduler.schedule(m_plugin, new Runnable() {
            @Override
            public void run() {
                runnable.run();

                synchronized (mutex) {
                    done.setValue(true);
                    mutex.notify();
                }
            }
        }, 100, TimeUnit.MILLISECONDS);

        if (!done.isSet()) {
            synchronized (mutex) {
                try {
                    mutex.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerStarter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
