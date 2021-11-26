package my.experiments

import jetbrains.datalore.base.observable.collections.Collections
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.*
import jetbrains.letsPlot.label.labs
import jetbrains.letsPlot.letsPlot
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class Implementation {
    private val sampleSize = 10_000_000

    data class Number(val inWork: AtomicBoolean = AtomicBoolean(false), var value: Int)

    private fun generateSample(): Array<Number> = Array(sampleSize) { Number(value = Random.nextInt()) }

    private fun doTimeTest(threadsLimit: Int): Pair<List<Long>, List<Long>> {
        val oneThreadResults = mutableListOf<Long>()
        val multiThreadResults = mutableListOf<Long>()
        for (threadsAmount in 1..threadsLimit) {
            val sample = generateSample()
            val sampleCopy = Collections.arrayCopy(sample)
            oneThreadResults.add(oneThread(sample))
            multiThreadResults.add(multiThread(sampleCopy, threadsAmount))
        }
        return oneThreadResults to multiThreadResults
    }

    private fun processNumber(number: Number) {
        if (number.inWork.get()) {
            return
        }
        number.inWork.set(true)
        number.value = number.value * number.value
    }

    private fun oneThread(sample: Array<Number>): Long {
        val time = measureTimeMillis {
            sample.forEach {
                processNumber(it)
            }
        }
        return time
    }

    private fun multiThread(sample: Array<Number>, threadsNumber: Int): Long {
        val executorService = Executors.newFixedThreadPool(threadsNumber)
        val executors = mutableListOf<Future<*>>()
        for (i in 1..threadsNumber) {
            executors.add(
                executorService.submit {
                    sample.forEach {
                        processNumber(it)
                    }
                }
            )
        }
        val time = measureTimeMillis {
            executors.forEach {
                it.get()
            }
        }
        return time
    }

    fun plotFunction() {
        val threadsLimit = 50
        val result = doTimeTest(threadsLimit)
        val xs = List(threadsLimit) { it + 1 }
        val data = mapOf<String, Any>(
            "x" to xs + xs,
            "y" to result.first + result.second,
            "Thread" to List(threadsLimit) {"Single"} + List(threadsLimit) {"Multi"},
        )
        var fig = letsPlot(data)
        fig += labs(x = "Threads", y = "Time", title = "Experiments")
        fig += geomLine(
            size = 1.0
        ) {
            x = "x"
            y = "y"
            color = "Thread"
        }
        fig.show()
        ggsave(fig, "fig.svg")
    }
}