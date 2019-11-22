package utils

import org.jetbrains.exposed.sql.*

fun <T> Table.maxValue(col: Column<T>): T? =
    slice(col).selectAll().orderBy(col, SortOrder.DESC).firstOrNull()?.get(col)

fun Query.isEmpty(): Boolean = count() == 0
fun Query.isNotEmpty() = !isEmpty()
fun Query.assertAuthorized() {
    if(isEmpty()){
        zError("unauthorized")
    }
}