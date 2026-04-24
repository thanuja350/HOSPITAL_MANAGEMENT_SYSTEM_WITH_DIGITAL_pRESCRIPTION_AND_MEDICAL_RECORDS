package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
}