package org.mersenne.primenet.results

import org.mersenne.primenet.PrimeNetProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/results", "/")
class ResultController @Autowired constructor(
        primeNetProperties: PrimeNetProperties,
        private val resultService: ResultService
) {

    private val identity = primeNetProperties.identity

    @CrossOrigin
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    protected fun getMyRecentResults() = this.getRecentResultsByUser(identity)

    protected fun getRecentResultsByUser(user: String): ResponseEntity<List<Result>> {
        val results = resultService.fetchRecentResultsByUser(user)
        return ResponseEntity
                .status(
                        if (results.isEmpty())
                            HttpStatus.NO_CONTENT
                        else
                            HttpStatus.OK)
                .body(results)
    }
}
