package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Payment;
import com.example.demo.model.Patient;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPatient(Patient patient);

    Payment findByPatientAndPaymentStatus(Patient patient, String paymentStatus);
}