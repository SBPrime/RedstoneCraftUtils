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

import java.util.Collection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.primesoft.redstoneCraftUtils.configuration.AutoStopConfig;
import org.primesoft.redstoneCraftUtils.configuration.ConfigProvider;

/**
 *
 * @author SBPrime
 */
public class ServerStop implements Runnable {

    private final static int TPS = 20;
    private final JavaPlugin m_plugin;
    private final BukkitScheduler m_scheduler;
    private BukkitTask m_task;
    private final Object m_mutex = new Object();
    private long m_lastLogout = 0;

    public ServerStop(JavaPlugin plugin) {
        m_plugin = plugin;
        m_scheduler = m_plugin.getServer().getScheduler();
    }

    public void stop() {
        synchronized (m_mutex) {
            if (m_task != null) {
                m_task.cancel();
            }
        }
    }
    
    public void sheduleTest() {        
        synchronized (m_mutex) {
            stop();
            
            AutoStopConfig config = ConfigProvider.getAutoStopConfig();
            if (!config.isEnabled()) {
                return;
            }

            int time = config.checkTime();
            if (time < 1) {
                return;
            }

            RCUtilsMain.log("Schedule auto server stop: " + time + "s");
            time *= TPS;
            m_task = m_scheduler.runTaskTimer(m_plugin, this, time, time);
        }
    }

    @Override
    public void run() {
        RCUtilsMain.log("Auto server stop test");
        long now = System.currentTimeMillis();
        long delta = now - m_lastLogout;
        
        AutoStopConfig config = ConfigProvider.getAutoStopConfig();
        if (!config.isEnabled()) {
            return;
        }
        
        long time = config.checkTime() * 1000;
        if (delta < time) {
            return;
        }
        
        Player[] players = m_plugin.getServer().getOnlinePlayers();
        if (players == null || players.length > 0) {
            return;
        }
        
        m_plugin.getServer().shutdown();
    }

    public void playerQuit() {
        m_lastLogout = System.currentTimeMillis();
    }
}
