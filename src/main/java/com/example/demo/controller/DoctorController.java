package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.*;
import com.example.demo.repository.*;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private PrescriptionRepository prescriptionRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    // ================= LOGIN =================
    @GetMapping("/login")
    public String loginPage() {
        return "doctor_login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model) {

        List<Doctor> doctors = doctorRepo.findAll();

        for (Doctor d : doctors) {
            if (d.getEmail().equals(email) && d.getPassword().equals(password)) {
                return "redirect:/doctor/dashboard";
            }
        }

        model.addAttribute("error", "Invalid Credentials");
        return "doctor_login";
    }

    // ================= DASHBOARD =================
    @GetMapping("/dashboard")
    public String dashboard() {
        return "doctor_dashboard";
    }

    // ================= VIEW APPOINTMENTS =================
    @GetMapping("/viewAppointments")
    public String viewAppointments(Model model) {

        model.addAttribute("appointments", appointmentRepo.findAll());

        return "view_appointments";
    }

    // ================= ACCEPT APPOINTMENT =================
    @GetMapping("/accept/{id}")
    public String acceptAppointment(@PathVariable Long id) {

        Appointment appt = appointmentRepo.findById(id).orElse(null);

        if (appt != null) {

            appt.setStatus("ACCEPTED");

            // Assign doctor (for now first doctor)
            Doctor doctor = doctorRepo.findAll().get(0);
            appt.setDoctor(doctor);

            appointmentRepo.save(appt);

            Long patientId = appt.getPatient().getId();

            // Redirect to video consultation
            return "redirect:/patient/video?patientId=" + patientId + "&appointmentId=" + id;
        }

        return "redirect:/doctor/viewAppointments";
    }

    // ================= REJECT =================
    @GetMapping("/reject/{id}")
    public String rejectAppointment(@PathVariable Long id) {

        Appointment appt = appointmentRepo.findById(id).orElse(null);

        if (appt != null) {
            appt.setStatus("REJECTED");
            appointmentRepo.save(appt);
        }

        return "redirect:/doctor/viewAppointments";
    }

    // ================= ADD PRESCRIPTION PAGE =================
    @GetMapping("/addPrescription")
    public String addPrescriptionPage(@RequestParam Long patientId,
                                      @RequestParam Long appointmentId,
                                      Model model) {

        model.addAttribute("patientId", patientId);
        model.addAttribute("appointmentId", appointmentId);

        return "add_prescription";
    }

    // ================= SAVE PRESCRIPTION =================
    @PostMapping("/savePrescription")
    public String savePrescription(@RequestParam Long patientId,
                                   @RequestParam Long appointmentId,
                                   @RequestParam String medicines,
                                   @RequestParam String dosage,
                                   @RequestParam String healthDetails,
                                   @RequestParam String nextVisitDate) {

        Patient patient = patientRepo.findById(patientId).orElseThrow();

        Prescription p = new Prescription();
        p.setPatient(patient);
        p.setMedicines(medicines);
        p.setDosage(dosage);
        p.setHealthDetails(healthDetails);
        p.setNextVisitDate(LocalDate.parse(nextVisitDate));

        prescriptionRepo.save(p);

        // Mark appointment completed
        Appointment appt = appointmentRepo.findById(appointmentId).orElse(null);
        if (appt != null) {
            appt.setStatus("COMPLETED");
            appointmentRepo.save(appt);
        }

        // Redirect to payment
        return "redirect:/doctor/addPayment?patientId=" + patientId;
    }

    // ================= ADD PAYMENT PAGE =================
    @GetMapping("/addPayment")
    public String addPaymentPage(@RequestParam Long patientId, Model model) {

        model.addAttribute("patientId", patientId);

        return "add_payment";
    }

    // ================= SAVE PAYMENT =================
    @PostMapping("/savePayment")
    public String savePayment(@RequestParam Long patientId,
                              @RequestParam Double amount,
                              @RequestParam String mode) {

        Patient patient = patientRepo.findById(patientId).orElseThrow();

        Payment payment = new Payment();
        payment.setPatient(patient);
        payment.setAmount(amount);
        payment.setPaymentMode(mode);
        payment.setPaymentStatus("PAID");
        payment.setPaymentDate(LocalDate.now());

        paymentRepo.save(payment);

        // Redirect to patient prescription page
        return "redirect:/patient/viewPrescription/" + patientId;
    }
}