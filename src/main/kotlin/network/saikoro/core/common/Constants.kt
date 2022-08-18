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
package network.saikoro.core.common

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class Constants {
    companion object {
        val ADVENTURE_PREFIX = Component.text("[", NamedTextColor.DARK_GRAY)
            .append(Component.text("Cube", NamedTextColor.AQUA))
            .append(Component.text("]", NamedTextColor.DARK_GRAY))
            .append(Component.text(" ", NamedTextColor.WHITE))

        const val PluginMessagingChannel = "saikoro_core:pmc"
    }

    object Permissions {
        const val BypassGeoIP = "network.saikoro.core.bypass_geoip"
        const val SendAlert = "network.saikoro.core.send_alert"
        const val MOTDReload = "network.saikoro.core.motd_reload"
        const val SendBugReport = "network.saikoro.core.send_bug_report"
        const val SendReport = "network.saikoro.core.send_report"
        const val BaseAnnoyancePermission = "network.saikoro.core.annoyances"
    }
}
