import engine.RiskEngine
import engine.Action

suspend fun main(args: Array<String>) {
    println(listOf<String>("a", "b").javaClass)
    println(mutableListOf<String>().javaClass)
    val engine = RiskEngine()
    engine.start()
    engine.process(Action())
    Thread.sleep(3000)
}
