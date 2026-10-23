package com.salon.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    private int rating;
    private String message;
    private LocalDateTime submittedAt;

    protected Feedback() {
    }

    public Feedback(Client client, int rating, String message) {
        this.client = client;
        this.rating = rating;
        this.message = message;
    }

    @PrePersist
    void setSubmittedAt() {
        submittedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public int getRating() {
        return rating;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}
