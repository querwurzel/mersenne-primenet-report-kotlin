package org.mersenne.primenet.imports

import org.mersenne.primenet.PrimeNetProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.*

@Service
class BootstrapService @Autowired constructor(
        primeNetProperties: PrimeNetProperties,
        private val importRepository: ImportRepository,
        private val importService: ImportService
) {

    private val log = LoggerFactory.getLogger(BootstrapService::class.java)

    private val importStart = primeNetProperties.start

    @Bean
    @Lazy
    protected fun importBootstrapper(): ApplicationRunner {
        return object : ApplicationRunner {
            @Async
            override fun run(args: ApplicationArguments) {
                if (importRepository.hasImports()) {
                    if (importRepository.hasImportGapsSince(importStart)) {
                        this@BootstrapService.bootstrapDailyImports()
                    }
                } else {
                    this@BootstrapService.bootstrapAnnualImports()
                    this@BootstrapService.bootstrapDailyImports()
                }
                log.info("Bootstrapping complete")
                System.gc()
            }
        }
    }

    private fun bootstrapAnnualImports() {
        val years = this.selectAnnualImports(importStart)
        years.forEach { year ->
            log.info("Importing annual results for year {}", year.year)
            importService.importAnnualResults(year)
            log.info("Imported annual results for year {}", year.year)
        }
    }

    private fun bootstrapDailyImports() {
        val days = this.selectDailyImports(importStart)
        if (days.isNotEmpty()) {
            log.info("Importing {} daily results as of {}", days.size, importStart)
            days.forEach { importService.importDailyResults(it) }
            log.info("Imported daily results")
        }
    }

    private fun selectAnnualImports(inclusiveStart: LocalDate): Set<LocalDate> {
        val missing = TreeSet<LocalDate>()

        var year = LocalDate.now().minusYears(1).with(TemporalAdjusters.firstDayOfYear())
        while (!inclusiveStart.isAfter(year)) {
            missing.add(year)
            year = year.minusYears(1)
        }

        return missing
    }

    private fun selectDailyImports(inclusiveStart: LocalDate): Set<LocalDate> {
        val missing = TreeSet<LocalDate>()

        var day = LocalDate.now().minusDays(1)
        while (!inclusiveStart.isAfter(day)) {
            missing.add(day)
            day = day.minusDays(1)
        }

        missing.removeAll(importRepository.findAllDates())
        return missing
    }
}
