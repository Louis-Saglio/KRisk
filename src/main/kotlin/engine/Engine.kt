package engine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.random.Random
import kotlin.random.nextInt

abstract class Engine<InputType, OutputType> {
    private val inputQueue = Channel<InputType>(Channel.UNLIMITED)
    private val outputQueue = Channel<OutputType>(Channel.UNLIMITED)

    protected open suspend fun main() {
        while (mustRun()) {
            val input = inputQueue.receive()
            outputQueue.send(handle(input))
            println("$input handled")
        }
    }

    protected open fun mustRun(): Boolean {
        return true
    }

    protected abstract suspend fun handle(input: InputType): OutputType

    fun start() {
        GlobalScope.launch { main() }
    }

    suspend fun process(input: InputType) {
        inputQueue.send(input)
    }

    suspend fun processAndReturn(input: InputType): OutputType {
        inputQueue.send(input)
        return outputQueue.receive()
    }

    suspend fun getResult(): OutputType {
        return outputQueue.receive()
    }
}

//suspend fun main(args: Array<String>) {
//
//    class Haha : Engine<String, String>() {
//        override suspend fun handle(input: String): String {
//            delay(Random.nextInt(100..1000).toLong())
//            return "nbr : $input"
//        }
//    }
//
//    val s = Haha()
//    s.start()
//    for (i in 1..10) {
//        s.process(i.toString())
//        delay(Random.nextInt(100..300).toLong())
//    }
//    for (i in 1..10) {
//        println(s.getResult())
//    }
//    runBlocking { delay(2000) }
//}
