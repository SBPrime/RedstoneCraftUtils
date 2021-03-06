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

import java.util.logging.Level;
import java.util.logging.Logger;
import net.craftminecraft.bungee.bungeeyaml.bukkitapi.ConfigurationSection;
import net.craftminecraft.bungee.bungeeyaml.bukkitapi.file.FileConfiguration;
import net.craftminecraft.bungee.bungeeyaml.pluginapi.ConfigurablePlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.PluginDescription;

/**
 *
 * @author SBPrime
 */
public class RCUtilsMain extends ConfigurablePlugin {
    private static Logger s_log;
        
    private static String s_prefix = null;

    private static final String s_logFormat = "%s %s";

    private static RCUtilsMain s_instance;
    
    public static RCUtilsMain getInstance() {
        return s_instance;
    }
    
    public static void say(ProxiedPlayer player, String msg) {
        if (player == null) {
            log(msg);
            return;
        }
        
        player.sendMessage(ChatMessageType.CHAT, new TextComponent(msg));
    }
    
    public static void log(String msg) {
        if (s_log == null || msg == null || s_prefix == null) {
            return;
        }

        s_log.log(Level.INFO, String.format(s_logFormat, s_prefix, msg));
    }

    /**
     * The event listener
     */
    private EventListener m_listener;
        
    private ServerStarter m_serverStarter;
    
    public ServerStarter getServerStarter() {
        return m_serverStarter;
    }
    
    @Override
    public void onEnable() {
        PluginDescription desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());
        s_instance = this;
        s_log = getProxy().getLogger();

        saveDefaultConfig();

        m_listener = new EventListener(this);
        getProxy().getPluginManager().registerListener(this, m_listener);
        
        FileConfiguration config = getConfig();
        ConfigurationSection mainSection = null;
        ConfigurationSection startupSection = null;
        if (config != null) {
            mainSection = config.getConfigurationSection("RCUtils");
        }
        if (mainSection != null) {
            startupSection = mainSection.getConfigurationSection("startup");
        }
        m_serverStarter = new ServerStarter(this, startupSection);
                
        log("initialized!");
    }

    @Override
    public void onDisable() {
        
        getProxy().getPluginManager().unregisterListener(m_listener);
        log("disabling!");
    }
}
