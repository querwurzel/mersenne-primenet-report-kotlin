package org.mersenne.primenet

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(PrimeNetProperties::class)
@SpringBootApplication
class PrimeNetReport {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(PrimeNetReport::class.java, *args)
        }
    }
}
