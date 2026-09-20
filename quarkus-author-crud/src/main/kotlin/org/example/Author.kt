package org.example

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.Entity
import java.time.LocalDate

@Entity
class Author : PanacheEntity() {
    companion object : PanacheCompanion<Author> {
        fun search(term: String): List<Author> {
            val pattern = "%${term.lowercase()}%"
            return list("lower(firstName) like ?1 or lower(lastName) like ?1", pattern)
        }
    }

    lateinit var firstName: String
    lateinit var lastName: String
    lateinit var birthDate: LocalDate
}
