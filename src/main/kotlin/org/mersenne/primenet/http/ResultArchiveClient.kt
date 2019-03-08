package org.mersenne.primenet.http

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class ResultArchiveClient @Autowired constructor(
        restTemplateBuilder: RestTemplateBuilder
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    private val restTemplate = restTemplateBuilder.build()

    companion object {
        private const val annualUrl = "https://www.mersenne.org/result_archive/%d.7z"
        private const val dailyUrl = "https://www.mersenne.org/result_archive/%d/%s.xml.bz2"
    }

    fun fetchDailyReport(date: LocalDate): ByteArray {
        // https://www.mersenne.org/result_archive/2019/2019-01-29.xml.bz2
        val url = String.format(dailyUrl, date.year, date.format(DateTimeFormatter.ISO_LOCAL_DATE))
        return this.fetchDailyReport(url)
    }

    fun fetchDailyReport(url: String): ByteArray {
        log.debug("Fetching daily report using url {}", url)
        return restTemplate.getForObject(url, ByteArray::class.java)!!
    }

    fun fetchAnnualReport(date: LocalDate): ByteArray {
        // https://www.mersenne.org/result_archive/2018.7z
        val url = String.format(annualUrl, date.year)
        return this.fetchAnnualReport(url)
    }

    fun fetchAnnualReport(url: String): ByteArray {
        log.debug("Fetching annual report using url {}", url)
        return restTemplate.getForObject(url, ByteArray::class.java)!!
    }
}
