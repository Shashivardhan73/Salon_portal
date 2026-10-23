package com.salon.repository;

import com.salon.model.Appointment;
import com.salon.model.Client;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findAllByAppointmentAtGreaterThanEqualOrderByAppointmentAtAsc(
            LocalDateTime appointmentAt);

    List<Appointment> findByClientOrderByAppointmentAtDesc(Client client);
}
