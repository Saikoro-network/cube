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
package network.saikoro.core.bungee.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.md_5.bungee.api.CommandSender
import network.saikoro.core.bungee.CorePlugin
import network.saikoro.core.common.Constants

class ServerSpecificAlertCommand(plugin: CorePlugin) :
    BaseAlertCommand(plugin, "salert", Constants.Permissions.SendAlert) {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            plugin.adventure.sender(sender).sendMessage(
                Constants.ADVENTURE_PREFIX
                    .append(Component.text("Použití: /salert <server> <...text>", NamedTextColor.RED))
            )
            return
        }

        val serverName = args.first()
        val server = plugin.proxy.servers[serverName] ?: return plugin.adventure.sender(sender).sendMessage(
            Constants.ADVENTURE_PREFIX
                .append(Component.text("Server $serverName neexistuje!", NamedTextColor.RED))
        )

        val alertComponent = LegacyComponentSerializer.legacyAmpersand()
            .deserialize(args.drop(1).joinToString(" "))
            .colorIfAbsent(NamedTextColor.WHITE)
        val title = getAdventureTitle(alertComponent)

        if (!server.players.contains(sender)) {
            plugin.adventure.sender(sender).sendMessage(
                Constants.ADVENTURE_PREFIX
                    .append(Component.text("Posílám alert na server $serverName...", NamedTextColor.GREEN))
            )
        }

        for (player in server.players) {
            plugin.adventure.player(player).apply {
                sendMessage(ADVENTURE_ALERT_PREFIX.append(alertComponent))
                showTitle(title)
            }
        }
    }

}
