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

interface TicketMessage : Entity<TicketMessage> {
    companion object : Entity.Factory<TicketMessage>()

    val id: Int
    var ticket: Ticket
    var author: UserAccount
    var params: String
    var message: String
    var timestamp: String
}

@Suppress("UNUSED")
object TicketMessages : Table<TicketMessage>("adminka_tickets`.`tickets_messages", "tickets_messages") {
    val id = int("id").primaryKey().bindTo { it.id }
    val ticketId = int("ticket_id").references(Tickets) { it.ticket }
    val author = int("author").references(UserAccounts) { it.author }
    val params = text("params").bindTo { it.params }
    val message = text("message").bindTo { it.message }
    val timestamp = text("timestamp").bindTo { it.timestamp }

    val Database.ticketMessages get() = this.sequenceOf(TicketMessages)
}