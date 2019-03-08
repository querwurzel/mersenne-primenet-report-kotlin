package org.mersenne.primenet.results

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ResultService @Autowired constructor(
        private val resultRepository: ResultRepository
) {
    fun fetchRecentResultsByUser(user: String): List<Result> {
        return resultRepository.findTop10ByUserNameOrderByDateDesc(user)
    }
}
