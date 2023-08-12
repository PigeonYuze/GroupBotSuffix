package com.pigeonyuze.execute

object RandomText {
    private val gotIndex = arrayListOf<Int>()

    fun randomTextImpl(arg: String): String {
        val args = arg.split("|")
        return random(args)
    }

    private fun random(args: List<String>): String{
        var index: Int

        if (gotIndex.size == args.size) {
            gotIndex.clear()
        }

        do {
            index = args.indices.random()
        }while (index !in gotIndex)

        gotIndex.add(index)
        return args[index]
    }
}