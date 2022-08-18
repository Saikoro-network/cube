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

package network.saikoro.core.bungee

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.event.ProxyPingEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.protocol.ProtocolConstants
import network.saikoro.core.bungee.annoyances.AnnoyanceManager
import network.saikoro.core.bungee.commands.*
import network.saikoro.core.bungee.listener.CoreListener
import network.saikoro.core.common.Constants
import network.saikoro.core.common.db.DatabaseGetter
import network.saikoro.core.common.db.models.PlayerStatistic
import org.ktorm.database.Database
import org.ktorm.dsl.delete
import org.ktorm.dsl.insert
import org.ktorm.dsl.less
import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("unused")
class CorePlugin : Plugin(), Listener {
    private val quartersOfHour = arrayOf(0, 15, 30, 45)

    private lateinit var db: Database
    internal lateinit var config: Configuration

    private var _adv: BungeeAudiences? = null
    val adventure: BungeeAudiences
        get() {
            return _adv ?: throw IllegalStateException("Audience is not available")
        }

    private lateinit var precompiledPlayerList: Array<out ServerPing.PlayerInfo>
    private lateinit var precompiledMotdText: BaseComponent
    private lateinit var precompiledMotdTextPre116: BaseComponent

    internal lateinit var whitelistedCountries: List<String>
    internal lateinit var whitelistedAddresses: List<String>
    internal lateinit var disconnectGeoipUnknownPrecompiled: Component
    internal lateinit var disconnectGeoipBlacklistedPrecompiled: Component

    internal lateinit var discordBugHookClient: WebhookClient
    internal lateinit var discordReportHookClient: WebhookClient

    internal lateinit var lpApi: LuckPerms


    val annoyances = AnnoyanceManager(this)

    override fun onEnable() {
        _adv = BungeeAudiences.create(this)
        lpApi = LuckPermsProvider.get()

        arrayOf(
            ::GlobalAlertCommand,
            ::LocalAlertCommand,
            ::ServerSpecificAlertCommand,
            ::ReloadMOTDCommand,
            ::ReportBugCommand,
            ::ReportBugDeprecationNoticeCommand,
            ::ReportCommand,
            ::AnnoyancesCommand
        ).forEach {
            proxy.pluginManager.registerCommand(this, it(this))
        }


        proxy.pluginManager.registerListener(this, this)
        proxy.pluginManager.registerListener(this, CoreListener(this))

        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }

        val f = ensureFile(dataFolder, "config.yml", "bungee_config.yml")
        config = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(f)

        whitelistedCountries = config.getStringList("geoip.allowed_countries")
        whitelistedAddresses = config.getStringList("geoip.allowed_ips")

        disconnectGeoipBlacklistedPrecompiled = LegacyComponentSerializer.legacyAmpersand()
            .deserialize(config.getString("geoip.disconnect_blacklisted") ?: "Blacklisted GeoIP location ({country})")
        disconnectGeoipUnknownPrecompiled = LegacyComponentSerializer.legacyAmpersand()
            .deserialize(config.getString("geoip.disconnect_unknown") ?: "Unknown GeoIP location")


        reloadMOTDFiles()

        discordBugHookClient = WebhookClientBuilder(config.getString("reports.bug_webhook_url"))
            .build()

        discordReportHookClient = WebhookClientBuilder(config.getString("reports.webhook_url"))
            .build()

        db = DatabaseGetter.getInstance(
            config.getInt("db.max_pool_size", 10),
            config.getString("db.host"),
            config.getString("db.port") ?: config.getInt("db.port", 3306).toString(),
            config.getString("db.name"),
            config.getString("db.username"),
            config.getString("db.password")
        )

        proxy.scheduler.schedule(this, {
            val cal = Calendar.getInstance()

            val min = cal.get(Calendar.MINUTE)
            val sec = cal.get(Calendar.SECOND)

            if (quartersOfHour.contains(min) && sec == 0) {
                val ts = LocalDateTime.now()
                db.insert(PlayerStatistic) {
                    set(it.timestamp, ts)
                    set(it.playerCount, proxy.players.size)
                }
                db.delete(PlayerStatistic) {
                    it.timestamp less ts.minusMonths(1L)
                }
            }
        }, 0, 1, TimeUnit.SECONDS)

        annoyances.enableDefaultAnnoyances()

        logger.info("Cube was loaded.")
    }

    override fun onDisable() {
        this._adv?.close()
    }

    @EventHandler
    fun on(ev: PluginMessageEvent) {
        if (ev.tag == Constants.PluginMessagingChannel) {
            if (ev.sender is ProxiedPlayer) {
                // Prevent users from sending their own commands (assumes the knowledge of the plugin channel)
                ev.isCancelled = true
                return
            }
            if (ev.receiver is ProxiedPlayer) {
                // Catch the plugin messages sent into this plugin
                handlePluginMessage(ev.data)
                // Prevent leaking the plugin channel messages to a player
                ev.isCancelled = true
                return
            }
        }
    }

    @EventHandler
    fun on(ev: ProxyPingEvent) {
        ev.response.apply {
            players.sample = precompiledPlayerList
            descriptionComponent = if (ev.connection.version >= ProtocolConstants.MINECRAFT_1_16) precompiledMotdText
            else precompiledMotdTextPre116
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handlePluginMessage(msg: ByteArray) {
        // No-op: might be handled soon.
    }

    fun reloadMOTDFiles() {
        val motdFile = ensureFile(dataFolder, "motd.txt")
        val playerListFile = ensureFile(dataFolder, "player_list.txt")

        val motdText = Component.text()
            .append(LegacyComponentSerializer.legacyAmpersand().deserialize(motdFile.readText()))
            .build()

        precompiledMotdText = BungeeComponentSerializer.get().serialize(motdText)[0]
        precompiledMotdTextPre116 = BungeeComponentSerializer.legacy().serialize(motdText)[0]

        playerListFile.useLines {
            precompiledPlayerList = it.take(64).map { str ->
                ServerPing.PlayerInfo(
                    ChatColor.translateAlternateColorCodes('&', str),
                    UUID.randomUUID()
                )
            }.toList().toTypedArray()
        }
    }

    private fun ensureFile(path: File, child: String, sourceFileName: String = child): File {
        val f = File(path, child)

        if (!f.exists()) {
            try {
                getResourceAsStream(sourceFileName)
                    .use {
                        Files.copy(it, f.toPath())
                    }
            } catch (ex: Exception) {
                logger.severe("Couldn't copy default configuration:")
                logger.severe(ex.stackTraceToString())
            }
        }

        return f
    }
}
