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

import club.minnced.discord.webhook.receive.ReadonlyMessage
import club.minnced.discord.webhook.send.WebhookEmbed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.md_5.bungee.api.plugin.Command
import network.saikoro.core.bungee.CorePlugin
import java.util.concurrent.CompletableFuture

abstract class BaseReportCommand(
    protected val plugin: CorePlugin,
    cmdName: String,
    permission: String,
    vararg aliases: String
) : Command(cmdName, permission, *aliases) {
    fun sendReportEmbeds(embeds: List<WebhookEmbed>, isBug: Boolean): CompletableFuture<ReadonlyMessage> {
        return if (isBug) {
            plugin.discordBugHookClient
        } else {
            plugin.discordReportHookClient
        }.send(embeds)
    }

    protected fun getDiscordMessageLinkURL(it: ReadonlyMessage): String {
        return "https://discord.com/channels/824022859126931496/${it.channelId}/${it.id}"
    }


    companion object {
        val ADVENTURE_REPORTS_PREFIX = Component.text("[", NamedTextColor.DARK_GRAY)
            .append(Component.text("Reports", NamedTextColor.RED))
            .append(Component.text("]"))
            .append(Component.text(" ", NamedTextColor.WHITE))
    }
}
