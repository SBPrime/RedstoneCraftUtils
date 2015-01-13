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
package org.primesoft.redstoneCraftUtils.configuration;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * This class contains configuration
 *
 * @author SBPrime
 */
public class ConfigProvider {        
    /**
     * The command block configuration
     */
    private static CommandBlockConfig m_cbConfig = CommandBlockConfig.parse(null);
    
    /**
     * The auto stop configuration
     */
    private static AutoStopConfig m_autoStopConfig = AutoStopConfig.parse(null);
    
    /**
     * The teleport configuration
     */
    private static TeleportConfig m_teleportConfig = TeleportConfig.parse(null, null);
    
    
    /**
     * The startup shell commands
     */
    private static String[] m_startup = new String[0];
    
    
    /**
     * The commandblock configuration
     * @return 
     */
    public static CommandBlockConfig getCommandBlockConfig() {
        return m_cbConfig;
    }
    
    /**
     * The teleport configuration
     * @return 
     */
    public static TeleportConfig getTeleportConfig() {
        return m_teleportConfig;
    }
        
    /**
     * The auto stop configuration
     * @return 
     */
    public static AutoStopConfig getAutoStopConfig() {
        return m_autoStopConfig;
    }
    
    
    /**
     * The startup shell commands
     * @return 
     */
    public static String[] getStartup() {
        return m_startup;
    }
    
    /**
     * Load configuration
     *
     * @param plugin parent plugin
     * @return true if config loaded
     */
    public static boolean load(JavaPlugin plugin) {
        if (plugin == null) {
            return false;
        }

        plugin.saveDefaultConfig();        

        Configuration config = plugin.getConfig();
        ConfigurationSection mainSection = config.getConfigurationSection("RCUtils");
        if (mainSection == null) {
            return false;
        }

        m_cbConfig = CommandBlockConfig.parse(mainSection.getConfigurationSection("commandBlocks"));
        m_autoStopConfig = AutoStopConfig.parse(mainSection.getConfigurationSection("autoStop"));
        m_teleportConfig = TeleportConfig.parse(plugin.getServer(), mainSection.getConfigurationSection("teleport"));
        
        m_startup = mainSection.getStringList("startup").toArray(new String[0]);
        
        return true;
    }
}