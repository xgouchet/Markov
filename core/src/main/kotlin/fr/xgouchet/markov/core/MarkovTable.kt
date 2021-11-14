package fr.xgouchet.markov.core

import kotlin.math.max
import kotlin.random.Random

class MarkovTable<T : Any>(
    val chainLength: Int,
    val tokens: Array<T>,
    val verbose: Boolean = false
) {
    val rng = Random(System.nanoTime() * System.currentTimeMillis())

    private val table: Array<Int>
    private var sampleCount = 0

    init {
        require(chainLength > 0) { "Markov chain analysis needs a dimension greater than 0" }

        val tableSize = (tokens.size.toLong() + 1) `^` chainLength
        check(tableSize < MAX_TABLE_SIZE) {
            "Markov table dimension or token count is too large: $tableSize"
        }

        table = Array(tableSize.toInt()) { 0 }
    }

    // region Analysis

    inline fun <reified D : T> analyzeSequence(sequence: Array<D>, reject: Boolean = false) {
        val dataWindow = Array<D?>(chainLength) { null }
        val weight = if (reject) -1 else 1

        sequence.forEach {
            dataWindow.shiftLeftAndInsert(it)
            sample(weight, *dataWindow)
        }

        for (i in 1 until chainLength) {
            dataWindow.shiftLeftAndInsert(null)
            sample(weight, *dataWindow)
        }
    }

    fun sample(weight: Int, vararg data: T?) {
        require(data.size == chainLength) { "Sample has invalid dimension" }
        val index = index(*data)
        val oldValue = table[index]
        table[index] = max(oldValue + weight, 0)
        sampleCount++
    }

    // endregion

    // region Generation

    inline fun <reified D : T> generate(): Sequence<T?> {
        val dataWindow = Array<D?>(chainLength) { null }

        return generateSequence {
            dataWindow.shiftLeftAndInsert(null)
            val baseWindowStr = dataWindow.windowStr()
            val total = getTotalOptions(dataWindow)
            if (total == 0) {
                log("No possible options with base window [$baseWindowStr]")
                return@generateSequence null
            }

            val x = if (total == 1) 1 else (rng.nextInt(total - 1) + 1)
            dataWindow[chainLength - 1] = null
            var sum = getOptions(dataWindow)
            if (sum >= x) {
                log(
                    "Selected EoW for window [${dataWindow.windowStr()}] " +
                            "with odds $sum ≥ $x ($sum/$total)"
                )
                return@generateSequence null
            }
            tokens.forEach { token ->
                dataWindow[chainLength - 1] = token as? D
                val options = getOptions(dataWindow)
                sum += options
                if (sum >= x) {
                    log(
                        "Selected window [${dataWindow.windowStr()}] " +
                                "with odds $sum ≥ $x ($options/$total)"
                    )
                    return@generateSequence token
                }
            }

            log(
                "Found no option for base window [$baseWindowStr] " +
                        "with odds $sum ≥ $x (/$total)"
            )
            null
        }
    }

    fun <K : Any> Array<K?>.windowStr() = joinToString("") { it?.toString() ?: "·" }

    // endregion

    // region Debug printCsv

    fun printCsv(ignoreEmpty: Boolean = true) {


        val inTokens = Array<Any?>(tokens.size + 1) { null }
        val outTokens = Array<Any?>(tokens.size + 1) { null }
        tokens.forEachIndexed { index, t ->
            inTokens[index + 1] = t
            outTokens[index] = t
        }
        println(outTokens.joinToString(",", prefix = "#,") { it?.toString() ?: "∅" })

        when (chainLength) {
            1 -> printCsv1(outTokens)
            2 -> printCsv2(inTokens, outTokens, ignoreEmpty)
            3 -> printCsv3(inTokens, outTokens, ignoreEmpty)
            4 -> printCsv4(inTokens, outTokens, ignoreEmpty)
            else -> TODO()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun printCsv1(outTokens: Array<Any?>) {
        check(chainLength == 1)
        println(outTokens.map { table[index(it as? T)] }.joinToString(","))
    }


    @Suppress("UNCHECKED_CAST")
    private fun printCsv2(inTokens: Array<Any?>, outTokens: Array<Any?>, ignoreEmpty: Boolean) {
        check(chainLength == 2)

        inTokens.forEach { from ->
            var line = "${from ?: "∅"}"
            var total = 0
            outTokens.forEach { to ->
                val value = table[index(from as? T, to as? T)]
                total += value
                line += ",$value"
            }
            if (total > 0 || !ignoreEmpty) println(line)
        }

    }

    fun printCsv3(inTokens: Array<Any?>, outTokens: Array<Any?>, ignoreEmpty: Boolean) {
        check(chainLength == 3)

        inTokens.forEach { from1 ->
            inTokens.forEach { from2 ->
                var total = 0
                var line = "${from1 ?: "∅"}${from2 ?: "∅"}"
                outTokens.forEach { to ->
                    val value = table[index(from1 as? T, from2 as? T, to as? T)]
                    total += value
                    line += ",$value"
                }
                if (total > 0 || !ignoreEmpty) println(line)
            }
        }
    }

    fun printCsv4(inTokens: Array<Any?>, outTokens: Array<Any?>, ignoreEmpty: Boolean) {
        check(chainLength == 4)

        inTokens.forEach { from1 ->
            inTokens.forEach { from2 ->
                inTokens.forEach { from3 ->
                    var total = 0
                    var line = "${from1 ?: "∅"}${from2 ?: "∅"}${from3 ?: "∅"}"
                    outTokens.forEach { to ->
                        val value = table[index(from1 as? T, from2 as? T, from3 as? T, to as? T)]
                        total += value
                        line += ",$value"
                    }
                    if (total > 0 || !ignoreEmpty) println(line)
                }
            }
        }
    }

    fun log(message: String) {
        if (verbose) println(message)
    }

    // endregion

    // region Utils

    private fun index(vararg data: T?): Int {
        var index = 0
        data.forEach { d ->
            val i = if (d == null) tokens.size else tokens.indexOf(d)
            check(i >= 0 && i <= tokens.size) {
                "Index for token '$d' is out of bounds"
            }
            index = (index * (tokens.size + 1)) + i
        }
        return index
    }

    fun <D : T> getOptions(dataWindow: Array<D?>): Int {
        return table[index(*dataWindow)]
    }

    fun <D : T> getTotalOptions(dataWindow: Array<D?>): Int {
        val index = index(*dataWindow)
        var total = table[index]
        tokens.forEach {
            dataWindow[chainLength - 1] = it as? D
            total += table[index]
        }
        return total
    }

    companion object {
        private const val MAX_TABLE_SIZE = 0xFFFFFFF
    }

}

fun <T> Array<T>.shiftLeftAndInsert(newData: T) {
    for (i in 0 until size - 1) {
        this[i] = this[i + 1]
    }
    this[size - 1] = newData
}
