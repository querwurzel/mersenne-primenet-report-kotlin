package org.mersenne.primenet.xml

import java.time.LocalDate
import javax.xml.stream.events.Attribute

class Results (
        var results: List<ResultLine> = listOf()
) {

    lateinit var date: String

    fun parseDate() = LocalDate.parse(date)

    fun date(date: Attribute): Results {
        this.date = date.value
        return this
    }

    fun notEmpty() = this.results.isNotEmpty()

    override fun toString() = "Results(date=$date, results=$results)"
}
