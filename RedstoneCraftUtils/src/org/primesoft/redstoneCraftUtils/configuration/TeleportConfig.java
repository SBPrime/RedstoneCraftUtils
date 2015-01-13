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

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.primesoft.redstoneCraftUtils.utils.InOutParam;
import org.primesoft.redstoneCraftUtils.utils.IntUtils;

/**
 *
 * @author SBPrime
 */
public class TeleportConfig {

    /**
     * The default teleport configuration
     */
    private final static TeleportConfig m_default = new TeleportConfig(null, null, null);

    /**
     * parse the configuration section
     *
     * @param section
     * @return
     */
    public static TeleportConfig parse(Server server, ConfigurationSection section) {
        if (section == null || server == null) {
            return m_default;
        }

        Location spawn = parse(server, section.getString("spawn", ""));
        Location join = parse(server, section.getString("join", ""));
        Location death = parse(server, section.getString("death", ""));

        return new TeleportConfig(spawn, join, death);
    }

    /**
     * Try to parse the location string
     *
     * @param s
     * @return
     */
    private static Location parse(Server server, String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }

        String[] parts = s.split("#");
        if (parts == null || parts.length != 6) {
            return null;
        }

        InOutParam<Integer> x = InOutParam.Out();
        InOutParam<Integer> y = InOutParam.Out();
        InOutParam<Integer> z = InOutParam.Out();
        InOutParam<Integer> yaw = InOutParam.Out();
        InOutParam<Integer> pitch = InOutParam.Out();

        if (!IntUtils.tryParseInteger(parts[1], x)
        || !IntUtils.tryParseInteger(parts[2], y)
        || !IntUtils.tryParseInteger(parts[3], z)
        || !IntUtils.tryParseInteger(parts[4], yaw)
        || !IntUtils.tryParseInteger(parts[5], pitch)) {
            return null;
        }

        World w = server.getWorld(parts[0]);
        if (w == null) {
            return null;
        }

        return new Location(w, x.getValue(), y.getValue(), z.getValue(), yaw.getValue(), pitch.getValue());
    }

    private final Location m_spawn;
    private final Location m_join;
    private final Location m_death;

    /**
     * The spawn
     *
     * @return
     */
    public Location getSpawn() {
        return m_spawn;
    }

    /**
     * Server join location
     *
     * @return
     */
    public Location getJoin() {
        return m_join;
    }

    /**
     * The death tp location
     *
     * @return
     */
    public Location getDeath() {
        return m_death;
    }

    private TeleportConfig(Location spawn, Location join, Location death) {
        m_spawn = spawn;
        m_join = join;
        m_death = death;
    }
}
