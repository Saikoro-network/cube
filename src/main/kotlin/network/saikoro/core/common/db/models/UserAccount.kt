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
import org.ktorm.schema.varchar

interface UserAccount : Entity<UserAccount> {
    companion object : Entity.Factory<UserAccount>()

    val id: Int
    var uniqueId: String
    var email: String
    val lastIp: String
    val ipList: String
}

@Suppress("UNUSED")
object UserAccounts : Table<UserAccount>("adminka`.`accounts", "accounts") {
    val id = int("id").primaryKey().bindTo { it.id }
    val uniqueId = varchar("authme_id").bindTo { it.uniqueId }
    val email = text("e-mail").bindTo { it.email }
    val lastIp = text("last-ip").bindTo { it.lastIp }
    val ipList = text("ip-list").bindTo { it.ipList }

    val Database.users get() = this.sequenceOf(UserAccounts)
}