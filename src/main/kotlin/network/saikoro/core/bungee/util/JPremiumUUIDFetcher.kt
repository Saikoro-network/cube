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
 *
 * Additional permission under GNU AGPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with JPremium (or a modified version of that library), containing parts covered
 * by the terms of JPremium, the licensors of this Program grant you additional
 * permission to convey the resulting work.
 */

package network.saikoro.core.bungee.util

import com.jakub.premium.JPremium
import com.jakub.premium.api.App
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

class JPremiumUUIDFetcher : UUIDFetcher() {
    private val jPremium: App = JPremium.getApplication()

    override fun getUniqueId(player: ProxiedPlayer): UUID {
        val profile = jPremium.getUserProfileByUniqueId(player.uniqueId)

        return if (profile.isEmpty) player.uniqueId
        else {
            val user = profile.get()

            user.premiumId ?: user.uniqueId
        }
    }
}