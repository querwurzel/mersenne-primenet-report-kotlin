package org.mersenne.primenet.imports

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import org.mersenne.primenet.imports.Import.State
import kotlin.collections.Map.Entry

@Repository
interface ImportRepository : ImportRepositoryCustom, JpaRepository<Import, LocalDate> {

    // used for retries
    fun findTop180ByState(state: State): List<Import>

    // used for bootstrapping imports
    @Query("SELECT date FROM #{#entityName}")
    fun findAllDates(): List<LocalDate>

    // used for cleanup
    fun findAllByStateAndLastAttemptBefore(state: State, lastAttempt: LocalDateTime): List<Import>

    // used for meta
    @Query("SELECT state AS key, COUNT(state) AS value FROM #{#entityName} GROUP BY state")
    fun countPerState(): List<Entry<State, Long>>

}
