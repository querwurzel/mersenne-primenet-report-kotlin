package org.mersenne.primenet.imports

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Objects
import javax.persistence.EntityManager
import javax.persistence.NoResultException

class ImportRepositoryCustomImpl(
        private val entityManager: EntityManager
) : ImportRepositoryCustom {

    override fun hasImports(): Boolean {
        try {
            val cb = entityManager.criteriaBuilder
            val query = cb.createQuery()
            val root = query.from(Import::class.java)
            query.select(root.get("date"))

            val result = entityManager.createQuery(query)
            result.maxResults = 1

            return Objects.nonNull(result.singleResult)
        } catch (e: NoResultException) {
            return false
        }
    }

    override fun hasImportGapsSince(inclusiveStart: LocalDate): Boolean {
        val yesterday = LocalDate.now().minusDays(1)
        return this.hasImportGapsSince(inclusiveStart, yesterday)
    }

    private fun hasImportGapsSince(inclusiveStart: LocalDate, exclusiveEnd: LocalDate): Boolean {
        val days = ChronoUnit.DAYS.between(inclusiveStart, exclusiveEnd)

        val cb = entityManager.criteriaBuilder
        val query = cb.createQuery(Long::class.java)
        val root = query.from<Import>(Import::class.java)

        val gteStart = cb.greaterThanOrEqualTo(
                root.get("date"), inclusiveStart)
        val ltEnd = cb.lessThan(
                root.get("date"), exclusiveEnd)

        query.select(cb.count(root))
        query.where(gteStart, ltEnd)

        val result = entityManager.createQuery(query)
        return result.singleResult < days
    }
}
