package org.example

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn

@Entity
class Book : PanacheEntity() {
    companion object : PanacheCompanion<Book> {
        fun search(term: String): List<Book> {
            val pattern = "%${term.lowercase()}%"
            return list("lower(title) like ?1 or lower(isbn) like ?1", pattern)
        }
    }

    lateinit var title: String

    @Column(unique = true)
    lateinit var isbn: String

    @ElementCollection
    @CollectionTable(name = "book_authors", joinColumns = [JoinColumn(name = "book_id")])
    @Column(name = "author_id")
    var authorIds: MutableSet<Long> = mutableSetOf()
}
