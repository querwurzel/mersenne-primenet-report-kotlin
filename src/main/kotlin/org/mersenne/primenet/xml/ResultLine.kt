package org.mersenne.primenet.xml

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.xml.stream.events.Attribute

class ResultLine {

    companion object {
        @JvmStatic
        private val knownFormats = listOf(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        )
    }

    var exponent: String? = null
    var userName: String? = null
    var computerName: String? = null
    var resultType: String? = null
    var ghzDays: String? = null
    var dateReceived: String? = null
    var assignmentAge: String? = null
    var message: String? = null

    fun exponent(exponent: Attribute) {
        this.exponent = exponent.value
    }

    fun parseDate(): LocalDate {
        for (format in knownFormats) {
            try {
                return LocalDate.parse(this.dateReceived, format)
            } catch (e: DateTimeParseException) {
                continue
            }
        }

        throw DateTimeParseException("Unknown format", this.dateReceived, 0)
    }

    fun parseTime(): LocalTime {
        for (format in knownFormats) {
            try {
                return LocalTime.parse(this.dateReceived, format)
            } catch (e: DateTimeParseException) {
                continue
            }
        }

        throw DateTimeParseException("Unknown format", this.dateReceived, 0)
    }

    override fun toString(): String {
        return "ResultLine(exponent=$exponent, userName=$userName, computerName=$computerName, resultType=$resultType, ghzDays=$ghzDays, dateReceived=$dateReceived, assignmentAge=$assignmentAge, message=$message)"
    }
}
