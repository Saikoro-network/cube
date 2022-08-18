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
package network.saikoro.core.bungee.listener

import com.maxmind.db.CHMCache
import com.maxmind.geoip2.DatabaseReader
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.api.event.TabCompleteResponseEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.protocol.ProtocolConstants
import network.saikoro.core.bungee.CorePlugin
import network.saikoro.core.common.Constants
import java.io.File
import java.net.InetSocketAddress
import java.util.*

class CoreListener(private val plugin: CorePlugin) : Listener {
    private val geoipDb = DatabaseReader.Builder(File(plugin.dataFolder, "GeoLite2-Country.mmdb"))
        .withCache(CHMCache()).build()

    @EventHandler
    fun on(ev: PreLoginEvent) {
        ev.registerIntent(plugin)
        plugin.lpApi.userManager.lookupUniqueId(ev.connection.name)
            .thenComposeAsync {
                if (it == null) {
                    // return a future with null to avoid errors 
                    /*CompletableFuture<User?>().apply {
                        complete(null)
                    }*/
                    plugin.lpApi.userManager.loadUser(UUID.nameUUIDFromBytes(("OfflinePlayer:${ev.connection.name}").toByteArray()))
                } else plugin.lpApi.userManager.loadUser(it)
            }
            .thenAcceptAsync {
                if (it.cachedData.permissionData.checkPermission(Constants.Permissions.BypassGeoIP)
                        .asBoolean()
                ) {
                    plugin.logger.info("Letting ${ev.connection.name} in as they have a bypass permission")
                    return@thenAcceptAsync
                }

                val sa = ev.connection.socketAddress
                if (sa !is InetSocketAddress) {
                    plugin.logger.warning("${ev.connection.name} has connected to the server via non-internet sockets, ignoring lookup.")
                    return@thenAcceptAsync
                }

                if (plugin.whitelistedAddresses.contains(sa.address.hostAddress)) {
                    plugin.logger.warning("Letting ${ev.connection.name} in (via IP whitelist: ${sa.address.hostAddress})")
                    return@thenAcceptAsync
                }

                val lr = geoipDb.tryCountry(sa.address)
                if (!lr.isPresent) {
                    plugin.logger.warning("${ev.connection.name} has connected to the server with an address from an unknown country, denying access.")
                    ev.isCancelled = true

                    ev.setCancelReason(
                        if (ev.connection.version >= ProtocolConstants.MINECRAFT_1_16) {
                            BungeeComponentSerializer.get()
                        } else {
                            BungeeComponentSerializer.legacy()
                        }.serialize(
                            plugin.disconnectGeoipUnknownPrecompiled
                        )[0]
                    )
                    return@thenAcceptAsync
                }

                val countryResp = lr.get()
                if (!plugin.whitelistedCountries.contains(countryResp.country.isoCode)) {
                    if (it.cachedData.permissionData
                            .checkPermission("${Constants.Permissions.BypassGeoIP}.${countryResp.country.isoCode}")
                            .asBoolean()
                    ) {
                        plugin.logger.warning("Letting ${ev.connection.name} from ${countryResp.country.name} in via a country-specific bypass (${Constants.Permissions.BypassGeoIP}.${countryResp.country.isoCode})")
                        return@thenAcceptAsync
                    }

                    plugin.logger.warning("${ev.connection.name} has connected to the server with an address from ${countryResp.country.name}, denying access.")
                    ev.isCancelled = true
                    ev.setCancelReason(
                        if (ev.connection.version >= ProtocolConstants.MINECRAFT_1_16) {
                            BungeeComponentSerializer.get()
                        } else {
                            BungeeComponentSerializer.legacy()
                        }.serialize(
                            plugin.disconnectGeoipBlacklistedPrecompiled
                                .replaceText(
                                    TextReplacementConfig.builder()
                                        .matchLiteral("{country}")
                                        .replacement(countryResp.country.name)
                                        .build()
                                )
                        )[0]
                    )
                    return@thenAcceptAsync
                }

                plugin.logger.info("Letting ${ev.connection.name} from ${countryResp.country.name} in via country whitelist")
            }
            .thenAcceptAsync {
                ev.completeIntent(plugin)
            }
            .exceptionally {
                ev.isCancelled = true
                ev.setCancelReason(
                    if (ev.connection.version >= ProtocolConstants.MINECRAFT_1_16) {
                        BungeeComponentSerializer.get()
                    } else {
                        BungeeComponentSerializer.legacy()
                    }.serialize(
                        Component.text(
                            "An internal error has occurred during checking access, please try again later.\n" +
                                    "If this persists, please let us know at info@saikoro.eu"
                        )
                            .color(NamedTextColor.RED)
                    )[0]
                )
                ev.completeIntent(plugin)

                plugin.logger.warning("Checking eligibility to join for ${ev.connection.name} has failed")
                plugin.logger.warning(it.stackTraceToString())

                null // WTF: kotlin infers Void! for this function
            }
    }

    @EventHandler
    fun on(ev: TabCompleteResponseEvent) {
        ev.suggestions.removeIf {
            it.contains(":") // it.startsWith("/") does not work with 1.13+
        }
    }
}

