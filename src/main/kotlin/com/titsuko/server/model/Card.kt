package com.titsuko.server.model

import com.titsuko.server.model.`object`.CardStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "cards")
class Card(
    @Id
    @Column(name = "card_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val slug: String = "",

    @Column(nullable = false)
    val title: String = "",

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: CardStatus = CardStatus.Hidden
)