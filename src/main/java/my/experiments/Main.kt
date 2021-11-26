package my.experiments

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Started...")
            val implementation = Implementation()
            implementation.plotFunction()
        }
    }
}