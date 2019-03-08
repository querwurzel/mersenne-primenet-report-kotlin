package org.mersenne.primenet.xml

import org.mersenne.primenet.xml.ResultParser.ResultSchema.ASSIGNMENTAGE
import org.mersenne.primenet.xml.ResultParser.ResultSchema.COMPUTERNAME
import org.mersenne.primenet.xml.ResultParser.ResultSchema.DATERECEIVED
import org.mersenne.primenet.xml.ResultParser.ResultSchema.DTSTART
import org.mersenne.primenet.xml.ResultParser.ResultSchema.EXPONENT
import org.mersenne.primenet.xml.ResultParser.ResultSchema.GHZDAYS
import org.mersenne.primenet.xml.ResultParser.ResultSchema.MESSAGE
import org.mersenne.primenet.xml.ResultParser.ResultSchema.RESULT
import org.mersenne.primenet.xml.ResultParser.ResultSchema.RESULTS
import org.mersenne.primenet.xml.ResultParser.ResultSchema.RESULTTYPE
import org.mersenne.primenet.xml.ResultParser.ResultSchema.USERNAME
import org.springframework.stereotype.Component
import java.io.InputStream
import java.util.ArrayList
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory

@Component
class ResultParser {

    companion object {
        private val factory = XMLInputFactory.newFactory()

        init {
            factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false)
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false)
            factory.setProperty(XMLInputFactory.IS_COALESCING, false)
        }
    }

    fun parseResults(stream: InputStream): Results {
        val reader = factory.createXMLEventReader(stream)
        val lines = ArrayList<ResultLine>()
        val results = Results(lines)

        try {
            var result = ResultLine()

            while (reader.hasNext()) {
                val event = reader.nextEvent()

                if (event.isStartElement) {
                    val element = event.asStartElement()
                    val tag = element.name

                    when(tag) {
                        RESULT -> result.exponent(element.getAttributeByName(EXPONENT))
                        USERNAME -> result.userName = reader.elementText
                        COMPUTERNAME -> result.computerName = reader.elementText
                        RESULTTYPE -> result.resultType = reader.elementText
                        DATERECEIVED -> result.dateReceived = reader.elementText
                        ASSIGNMENTAGE -> result.assignmentAge = reader.elementText
                        GHZDAYS -> result.ghzDays = reader.elementText
                        MESSAGE -> result.message = reader.elementText
                        RESULTS -> results.date(element.getAttributeByName(DTSTART))
                    }
                }

                if (event.isEndElement) {
                    val element = event.asEndElement()
                    val tag = element.name

                    if (RESULT == tag) {
                        lines.add(result)
                        result = ResultLine()
                    }
                }
            }
        } finally {
            reader.close()
        }

        return results
    }

    protected object ResultSchema {
        val RESULTS = QName.valueOf("results")
        val DTSTART = QName.valueOf("dtStart")
        val RESULT = QName.valueOf("result")
        val EXPONENT = QName.valueOf("exponent")
        val USERNAME = QName.valueOf("UserName")
        val COMPUTERNAME = QName.valueOf("ComputerName")
        val RESULTTYPE = QName.valueOf("ResultType")
        val DATERECEIVED = QName.valueOf("DateReceived")
        val ASSIGNMENTAGE = QName.valueOf("AssignmentAge")
        val MESSAGE = QName.valueOf("Message")
        val GHZDAYS = QName.valueOf("GHzDays")
    }
}