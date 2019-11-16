package utils

import java.lang.IllegalStateException

class ZhtApiException(message: String): IllegalStateException(message)

fun zError(message: String): Nothing {
    throw ZhtApiException(message)
}