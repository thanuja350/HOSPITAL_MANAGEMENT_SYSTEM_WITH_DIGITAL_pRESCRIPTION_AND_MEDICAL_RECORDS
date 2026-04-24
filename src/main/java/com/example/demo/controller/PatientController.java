package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.Appointment;
import com.example.demo.model.Doctor;
import com.example.demo.model.Feedback;
import com.example.demo.model.Patient;
import com.example.demo.model.Payment;
import com.example.demo.model.Prescription;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.repository.PatientRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.PrescriptionRepository;
import com.itextpdf.text.pdf.PdfPTable;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private PrescriptionRepository prescriptionRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private FeedbackRepository feedbackRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    // ================= LOGIN =================
    @GetMapping("/login")
    public String loginPage() {
        return "patient_login";
    }

    @PostMapping("/login")
    public String login(@RequestParam Long id, Model model) {

        Patient patient = patientRepo.findById(id).orElse(null);

        if (patient == null) {
            model.addAttribute("error", "Invalid Patient ID");
            return "patient_login";
        }

        return "redirect:/patient/dashboard/" + id;
    }

    // ================= DASHBOARD =================
    @GetMapping("/dashboard/{id}")
    public String dashboard(@PathVariable Long id, Model model) {

        Patient patient = patientRepo.findById(id).orElse(null);

        Appointment ap = appointmentRepo
                .findTopByPatientOrderByIdDesc(patient);

        model.addAttribute("patient", patient);
        model.addAttribute("appointment", ap);

        return "patient_dashboard";
    }

    // ================= REQUEST APPOINTMENT =================
    @PostMapping("/requestAppointment")
    public String requestAppointment(@RequestParam Long patientId,
                                     @RequestParam String symptoms) {

        Patient patient = patientRepo.findById(patientId).orElse(null);

        // 🔥 assign doctor automatically
        Doctor doctor = doctorRepo.findAll().get(0);

        Appointment ap = new Appointment();
        ap.setPatient(patient);
        ap.setDoctor(doctor);
        ap.setSymptoms(symptoms);
        ap.setStatus("PENDING");
        ap.setAppointmentDate(LocalDate.now());

        appointmentRepo.save(ap);

        // 🔥 IMPORTANT CHANGE
        return "redirect:/doctor/viewAppointments";
    }

    // ================= VIDEO =================
    @GetMapping("/video")
public String videoPage(@RequestParam Long patientId,
                        @RequestParam Long appointmentId,
                        Model model) {

    model.addAttribute("patientId", patientId);
    model.addAttribute("appointmentId", appointmentId);

    return "video_consultation";
}

    // ================= VIEW PRESCRIPTION =================
    @GetMapping("/viewPrescription/{id}")
    public String viewPrescription(@PathVariable Long id, Model model) {

        Patient patient = patientRepo.findById(id).orElse(null);

        List<Payment> payments = paymentRepo.findByPatient(patient);

        boolean isPaid = payments.stream()
                .anyMatch(p -> "PAID".equalsIgnoreCase(p.getPaymentStatus()));

        if (!isPaid) {
            model.addAttribute("patientId", id);
            return "payment_required";
        }

        List<Prescription> prescriptions =
                prescriptionRepo.findByPatient(patient);

        model.addAttribute("prescriptions", prescriptions);
        model.addAttribute("patientId", id);

        return "view_prescription";
    }

    // ================= PAY =================
    @PostMapping("/payNow")
    public String payNow(@RequestParam Long patientId) {

        Patient patient = patientRepo.findById(patientId).orElse(null);

        Payment payment = new Payment();
        payment.setPatient(patient);
        payment.setAmount(500.0);
        payment.setPaymentStatus("PAID");
        payment.setPaymentDate(LocalDate.now());

        paymentRepo.save(payment);

        return "redirect:/patient/viewPrescription/" + patientId;
    }

    // ================= FEEDBACK =================
    @GetMapping("/giveFeedback/{id}")
    public String giveFeedbackForm(@PathVariable Long id, Model model) {

        model.addAttribute("patientId", id);
        model.addAttribute("feedback", new Feedback());

        return "give_feedback";
    }

    @PostMapping("/saveFeedback")
    public String saveFeedback(@ModelAttribute Feedback feedback,
                              @RequestParam Long patientId) {

        Patient patient = patientRepo.findById(patientId).orElse(null);

        feedback.setPatient(patient);
        feedbackRepo.save(feedback);

        return "redirect:/patient/dashboard/" + patientId;
    }
    @GetMapping("/downloadPrescription/{id}")
public void downloadPrescription(@PathVariable Long id,
                                 HttpServletResponse response) throws Exception {

    Prescription p = prescriptionRepo.findById(id).orElseThrow();

    response.setContentType("application/pdf");
    response.setHeader("Content-Disposition", "attachment; filename=prescription.pdf");

    Document document = new Document();
    PdfWriter.getInstance(document, response.getOutputStream());

    document.open();

    // 🔵 TITLE
    Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    Paragraph title = new Paragraph("DIGITAL PRESCRIPTION SYSTEM", titleFont);
    title.setAlignment(Element.ALIGN_CENTER);
    document.add(title);

    document.add(new Paragraph(" "));

    // 🔵 HOSPITAL INFO
    document.add(new Paragraph("Hospital: SmartCare"));
    document.add(new Paragraph("Date: " + LocalDate.now()));
    document.add(new Paragraph(" "));

    // 🔵 PATIENT DETAILS
    document.add(new Paragraph("Patient: " + p.getPatient().getName()));
    document.add(new Paragraph("Health Details: " + p.getHealthDetails()));
    document.add(new Paragraph(" "));

    // 🔵 TABLE
    PdfPTable table = new PdfPTable(3);
    table.setWidthPercentage(100);

    table.addCell("Medicines");
    table.addCell("Dosage");
    table.addCell("Next Visit");

    table.addCell(p.getMedicines());
    table.addCell(p.getDosage());
    table.addCell(p.getNextVisitDate().toString());

    document.add(table);

    document.add(new Paragraph(" "));

    // 🔵 FOOTER
    Paragraph footer = new Paragraph("Doctor Signature: __________");
    footer.setAlignment(Element.ALIGN_RIGHT);
    document.add(footer);

    document.close();
}
}