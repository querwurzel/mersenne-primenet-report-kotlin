package org.mersenne.primenet.imports

import org.mersenne.primenet.compress.Bzip2
import org.mersenne.primenet.compress.SevenZip
import org.mersenne.primenet.api.ResultArchiveClient
import org.mersenne.primenet.results.Result
import org.mersenne.primenet.results.ResultRepository
import org.mersenne.primenet.xml.ResultLine
import org.mersenne.primenet.xml.ResultParser
import org.mersenne.primenet.xml.Results
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import javax.xml.stream.XMLStreamException

@Service
class ImportService @Autowired constructor(
        private val importRepository: ImportRepository,
        private val resultRepository: ResultRepository,
        private val resultClient: ResultArchiveClient,
        private val resultParser: ResultParser
) {

    private val log = LoggerFactory.getLogger(ImportService::class.java)

    @Scheduled(cron = "33 33 3 * * *")
    protected fun processYesterdayImport() {
        val yesterday = LocalDate.now().minusDays(1)
        log.info("Importing results from yesterday [{}]", yesterday)
        this.importDailyResults(yesterday)
        System.gc()
    }

    @Scheduled(initialDelay = 2 * 60 * 1000, fixedDelay = 60 * 60 * 1000)
    protected fun processPendingImports() {
        val imports = importRepository.findTop180ByState(Import.State.PENDING)

        if (imports.isNotEmpty()) {
            log.info("Scheduling pending {} imports", imports.size)
            imports.forEach { this.importDailyResults(it) }
            log.info("Processed {} imports", imports.size)
            System.gc()
        }
    }

    @Scheduled(initialDelay = 60 * 1000, fixedDelay = 12 * 60 * 60 * 1000)
    protected fun processStaleImports() {
        val threshold = LocalDateTime.now().minusHours(12)
        val imports = importRepository.findAllByStateAndLastAttemptBefore(Import.State.ACTIVE, threshold)

        if (imports.isNotEmpty()) {
            log.warn("Resetting {} stale imports!", imports.size)
            imports.forEach { theImport ->
                theImport.reset()
                resultRepository.deleteAllByDate(theImport.date)
            }
            importRepository.saveAll(imports)
            System.gc()
        }
    }

    fun importAnnualResults(year: LocalDate) {
        try {
            val archives = this.parseArchives(resultClient.fetchAnnualReport(year))
            archives.forEach { this.importDailyResults(it) }
        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.NOT_FOUND || e.statusCode == HttpStatus.FORBIDDEN) {
                log.error("Failed to fetch result archive of {}, HTTP {}", year.year, e.statusCode)
            } else {
                log.error("Failed to fetch annual archive of {}", year.year, e)
            }
        } catch (e: IOException) {
            log.error("Failed to extract annual archive for {}", year.year, e)
        } catch (e: NoSuchElementException) {
            log.error("Failed to extract annual archive for {}", year.year, e)
        }
    }

    internal fun importDailyResults(date: LocalDate) {
        this.importDailyResults(Import(date))
    }

    private fun importDailyResults(archive: ByteArray) {
        try {
            val results = this.parseResults(archive)
            val date = results.parseDate()
            this.importDailyResults(date, results)
        } catch (e: IOException) {
            log.error("Failed to parse some results of annual archive", e)
        } catch (e: XMLStreamException) {
            log.error("Failed to parse some results of annual archive", e)
        }
    }

    private fun importDailyResults(date: LocalDate, results: Results) {
        try {
            val theImport = importRepository.save(Import(date).nextAttempt())
            this.persistImportAndResults(theImport, results)
        } catch (e: DataIntegrityViolationException) {
            log.info("Import of {} already exists", date)
        }
    }

    private fun importDailyResults(anImport: Import) {
        try {
            val theImport = importRepository.save(anImport.nextAttempt())
            val results = this.parseResults(resultClient.fetchDailyReport(theImport.date))
            this.persistImportAndResults(theImport, results)
        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.NOT_FOUND || e.statusCode == HttpStatus.FORBIDDEN) {
                log.error("Failed to fetch result archive of {}, HTTP {}", anImport.date, e.statusCode)
            } else {
                log.error("Failed to fetch result archive of {}", anImport.date, e)
            }
            importRepository.save(anImport.failed(e.message))
        } catch (e: IOException) {
            log.error("Failed to parse results of {}", anImport.date, e)
            importRepository.save(anImport.failed(e.message))
        } catch (e: XMLStreamException) {
            log.error("Failed to parse results of {}", anImport.date, e)
            importRepository.save(anImport.failed(e.message))
        } catch (e: DataIntegrityViolationException) {
            log.info("Import of {} already exists", anImport.date)
        } catch (e: IllegalStateException) {
            log.warn("Import of {} has inconsistencies between state and attempts; resetting!", anImport.date)
            importRepository.save(anImport.reset())
        }
    }

    @Throws(IOException::class, XMLStreamException::class)
    private fun parseResults(archive: ByteArray): Results {
        Bzip2.stream(archive).use {
            return resultParser.parseResults(it)
        }
    }

    @Throws(IOException::class, NoSuchElementException::class)
    private fun parseArchives(annualArchive: ByteArray): List<ByteArray> {
        val archives = ArrayList<ByteArray>(365)
        SevenZip.extract(annualArchive).forEach { archives.add(it) }
        return archives
    }

    private fun persistImportAndResults(theImport: Import, result: Results) {
        val count = result.size

        result.lines
                .map {
                    result.lines.remove(it)
                    resultMapper(theImport, it)
                }
                .chunked(10_000)
                .forEach { resultRepository.saveAll(it) }

        importRepository.save(theImport.succeeded())
        log.info("Imported {} results of {}", String.format("%1$6s", count), theImport.date)
    }

    companion object {
        private val resultMapper = { theImport: Import, line: ResultLine -> Result()
                    .setImport(theImport)
                    .setDate(line.parseDate())
                    .setTime(line.parseTime())
                    .setExponent(line.exponent!!)
                    .setUserName(line.userName!!)
                    .setComputerName(line.computerName!!)
                    .setResultType(line.resultType!!)
                    .setGhzDays(line.ghzDays!!)
                    .setAssignmentAge(line.assignmentAge)
                    .setMessage(line.message)
        }
    }
}
