package org.mersenne.primenet.xml

import java.time.LocalDate
import java.util.*
import javax.xml.stream.events.Attribute

class Results (
        val lines: Queue<ResultLine> = ArrayDeque()
) {

    lateinit var date: String

    fun date(date: Attribute): Results {
        this.date = date.value
        return this
    }

    fun parseDate() = LocalDate.parse(date)

    val notEmpty: Boolean
        get() = this.lines.isNotEmpty()

    val size: Int
        get() = this.lines.size

    override fun toString() = "Results(date=$date, results=$lines)"
}
