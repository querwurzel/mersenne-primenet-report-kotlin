package org.mersenne.primenet.results

import org.mersenne.primenet.PrimeNetProperties
import org.mersenne.primenet.imports.Import.State
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicReference

@RestController
@RequestMapping("/results/meta")
class AdministrativeController @Autowired constructor(
        primeNetProperties: PrimeNetProperties,
        private val administrativeService: AdministrativeService
) {

    private val identity = primeNetProperties.identity

    private val meta = AtomicReference(Meta(this.identity))

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    protected fun getMetaData() = this.meta.get()

    @Scheduled(initialDelay = 5 * 60 * 1000, fixedDelay = 6 * 60 * 60 * 1000)
    protected fun refreshMeta() {
        this.meta.set(Meta()
                .setResults(administrativeService.countResults())
                .setImportStates(administrativeService.countImportsPerState())
                .setUser(identity)
                .setUserResults(administrativeService.countResultsByUserName(identity)))
    }

    protected inner class Meta {

        val lastUpdated = LocalDateTime.now()!!

        val imports = ImportMeta()

        val user = UserMeta()

        val results = ResultMeta()

        constructor()

        constructor(user: String) {
            this.setUser(user)
        }

        fun setResults(total: Long): Meta {
            this.results.total = total
            return this
        }

        fun setUser(user: String): Meta {
            this.user.name = user
            return this
        }

        fun setUserResults(total: Long): Meta {
            this.user.total = total
            return this
        }

        fun setImportStates(states: Map<State, Long>): Meta {
            this.imports.states = states
            return this
        }
    }

    protected data class UserMeta(
            var name: String? = null,
            var total: Long = 0
    )

    protected data class ImportMeta(
            var states: Map<State, Long> = emptyMap()
    ) {
        fun getTotal() = this.states.values.stream().mapToLong { value -> value }.sum()
    }

    protected data class ResultMeta (
            var total: Long = 0
    )
}
