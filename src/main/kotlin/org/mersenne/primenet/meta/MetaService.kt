package org.mersenne.primenet.meta

import org.mersenne.primenet.imports.Import.State
import org.mersenne.primenet.imports.ImportRepository
import org.mersenne.primenet.results.ResultRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MetaService @Autowired constructor(
        private val importRepository: ImportRepository,
        private val resultRepository: ResultRepository
) {

    fun countResults() = resultRepository.count()

    fun countResultsByUserName(userName: String) = resultRepository.countAllByUserName(userName)

    fun countImports() = importRepository.count()

    fun countImportsPerState(): Map<State, Long> {
        return importRepository.countPerState()
                .map { it.key to it.value }
                .toMap()
    }
}
