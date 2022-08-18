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
package network.saikoro.core.bungee.annoyances

import net.md_5.bungee.api.connection.ProxiedPlayer
import network.saikoro.core.bungee.CorePlugin
import network.saikoro.core.bungee.annoyances.impl.AutoMessageAnnoyance
import java.util.concurrent.CompletableFuture

class AnnoyanceManager(internal val plugin: CorePlugin) {
    private val annoyances = HashMap<String, Annoyance>()

    init {
        annoyances["automessages"] = AutoMessageAnnoyance(plugin)
    }

    fun enableAnnoyance(name: String) {
        val annoyance = annoyances[name] ?: throw Exception("This annoyance does not exist")


        val cfgSection = plugin.config.getSection("annoyances").getSection(name)
        if (!annoyance.isEnabled) {
            annoyance.isEnabled = annoyance.enable(cfgSection)
        }
    }

    fun disableAnnoyance(name: String) {
        val annoyance = annoyances[name] ?: throw Exception("This annoyance does not exist")

        if (annoyance.isEnabled) {
            annoyance.disable()
            annoyance.isEnabled = false
        }
    }

    fun disableAnnoyanceForPlayer(name: String, player: ProxiedPlayer): CompletableFuture<Void> {
        val annoyance =
            annoyances[name] ?: return CompletableFuture.failedFuture(Exception("This annoyance does not exist"))

        return annoyance.disableForPlayer(player)
    }

    fun enableAnnoyanceForPlayer(name: String, player: ProxiedPlayer): CompletableFuture<Void> {
        val annoyance =
            annoyances[name] ?: return CompletableFuture.failedFuture(Exception("This annoyance does not exist"))

        return annoyance.enableForPlayer(player)
    }

    fun enableDefaultAnnoyances() {
        for ((k) in annoyances) {
            val cfgSection = plugin.config.getSection("annoyances").getSection(k)
            if (cfgSection?.getBoolean("enabled") == true) {
                plugin.logger.info("Enabling default annoyance $k")
                enableAnnoyance(k)
            }
        }
    }

    fun disableAllAnnoyances() {
        annoyances.values.forEach {
            if (it.isEnabled) {
                it.disable()
                it.isEnabled = false
            }
        }
    }

    class Exception(msg: String) : kotlin.Exception(msg)
}
