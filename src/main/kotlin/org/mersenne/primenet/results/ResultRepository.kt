package org.mersenne.primenet.results

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import java.time.LocalDate
import javax.transaction.Transactional

@Repository
interface ResultRepository : JpaRepository<Result, Long> {

    fun findTop10ByUserNameOrderByDateDesc(userName: String): List<Result>

    fun countAllByUserName(userName: String): Long

    @Modifying
    @Transactional
    fun deleteAllByDate(date: LocalDate): Long

}
