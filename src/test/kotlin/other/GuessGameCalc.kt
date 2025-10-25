package other

import kotlin.math.abs

data class Topic(
    val title: String,
    val correctAnswer: Int,
)
val topics = listOf(
    Topic("test1", 10),
    Topic("test2", 50),
    Topic("test3", 100),
)
val topicCount = topics.size

data class Handin(
    val colleagueName: String,
    val guessedAnswers: List<Int>,
) {
    init {
        require(colleagueName.isNotEmpty())
        require(guessedAnswers.size == topicCount) { "Answer size must be [$topicCount] but was [${guessedAnswers.size}] for [$colleagueName]" }
    }
}


data class Result(
    val handin: Handin,
    val answerResults: List<AnswerResult>,
)

data class AnswerResult(
    val topic: Topic,
    val guessedAnswer: Int,
    val percentage: Double,
)

object GuessGameCalc {
    @JvmStatic
    fun main(args: Array<String>) {

        val handins = listOf(
            Handin("Max", listOf(9, 49, 99)),
            Handin("Anna", listOf(3, 50, 60)),
            Handin("Otto", listOf(32, 98, 5, 3)),
        )

        val winner = calcResults(handins).maxByOrNull { result ->
            result.answerResults.sumOf { answer ->
                answer.percentage
            }
        }!!
        println("winner: ${winner.handin.colleagueName}")
    }

    /*
    correct: 10
    A: 5 ... diff: |10-5|: 5
    B: 12 ... diff: |10-12|: 2
    C: 6 ... diff: 10-6: 4
    diff range: 2,5 (min/max from diffs)
     */
    private fun calcResults(handins: List<Handin>): List<Result> {
        val guessesDiffs = handins.map { handin ->
            List(topics.size) { topicIndex ->
                val guessedTopicAnswer = handin.guessedAnswers[topicIndex] // A: 5
                val correctAnswer = topics[topicIndex].correctAnswer // 10
                val answerDiff = abs(correctAnswer - guessedTopicAnswer) // 5
                answerDiff
            }
        }
//            .also { println(it) } // [[5], [2], [4]]
        val guessesDiffsRange = List(topics.size) { topicIndex ->
            guessesDiffs.map { it[topicIndex] }.let { it.min() to it.max() }
        }
//            .also { println(it) } // [(2, 5)]

        /*
        ' ... diff - min diff range (nullify-normalize)
        A': 5 - 2 = 3
        B': 2 - 2 = 0
        C': 4 - 2 = 2
        range' = 5 - 2 = 3
         */
        val guessesDiffsNorm = List(topics.size) { topicIndex ->
            val lowerRange = guessesDiffsRange[topicIndex].first
            guessesDiffs.map { guessDiff ->
                guessDiff[topicIndex] - lowerRange
            }
        }
//            .also { println(it) } // [[3, 0, 2]]
        val globalDiffsNorm = List(topics.size) { topicIndex ->
            val minRange = guessesDiffsRange[topicIndex].first
            val maxRange = guessesDiffsRange[topicIndex].second
            maxRange - minRange
        }
//            .also { println(it) } // range' = [3]

        /*
        compare ' with each other:
        A*: 3 - 3 = 0
        B*: 3 - 0 = 3
        C*: 3 - 2 = 1
        range it: 0,3
        */
        val guessesAbsDiff = List(topics.size) { topicIndex ->
            val globalDiffNorm = globalDiffsNorm[topicIndex] // 3
            guessesDiffsNorm[topicIndex].map { guessesDiffNorm ->
                globalDiffNorm - guessesDiffNorm
            }
        }
//            .also { println(it) } // [[0, 3, 1]]
        val guessesAbsMax = guessesAbsDiff.map {
            it.max()
        }
//            .also { println(it) } // [3]

        /*
        3=100%, 1?
        A_: 0 / 3 = 0
        B_: 3 / 3 = 1.0
        C_: 1 / 3 = 0.3
        */
        return handins.mapIndexed { handinIndex, handin ->
            Result(
                handin = handin,
                answerResults = List(topics.size) { topicIndex ->
                    AnswerResult(
                        topic = topics[topicIndex],
                        guessedAnswer = handin.guessedAnswers[topicIndex],
                        percentage = guessesAbsDiff[topicIndex][handinIndex].toDouble() / guessesAbsMax[topicIndex].toDouble(),
                    )
                }
            )
        }
    }
}
