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
import org.ktorm.schema.text

@Suppress("UNUSED")
enum class TicketStatus {
    WAITING_FOR_USER_RESPONSE,
    WAITING_FOR_SUPPORT_RESPONSE,
    CLOSED;
}

interface Ticket : Entity<Ticket> {
    companion object : Entity.Factory<Ticket>()

    val id: Int
    var author: UserAccount
    var title: String
    var assignedGroup: String
    var type: String
    var status: Int
    var createdAt: String
}

@Suppress("UNUSED")
object Tickets : Table<Ticket>("adminka_tickets`.`tickets_list", "tickets_list") {
    val id = int("id").primaryKey().bindTo { it.id }
    val author = int("author").references(UserAccounts) { it.author }
    val title = text("title").bindTo { it.title }
    val assignedGroup = text("for").bindTo { it.assignedGroup }
    val reason = text("reason").bindTo { it.type }
    val status = int("waiting_for").bindTo { it.status }
    val createdAt = text("create_timestamp").bindTo { it.createdAt }

    val Database.tickets get() = this.sequenceOf(Tickets)
}