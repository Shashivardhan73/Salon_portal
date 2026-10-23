package com.salon.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, length = 120)
    private String service;

    @Column(name = "appointment_at", nullable = false)
    private LocalDateTime appointmentAt;

    protected Appointment() {
    }

    public Appointment(Client client, String service, LocalDateTime appointmentAt) {
        this.client = client;
        this.service = service;
        this.appointmentAt = appointmentAt;
    }

    public Long getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public String getService() {
        return service;
    }

    public LocalDateTime getAppointmentAt() {
        return appointmentAt;
    }
}
