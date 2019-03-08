package org.mersenne.primenet.imports

import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Objects
import javax.persistence.*

@Entity
@Table(name = "imports", indexes = [Index(name = "idx_state", columnList = "state")])
class Import() : Serializable {

    companion object {
        private const val MAX_ATTEMPTS = 3
    }

    constructor(date: LocalDate) : this() {
        this.date = Objects.requireNonNull(date)
    }

    @Id
    var date = LocalDate.now()

    @Column(nullable = false)
    private var attempts = 0
    private var lastAttempt: LocalDateTime? = null

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private var state = State.PENDING
    private var reason: String? = null

    fun reset(): Import {
        this.attempts = 0
        this.lastAttempt = null
        this.reason = null
        this.state = State.PENDING
        return this
    }

    fun hasNextAttempt() = this.attempts < MAX_ATTEMPTS

    fun nextAttempt(): Import {
        if (this.hasNextAttempt()) {
            this.lastAttempt = LocalDateTime.now()
            this.state = State.ACTIVE
            return this
        }

        throw IllegalStateException("Import has reached max attempts")
    }

    fun succeeded() = this.handleSuccess()

    fun failed(reason: String?) = this.handleNonSuccess(reason)

    private fun handleSuccess(): Import {
        this.reason = null
        this.attempts++
        this.state = State.SUCCESS
        return this
    }

    private fun handleNonSuccess(reason: String?): Import {
        this.reason = reason
        this.attempts++

        if (this.hasNextAttempt()) {
            this.state = State.PENDING
        } else {
            this.state = State.FAILURE
        }

        return this
    }

    enum class State {
        PENDING,
        ACTIVE,
        FAILURE,
        SUCCESS
    }
}
