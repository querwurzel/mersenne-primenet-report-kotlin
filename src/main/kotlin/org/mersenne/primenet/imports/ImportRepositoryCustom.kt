package org.mersenne.primenet.imports

import java.time.LocalDate

interface ImportRepositoryCustom {

    fun hasImports(): Boolean

    fun hasImportGapsSince(inclusiveStart: LocalDate): Boolean

}
