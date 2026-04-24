package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.Doctor;
import com.example.demo.model.Patient;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.repository.PatientRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PatientRepository patientRepo;

    // LOGIN PAGE
    @GetMapping("/login")
    public String loginPage() {
        return "admin_login";
    }

    // LOGIN CHECK
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model) {

        if (username.equals("admin") && password.equals("admin")) {
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("error", "Invalid Credentials");
            return "admin_login";
        }
    }

    // DASHBOARD
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin_dashboard";
    }

    // ================== DOCTOR ==================

    @GetMapping("/addDoctor")
    public String addDoctorForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        return "add_doctor";
    }

    @PostMapping("/saveDoctor")
    public String saveDoctor(@ModelAttribute Doctor doctor) {
        doctorRepo.save(doctor);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/viewDoctors")
    public String viewDoctors(Model model) {
        model.addAttribute("doctors", doctorRepo.findAll());
        return "view_doctors";
    }

    // ================== PATIENT ==================

    @GetMapping("/addPatient")
    public String addPatientForm(Model model) {
        model.addAttribute("patient", new Patient());
        model.addAttribute("doctors", doctorRepo.findAll()); // 🔥 load doctors
        return "add_patient";
    }

    @PostMapping("/savePatient")
    public String savePatient(
            @ModelAttribute Patient patient,
            @RequestParam Long doctorId) {

        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);

        patient.setDoctor(doctor);   // 🔥 assign doctor
        patient.setStatus("PENDING");

        patientRepo.save(patient);

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/viewPatients")
    public String viewPatients(Model model) {
        model.addAttribute("patients", patientRepo.findAll());
        return "view_patients";
    }
}