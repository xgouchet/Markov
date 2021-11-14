package fr.xgouchet.markov.core

interface MarkovIndexer<T : Any> {
    fun getIndex(data: T): Int
}