package fr.xgouchet.markov

import fr.xgouchet.markov.core.MarkovTable
import java.io.Console
import java.io.File
import java.io.FileFilter
import java.util.Scanner


class Main {

    companion object {

        //val tokens = "abcdefghijklmnopqrstuvwxyzäàâåéèêëïîöôüùûæœçñ'-".toCharArray().toTypedArray()
        val tokens = "abcdefghijklmnopqrstuvwxyz".toCharArray().toTypedArray()

        @JvmStatic
        fun main(args: Array<String>) {

            val chainLength = 4

            val sourceFiles = File(".")
                .listFiles { file -> file != null && file.extension == "txt" && file.nameWithoutExtension == "last_names" }
                .orEmpty()
                .sorted()

            for (sourceFile in sourceFiles) {
                if (sourceFile.exists() && sourceFile.canRead()) {
                    generateWords(sourceFile, chainLength)
                }
            }
        }


        private fun generateWords(
            dictionary: List<String>,
            chainLength: Int = 3,
            generatedCount: Int = 32
        ): List<String> {
            val markovTable = MarkovTable(chainLength, tokens)
            dictionary.forEach { markovTable.analyzeWord(it) }
            markovTable.printCsv()

            return (0 until generatedCount).map { markovTable.generateWord() }.sorted()
        }

        private fun generateWords(sourceFile: File, chainLength: Int) {
            println("----")
            val dictionary = sourceFile.readLines().map { it.toLowerCase() }.sorted()
            println("Generating words from ${sourceFile.nameWithoutExtension}, using a dict of size ${dictionary.size}")
            val generated = generateWords(dictionary, chainLength)
            val filtered = filterRealWords(generated, dictionary)
            // println(filtered.joinToString("\n"))
        }

        private fun filterRealWords(
            generated: List<String>,
            dictionary: List<String>
        ): List<String> {
            val result = mutableListOf<String>()

            var idxGen = 0
            var idxDic = 0

            while (idxGen < generated.size && idxDic < dictionary.size) {
                val wGen = generated[idxGen]
                val wDic = dictionary[idxDic]

                val comp = wGen.compareTo(wDic, ignoreCase = true)
                when {
                    comp < 0 -> {
                        idxGen++
                        result.add(wGen)
                    }
                    comp == 0 -> {
                        idxGen++
                        idxDic++
                    }
                    comp > 0 -> idxDic++
                }
            }

            return result
        }

        private fun MarkovTable<Char>.analyzeWord(word: String) {
            analyzeSequence(word.toCharArray().toTypedArray())
        }

        private fun MarkovTable<Char>.rejectWord(word: String) {
            analyzeSequence(word.toCharArray().toTypedArray(), reject = true)
        }

        private fun MarkovTable<Char>.generateWord(): String {
            return generate<Char>().toList().toTypedArray().joinToString("")
        }


        private fun getChoice(msg: String): String {
            val console: Console? = System.console()

            return if (console != null) {
                console.readLine(msg)
            } else {
                print(msg)
                val inputReader = Scanner(System.`in`)
                inputReader.nextLine()
            }
        }

    }
}