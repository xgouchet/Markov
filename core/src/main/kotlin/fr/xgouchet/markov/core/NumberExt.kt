package fr.xgouchet.markov.core


infix fun Long.`^`(power: Int): Long {
    return when {
        power == 0 -> 1L
        power == 1 -> this
        power > 1 -> this * (this `^` (power - 1))
        else -> throw UnsupportedOperationException("Can't compute power of negative integer")
    }
}