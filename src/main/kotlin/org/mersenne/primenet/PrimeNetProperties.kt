package org.mersenne.primenet

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.validation.annotation.Validated
import java.time.LocalDate
import java.util.Objects
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Past

@Validated
@ConfigurationProperties(prefix = "imports")
class PrimeNetProperties {

    @NotNull
    @NotBlank
    var identity: String = "ANONYMOUS"
        set(value) {
            field = when (value.isBlank()) {
                true -> field
                else -> value
            }
        }

    /**
     * PrimeNet result archives exists as of 1997-11-11.
     */
    @NotNull
    @Past
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var start: LocalDate = LocalDate.now().minusDays(1)
        set(value) {
            field = when (Objects.isNull(value)) {
                true -> LocalDate.now().minusDays(1)
                else -> value
            }
        }

}
