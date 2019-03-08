package org.mersenne.primenet.results

import com.fasterxml.jackson.annotation.JsonIgnore
import org.mersenne.primenet.imports.Import
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.*

@Entity
@Table(name = "results", indexes = [Index(name = "idx_user", columnList = "userName")])
class Result : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long? = null

    @Column(nullable = false)
    private var time: LocalTime? = null
        @JsonIgnore
        get() = field

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "date", nullable = false, referencedColumnName = "date", foreignKey = ForeignKey(name = "fk_date"))
    private var theImport: Import? = null

    @Column(name = "date", nullable = false, insertable = false, updatable = false)
    private var date: LocalDate? = null
        @JsonIgnore
        get() = field

    @Column(nullable = false)
    private var exponent: String? = null
    @Column(nullable = false)
    private var userName: String? = null
    @Column(nullable = false)
    private var computerName: String? = null
    @Column(nullable = false)
    private var resultType: String? = null
    @Column(nullable = false)
    private var ghzDays: String? = null

    private var assignmentAge: String? = null
    private var message: String? = null

    @Transient
    fun getUrl() = "https://www.mersenne.org/report_exponent/?full=1&exp_lo=" + this.exponent

    @Transient
    fun getDateReceived() = LocalDateTime.of(this.date, this.time)

    fun setImport(theImport: Import): Result {
        this.theImport = theImport
        return this
    }

    fun setExponent(exponent: String): Result {
        this.exponent = exponent
        return this
    }

    fun setUserName(userName: String): Result {
        this.userName = userName
        return this
    }

    fun setComputerName(computerName: String): Result {
        this.computerName = computerName
        return this
    }

    fun setResultType(resultType: String): Result {
        this.resultType = resultType
        return this
    }

    fun setDate(date: LocalDate): Result {
        this.date = date
        return this
    }

    fun setTime(time: LocalTime): Result {
        this.time = time
        return this
    }

    fun setAssignmentAge(assignmentAge: String?): Result {
        this.assignmentAge = assignmentAge
        return this
    }

    fun setGhzDays(ghzDays: String): Result {
        this.ghzDays = ghzDays
        return this
    }

    fun setMessage(message: String?): Result {
        this.message = message
        return this
    }
}
