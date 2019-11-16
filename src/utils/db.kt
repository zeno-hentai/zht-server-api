package utils

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll

fun <T> Table.maxValue(col: Column<T>): T? =
    slice(col).selectAll().orderBy(col, SortOrder.DESC).firstOrNull()?.get(col)