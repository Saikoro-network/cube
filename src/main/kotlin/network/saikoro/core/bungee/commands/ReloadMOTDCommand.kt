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
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import network.saikoro.core.bungee.CorePlugin
import network.saikoro.core.common.Constants

class ReloadMOTDCommand(private val plugin: CorePlugin) : Command("motd_reload", Constants.Permissions.MOTDReload) {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        // plugin.reloadMOTDFiles()
        plugin.adventure.sender(sender).sendMessage(
            Constants.ADVENTURE_PREFIX
                .append(Component.text("Konfigurace MOTD byla uspesne prenactena.", NamedTextColor.GREEN))
        )
    }
}
