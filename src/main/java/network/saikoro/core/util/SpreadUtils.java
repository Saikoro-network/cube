/**
 * Copyright (C) 2022 TTtie
 * <p>
 * This file is part of Cube.
 * <p>
 * Cube is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Cube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with Cube.  If not, see <http://www.gnu.org/licenses/>.
 */

package network.saikoro.core.util;

/**
 * A helper Java class used to avoid using Kotlin's spread operator,
 * which is known to copy the whole array before passing it through,
 * resulting in a potential loss of performance.
 */
public class SpreadUtils {
    public static void setCancelReason(net.md_5.bungee.api.event.PreLoginEvent ev,
                                       net.md_5.bungee.api.chat.BaseComponent[] components) {
        ev.setCancelReason(components);
    }
}
