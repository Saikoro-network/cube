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
import net.md_5.bungee.api.plugin.Command
import network.saikoro.core.bungee.CorePlugin
import net.kyori.adventure.title.Title as AdventureTitle

abstract class BaseAlertCommand(
    protected val plugin: CorePlugin,
    cmdName: String,
    permission: String,
    vararg aliases: String
) : Command(cmdName, permission, *aliases) {

    fun getAdventureTitle(subTitle: Component): AdventureTitle {
        return AdventureTitle.title(
            ALERT_TEXT,
            subTitle
        )
    }

    companion object {
        val ALERT_TEXT = Component.text("Alert", NamedTextColor.RED)

        val ADVENTURE_ALERT_PREFIX = Component.text("[", NamedTextColor.DARK_GRAY)
            .append(ALERT_TEXT)
            .append(Component.text("]"))
            .append(Component.text(" ", NamedTextColor.WHITE))
    }

}
