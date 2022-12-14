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
import network.saikoro.core.bungee.util.getUserId
import network.saikoro.core.common.Constants
import network.saikoro.core.common.db.models.Ticket
import network.saikoro.core.common.db.models.TicketBlocks.ticketBlocks
import network.saikoro.core.common.db.models.TicketMessage
import network.saikoro.core.common.db.models.TicketMessages.ticketMessages
import network.saikoro.core.common.db.models.TicketStatus
import network.saikoro.core.common.db.models.Tickets.tickets
import network.saikoro.core.common.db.models.UserAccounts.users
import org.apache.commons.text.StringEscapeUtils.escapeHtml4
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.find
import java.time.ZonedDateTime

class ReportCommand(plugin: CorePlugin) : BaseReportCommand(plugin, "report", Constants.Permissions.SendReport) {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is ProxiedPlayer) {
            plugin.adventure.sender(sender).sendMessage(
                Constants.ADVENTURE_PREFIX
                    .append(Component.text("Tento p????kaz nelze pou????t z konzole!", NamedTextColor.RED))
            )

            return
        }

        if (args.size < 2) {
            plugin.adventure.sender(sender).sendMessage(
                Constants.ADVENTURE_PREFIX
                    .append(Component.text("Pou??it??: /report <player> <...text>", NamedTextColor.RED))
            )

            return
        }

        val reportPlayer = plugin.proxy.getPlayer(args[0]) ?: return plugin.adventure.sender(sender)
            .sendMessage(
                ADVENTURE_REPORTS_PREFIX
                    .append(Component.text("Tento hr???? se nenach??z?? na serveru!", NamedTextColor.RED))
            )

        if (reportPlayer == sender) {
            plugin.adventure.sender(sender)
                .sendMessage(
                    ADVENTURE_REPORTS_PREFIX
                        .append(Component.text("Pro?? bys nahla??oval s??m sebe? ;(", NamedTextColor.RED))
                )
            return
        }

        val dbUser = plugin.db.users.find {
            // jpremium uuids are dash-less
            it.uniqueId eq sender.uniqueId.toString().replace("-", "")
        } ?: return plugin.adventure.sender(sender)
            .sendMessage(
                ADVENTURE_REPORTS_PREFIX
                    .append(
                        Component.text("Pros??m, p??ihla?? se alespo?? jednou do ", NamedTextColor.RED)
                            .append(
                                Component.text("info panelu", NamedTextColor.BLUE)
                                    .clickEvent(ClickEvent.openUrl("https://info.saikoro.eu"))
                            )
                            .append(Component.text(" a zkus to znovu."))
                    )
            )

        if (plugin.db.ticketBlocks.find {
                it.user eq dbUser.id
            } != null) {
            return plugin.adventure.sender(sender)
                .sendMessage(
                    ADVENTURE_REPORTS_PREFIX
                        .append(
                            Component.text(
                                "Moment??ln?? nem????e?? nahla??ovat hr????e, jeliko?? je tv??j p????stup k ticket syst??mu zablokov??n.",
                                NamedTextColor.RED
                            )
                        )
                )
        }

        val reportReason = args.drop(1).joinToString(" ").apply {
            substring(0..kotlin.math.min(256, length - 1))
        }

        val creationTime = ZonedDateTime.now()

        val reportTicket = Ticket {
            author = dbUser
            title = "[${reportPlayer.server.info.name}] ${reportPlayer.name}"
            assignedGroup = "reports"
            type = "Nahl????en?? hackera"
            status = TicketStatus.WAITING_FOR_SUPPORT_RESPONSE.ordinal
            createdAt = creationTime.toEpochSecond().toString()
        }

        plugin.db.tickets.add(reportTicket)

        val ticketMessage = TicketMessage {
            ticket = reportTicket
            author = dbUser
            params = "{\"admin\": false}"
            message = "<p>Dobr?? den,<br> cht??l bych nahl??sit hr????e <strong>${reportPlayer.name}</strong> na serveru" +
                    "<strong>${reportPlayer.server.info.name}</strong>.</p>" +
                    "<p><strong>D??vod: </strong> <code>${escapeHtml4(reportReason)}</code></p>"
            timestamp = creationTime.toEpochSecond().toString()
        }

        plugin.db.ticketMessages.add(ticketMessage)

        sendReportEmbeds(
            listOf(
                WebhookEmbedBuilder()
                    .setTitle(
                        WebhookEmbed.EmbedTitle(
                            "\uD83D\uDC65 Nahl????en?? hr????e",
                            "https://info.saikoro.eu/?ticket-view-admin&id=${reportTicket.id}"
                        )
                    )
                    .setAuthor(
                        WebhookEmbed.EmbedAuthor(
                            "${sender.name} (ze serveru ${sender.server.info.name})",
                            "https://visage.surgeplay.com/bust/512/${sender.getUserId()}", // TODO: Replace with our skin service whenever that's implemented
                            null
                        )
                    )
                    .addField(WebhookEmbed.EmbedField(false, "Hr????", "`${reportPlayer.name}`"))
                    .addField(WebhookEmbed.EmbedField(false, "Server", "`${reportPlayer.server.info.name}`"))
                    .addField(WebhookEmbed.EmbedField(false, "D??vod", reportReason))
                    .setTimestamp(creationTime)
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
                            Component.text(reportReason, NamedTextColor.WHITE)
                                .decorate(TextDecoration.ITALIC)
                        )
                )

            plugin.adventure.sender(sender)
                .sendMessage(
                    ADVENTURE_REPORTS_PREFIX
                        .append(Component.text("Report byl odesl??n. D??kujeme za nahl????en??!", NamedTextColor.GREEN))
                )
        }.exceptionally {
            plugin.adventure.sender(sender)
                .sendMessage(
                    ADVENTURE_REPORTS_PREFIX
                        .append(
                            Component.text(
                                "Nastala chyba p??i odes??l??n?? reportu, zkus to znovu, nebo n??s kontaktuj ru??n?? na ",
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
