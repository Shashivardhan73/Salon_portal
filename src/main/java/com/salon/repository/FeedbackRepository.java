package com.salon.repository;

import com.salon.model.Client;
import com.salon.model.Feedback;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findAllByOrderBySubmittedAtDesc();

    List<Feedback> findByClientOrderBySubmittedAtDesc(Client client);
}
