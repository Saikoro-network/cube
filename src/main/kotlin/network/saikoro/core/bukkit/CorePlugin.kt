/**
 * Copyright (C) 2022 TTtie
 *
 * This file is part of Cube.
 *
 * Cube is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Cube.  If not, see <http://www.gnu.org/licenses/>.
 */
package network.saikoro.core.bukkit

import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener

@Suppress("unused")
class CorePlugin : JavaPlugin(), PluginMessageListener, Listener {

    override fun onEnable() {
        // No messages need to be handled right now
        // server.messenger.registerIncomingPluginChannel(this, Constants.PluginMessagingChannel, this)
        // server.messenger.registerOutgoingPluginChannel(this, Constants.PluginMessagingChannel)

        // No listeners need to be handled right now
        // server.pluginManager.registerEvents(this, this)

        logger.info("Vcela was loaded")
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        // No messages need to be handled right now
        return

        /*if (channel != Constants.PluginMessagingChannel) return
        ByteArrayInputStream(message).use {
            DataInputStream(it).use {
                // handle any incoming messages as needed
            }
        }*/
    }
}
