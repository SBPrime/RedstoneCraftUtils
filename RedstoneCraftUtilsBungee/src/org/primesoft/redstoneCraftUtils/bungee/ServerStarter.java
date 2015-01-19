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

import com.sun.org.apache.bcel.internal.generic.AALOAD;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;
import java.io.IOException;
import java.net.SocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.craftminecraft.bungee.bungeeyaml.bukkitapi.ConfigurationSection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import org.primesoft.redstoneCraftUtils.bungee.utils.ExceptionHelper;
import org.primesoft.redstoneCraftUtils.bungee.utils.InOutParam;
import org.primesoft.redstoneCraftUtils.bungee.utils.Ping;

/**
 *
 * @author SBPrime
 */
public class ServerStarter {

    private final static SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static int WAIT_TIME = 5000;
    private final static int RETRY = 60;

    private final Object m_mutex;

    private final HashMap<String, String> m_startupCommand;
    private final RCUtilsMain m_plugin;
    private final TaskScheduler m_scheduler;

    public ServerStarter(RCUtilsMain plugin, ConfigurationSection configuration) {
        m_plugin = plugin;
        m_mutex = new Object();
        m_startupCommand = new HashMap<String, String>();

        RCUtilsMain.log("Server startup commands:");
        if (configuration != null) {
            for (String key : configuration.getKeys(false)) {
                String command = configuration.getString(key);
                RCUtilsMain.log(" * " + key + " -> " + command);

                m_startupCommand.put(key.toLowerCase(), command);
            }
        }

        m_scheduler = m_plugin.getProxy().getScheduler();
    }

    public void startServer(final ServerInfo server, final ProxiedPlayer player) {
        if (server == null || player == null) {
            return;
        }

        final String name = server.getName().toLowerCase();
        synchronized (m_mutex) {
            if (!m_startupCommand.containsKey(name)) {
                RCUtilsMain.log("Server " + name + " not configured for auto start.");
                RCUtilsMain.say(player, "Server " + name + " is offline.");
                return;
            }

            RCUtilsMain.say(player, "Starting server " + name + "...");
            final String command = m_startupCommand.get(name);

            m_scheduler.runAsync(m_plugin, new Runnable() {

                @Override
                public void run() {
                    start(server, command, player);
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
    private void start(final ServerInfo server, String command, final ProxiedPlayer player) {
        final String serverName = server.getName();
        final Object mutex = new Object();

        try {
            Runtime.getRuntime().exec(command);
        } catch (final IOException ex) {
            invoke(new Runnable() {

                @Override
                public void run() {
                    ExceptionHelper.printException(ex, "Unable to start server");
                    RCUtilsMain.say(player, "Unable to start server " + serverName + ", server is offline.");
                }
            });

            return;
        }

        final InOutParam<Boolean> isOnline = InOutParam.Ref(false);
        final InOutParam<Boolean> tryingToConnect = InOutParam.Ref(false);

        for (int i = 0; i < RETRY && !isOnline.getValue(); i++) {
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException ex) {
            }

            final int run = i;
            invoke(new Runnable() {
                @Override
                public void run() {
                    testConnection(run, mutex,
                            isOnline, tryingToConnect,
                            server, player);
                }
            });
        }

        if (isOnline.getValue()) {
            return;
        }

        invoke(new Runnable() {
            @Override
            public void run() {
                RCUtilsMain.log("Server did not start in the required time");
                RCUtilsMain.say(player, "Unable to start server " + serverName + ", server is offline.");
            }
        });
    }

    private void testConnection(int run, final Object mutex,
            final InOutParam<Boolean> isOnline, final InOutParam<Boolean> tryingToConnect,
            ServerInfo server, ProxiedPlayer player) {

        ServerPing ping = Ping.ping(server);
        Date date = null;

        if (ping != null) {
            String description = ping.getDescription();
            try {
                date = DATE_FORMATER.parse(description);
            } catch (ParseException ex) {
                date = null;
            }
        }

        long delta = -1;
        if (date != null) {
            long nowTime = System.currentTimeMillis();
            long serverTime = date.getTime();

            delta = Math.abs(serverTime - nowTime);
        }

        if (delta != -1 && delta < 5000 && !tryingToConnect.getValue()) {
            RCUtilsMain.log("Got ping trying to connect.");
            tryingToConnect.setValue(true);

            player.connect(server);
            synchronized (mutex) {
                isOnline.setValue(true);
            }
            
            return;
        }

        RCUtilsMain.say(player, "Waiting for server " + server.getName() + " to start (" + run + "/" + RETRY + ")");
    }

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
