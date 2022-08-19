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

import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import network.saikoro.core.bungee.CorePlugin
import network.saikoro.core.common.Constants
import java.time.ZonedDateTime

class ReportCommand(plugin: CorePlugin) : BaseReportCommand(plugin, "report", Constants.Permissions.SendReport) {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is ProxiedPlayer) {
            plugin.adventure.sender(sender).sendMessage(
                Constants.ADVENTURE_PREFIX
                    .append(Component.text("Tento příkaz nelze použít z konzole!", NamedTextColor.RED))
            )

            return
        }

        if (args.size < 2) {
            plugin.adventure.sender(sender).sendMessage(
                Constants.ADVENTURE_PREFIX
                    .append(Component.text("Použití: /reportbug <...text>", NamedTextColor.RED))
            )

            return
        }

        val reportPlayer = plugin.proxy.getPlayer(args[0]) ?: return plugin.adventure.sender(sender)
            .sendMessage(
                ADVENTURE_REPORTS_PREFIX
                    .append(Component.text("Tento hráč se nenachází na serveru!", NamedTextColor.RED))
            )

        if (reportPlayer == sender) {
            plugin.adventure.sender(sender)
                .sendMessage(
                    ADVENTURE_REPORTS_PREFIX
                        .append(Component.text("Proč bys nahlašoval sám sebe? ;(", NamedTextColor.RED))
                )
            return
        }

        val reason = args.drop(1).joinToString(" ").apply {
            substring(0..kotlin.math.min(256, length - 1))
        }

        sendReportEmbeds(
            listOf(
                WebhookEmbedBuilder()
                    .setTitle(WebhookEmbed.EmbedTitle("\uD83D\uDC65 Nahlášení hráče", null))
                    .setAuthor(
                        WebhookEmbed.EmbedAuthor(
                            "${sender.name} (ze serveru ${sender.server.info.name})",
                            "https://visage.surgeplay.com/bust/512/${sender.uniqueId}", // TODO: Replace with our skin service whenever that's implemented
                            null
                        )
                    )
                    .addField(WebhookEmbed.EmbedField(false, "Hráč", "`${reportPlayer.name}`"))
                    .addField(WebhookEmbed.EmbedField(false, "Server", "`${reportPlayer.server.info.name}`"))
                    .addField(WebhookEmbed.EmbedField(false, "Důvod", reason))
                    .setTimestamp(ZonedDateTime.now())
                    .setColor(0xFFFF00)
                    .build()
            ),
            false
        ).thenAcceptAsync {
            plugin.adventure.permission(Constants.Permissions.NotifyReports)
                .sendMessage(
                    ADVENTURE_REPORTS_PREFIX
                        .append(Component.text(reportPlayer.name, NamedTextColor.RED))
                        .append(Component.text(" "))
                        .append(
                            Component.text("(", NamedTextColor.DARK_GRAY)
                                .append(Component.text(reportPlayer.server.info.name, NamedTextColor.GOLD))
                                .clickEvent(ClickEvent.suggestCommand("/server ${reportPlayer.server.info.name}"))
                                .append(Component.text(")", NamedTextColor.DARK_GRAY))
                        )
                        .append(Component.text(" - "))
                        .append(
                            Component.text(reason, NamedTextColor.WHITE)
                                .decorate(TextDecoration.ITALIC)
                        )
                )

            plugin.adventure.sender(sender)
                .sendMessage(
                    ADVENTURE_REPORTS_PREFIX
                        .append(Component.text("Report byl odeslán. Děkujeme za nahlášení!", NamedTextColor.GREEN))
                )
        }.exceptionally {
            plugin.adventure.sender(sender)
                .sendMessage(
                    ADVENTURE_REPORTS_PREFIX
                        .append(
                            Component.text(
                                "Nastala chyba při odesílání reportu, zkus to znovu, nebo nás kontaktuj ručně na ",
                                NamedTextColor.RED
                            )
                                .append(
                                    Component.text("Discordu", TextColor.color(0x7289da))
                                        .clickEvent(ClickEvent.openUrl("https://link.saikoro.eu/discord"))
                                )
                                .append(Component.text("."))
                        )
                )

            it.printStackTrace()
            null // WTF: kotlin infers Void! for this
        }
    }

}
