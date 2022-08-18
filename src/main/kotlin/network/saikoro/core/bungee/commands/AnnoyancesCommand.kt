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
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import network.saikoro.core.bungee.CorePlugin
import network.saikoro.core.bungee.annoyances.AnnoyanceManager
import network.saikoro.core.common.Constants

class AnnoyancesCommand(private val plugin: CorePlugin) : Command(
    "annoyances",
    Constants.Permissions.BaseAnnoyancePermission, "ann"
) {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.isEmpty()) {
            plugin.adventure.sender(sender)
                .sendMessage(
                    Constants.ADVENTURE_PREFIX
                        .append(Component.text("Použití: /annoyances <on|off> <annoyanceName>", NamedTextColor.RED))
                )

            return
        }

        when (args[0]) {
            "enable" -> {
                if (!sender.hasPermission("${Constants.Permissions.BaseAnnoyancePermission}.manage_annoyances")) return
                if (args.size < 2) {
                    plugin.adventure.sender(sender)
                        .sendMessage(
                            Constants.ADVENTURE_PREFIX
                                .append(
                                    Component.text(
                                        "Použití: /annoyances enable <annoyanceName>",
                                        NamedTextColor.RED
                                    )
                                )
                        )

                    return
                }

                val annoyanceName = args[1]

                try {
                    plugin.annoyances.enableAnnoyance(annoyanceName)

                    plugin.adventure.sender(sender)
                        .sendMessage(
                            Constants.ADVENTURE_PREFIX
                                .append(
                                    Component.text(
                                        "Rušivý element $annoyanceName byl zapnut.",
                                        NamedTextColor.GREEN
                                    )
                                )
                        )
                } catch (ex: AnnoyanceManager.Exception) {
                    plugin.adventure.sender(sender)
                        .sendMessage(
                            Constants.ADVENTURE_PREFIX
                                .append(Component.text("Tento rušivý element neexistuje!", NamedTextColor.RED))
                        )
                } catch (ex: Exception) {
                    plugin.adventure.sender(sender)
                        .sendMessage(
                            Constants.ADVENTURE_PREFIX
                                .append(
                                    Component.text(
                                        "Nastala chyba při zapínaní rušivého elementu, mrkni do konzole.",
                                        NamedTextColor.RED
                                    )
                                )
                        )

                    ex.printStackTrace()
                }
            }
            "disable" -> {
                if (!sender.hasPermission("${Constants.Permissions.BaseAnnoyancePermission}.manage_annoyances")) return
                if (args.size < 2) {
                    plugin.adventure.sender(sender)
                        .sendMessage(
                            Constants.ADVENTURE_PREFIX
                                .append(
                                    Component.text(
                                        "Použití: /annoyances disable <annoyanceName>",
                                        NamedTextColor.RED
                                    )
                                )
                        )

                    return
                }

                val annoyanceName = args[1]

                try {
                    plugin.annoyances.disableAnnoyance(annoyanceName)

                    plugin.adventure.sender(sender)
                        .sendMessage(
                            Constants.ADVENTURE_PREFIX
                                .append(
                                    Component.text(
                                        "Rušivý element $annoyanceName byl vypnut.",
                                        NamedTextColor.GREEN
                                    )
                                )
                        )
                } catch (ex: AnnoyanceManager.Exception) {
                    plugin.adventure.sender(sender)
                        .sendMessage(
                            Constants.ADVENTURE_PREFIX
                                .append(Component.text("Tento rušivý element neexistuje!", NamedTextColor.RED))
                        )
                } catch (ex: Exception) {
                    plugin.adventure.sender(sender)
                        .sendMessage(
                            Constants.ADVENTURE_PREFIX
                                .append(
                                    Component.text(
                                        "Nastala chyba při vypínání rušivého elementu, mrkni do konzole.",
                                        NamedTextColor.RED
                                    )
                                )
                        )

                    ex.printStackTrace()
                }
            }
            "on" -> {
                if (sender !is ProxiedPlayer) {
                    plugin.adventure.sender(sender)
                        .sendMessage(
                            Constants.ADVENTURE_PREFIX
                                .append(
                                    Component.text(
                                        "Tento příkaz nemůže být spuštěn z konzole!",
                                        NamedTextColor.RED
                                    )
                                )
                        )

                    return
                }
                if (args.size < 2) {
                    plugin.adventure.sender(sender)
                        .sendMessage(
                            Constants.ADVENTURE_PREFIX
                                .append(Component.text("Použití: /annoyances on <annoyanceName>", NamedTextColor.RED))
                        )

                    return
                }

                val annoyanceName = args[1]

                plugin.annoyances.enableAnnoyanceForPlayer(annoyanceName, sender)
                    .thenAcceptAsync {
                        plugin.adventure.sender(sender)
                            .sendMessage(
                                Constants.ADVENTURE_PREFIX
                                    .append(
                                        Component.text(
                                            "Rušivý element $annoyanceName byl pro tebe zapnut.",
                                            NamedTextColor.GREEN
                                        )
                                    )
                            )
                    }
                    .exceptionally {
                        if (it.cause is AnnoyanceManager.Exception) {
                            plugin.adventure.sender(sender)
                                .sendMessage(
                                    Constants.ADVENTURE_PREFIX
                                        .append(Component.text("Tento rušivý element neexistuje!", NamedTextColor.RED))
                                )
                        } else {
                            plugin.adventure.sender(sender)
                                .sendMessage(
                                    Constants.ADVENTURE_PREFIX
                                        .append(
                                            Component.text(
                                                "Nastala chyba při zapínání rušivého elementu, kontaktuj nás na Discordu.",
                                                NamedTextColor.RED
                                            )
                                        )
                                )

                            it.printStackTrace()
                        }

                        null
                    }
            }
            "off" -> {
                if (sender !is ProxiedPlayer) {
                    plugin.adventure.sender(sender)
                        .sendMessage(
                            Constants.ADVENTURE_PREFIX
                                .append(
                                    Component.text(
                                        "Tento příkaz nemůže být spuštěn z konzole!",
                                        NamedTextColor.RED
                                    )
                                )
                        )

                    return
                }
                if (args.size < 2) {
                    plugin.adventure.sender(sender)
                        .sendMessage(
                            Constants.ADVENTURE_PREFIX
                                .append(Component.text("Použití: /annoyances off <annoyanceName>", NamedTextColor.RED))
                        )

                    return
                }

                val annoyanceName = args[1]

                plugin.annoyances.disableAnnoyanceForPlayer(annoyanceName, sender)
                    .thenAcceptAsync {
                        plugin.adventure.sender(sender)
                            .sendMessage(
                                Constants.ADVENTURE_PREFIX
                                    .append(
                                        Component.text(
                                            "Rušivý element $annoyanceName byl pro tebe vypnut.",
                                            NamedTextColor.GREEN
                                        )
                                    )
                            )
                    }
                    .exceptionally {
                        if (it.cause is AnnoyanceManager.Exception) {
                            plugin.adventure.sender(sender)
                                .sendMessage(
                                    Constants.ADVENTURE_PREFIX
                                        .append(Component.text("Tento rušivý element neexistuje!", NamedTextColor.RED))
                                )
                        } else {
                            plugin.adventure.sender(sender)
                                .sendMessage(
                                    Constants.ADVENTURE_PREFIX
                                        .append(
                                            Component.text(
                                                "Nastala chyba při vypínání rušivého elementu, kontaktuj nás na Discordu.",
                                                NamedTextColor.RED
                                            )
                                        )
                                )

                            it.printStackTrace()
                        }

                        null
                    }
            }
        }
    }

}
