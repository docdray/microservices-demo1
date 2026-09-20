package org.acme

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import io.quarkus.panache.common.Sort
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime

@Entity
class Message : PanacheEntity() {
    companion object : PanacheCompanion<Message> {
        fun search(
            text: String?,
            createdFrom: LocalDateTime?,
            createdTo: LocalDateTime?,
            updatedFrom: LocalDateTime?,
            updatedTo: LocalDateTime?
        ): List<Message> {
            val conditions = mutableListOf<String>()
            val params = mutableMapOf<String, Any>()

            if (!text.isNullOrBlank()) {
                conditions += "lower(text) like :text"
                params["text"] = "%${text.lowercase()}%"
            }
            if (createdFrom != null) {
                conditions += "createdAt >= :createdFrom"
                params["createdFrom"] = createdFrom
            }
            if (createdTo != null) {
                conditions += "createdAt <= :createdTo"
                params["createdTo"] = createdTo
            }
            if (updatedFrom != null) {
                conditions += "updatedAt >= :updatedFrom"
                params["updatedFrom"] = updatedFrom
            }
            if (updatedTo != null) {
                conditions += "updatedAt <= :updatedTo"
                params["updatedTo"] = updatedTo
            }

            if (conditions.isEmpty()) return listAll(Sort.by("id"))
            return list(conditions.joinToString(" and "), Sort.by("id"), params)
        }
    }

    @Column(nullable = false, columnDefinition = "text")
    lateinit var text: String

    lateinit var createdAt: LocalDateTime
    lateinit var updatedAt: LocalDateTime

    @PrePersist
    fun onCreate() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
