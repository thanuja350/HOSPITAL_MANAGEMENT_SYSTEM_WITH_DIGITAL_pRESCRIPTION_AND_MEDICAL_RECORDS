package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Appointment;
import com.example.demo.model.Patient;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Appointment findTopByPatientOrderByIdDesc(Patient patient);

}