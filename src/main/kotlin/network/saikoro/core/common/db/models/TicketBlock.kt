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


package network.saikoro.core.common.db.models

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int

interface TicketBlock : Entity<TicketBlock> {
    companion object : Entity.Factory<TicketBlock>()

    val id: Int
    var user: UserAccount
}

@Suppress("UNUSED")
object TicketBlocks : Table<TicketBlock>("adminka_tickets`.`tickets_banned_users", "tickets_banned_users") {
    val id = int("id").primaryKey().bindTo { it.id }
    val user = int("user_id").references(UserAccounts) { it.user }
    // Rest of database storage not shown

    val Database.ticketBlocks get() = this.sequenceOf(TicketBlocks)
}