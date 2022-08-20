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

package network.saikoro.core.bungee.util

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

abstract class UUIDFetcher {
    abstract fun getUniqueId(player: ProxiedPlayer): UUID

    companion object {
        private var instance: UUIDFetcher? = null

        fun get(): UUIDFetcher = if (instance != null) instance as UUIDFetcher
        else {
            instance = if (ProxyServer.getInstance().pluginManager.getPlugin("JPremium") != null) {
                JPremiumUUIDFetcher()
            } else {
                BungeeUUIDFetcher()
            }
            instance as UUIDFetcher
        }
    }
}

fun ProxiedPlayer.getUserId() = UUIDFetcher.get().getUniqueId(this)

