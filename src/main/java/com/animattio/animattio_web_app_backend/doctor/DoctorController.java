package com.animattio.animattio_web_app_backend.doctor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/doctors")
public class DoctorController {
    public DoctorService doctorService;
    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/create-doctor")
    public String createDoctor(@RequestBody Doctor doctor) throws InterruptedException, ExecutionException {
        return doctorService.createDoctor(doctor);
    }

    @GetMapping("/get-doctor")
    public Doctor getDoctor(@RequestParam String documentId) throws InterruptedException, ExecutionException {
        return doctorService.getDoctor(documentId);
    }
    @PreAuthorize("hasRole('doctor')")
    @DeleteMapping("/delete-doctor")
    public String deleteDoctor(@RequestParam String documentId) throws InterruptedException, ExecutionException {
        return doctorService.deleteDoctor(documentId);
    }
    @PreAuthorize("hasRole('doctor')")
    @GetMapping("/get-doctor-list")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        try {
            List<Doctor> doctors = doctorService.getAllDoctors();
            return ResponseEntity.ok(doctors);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/update-username/{currentUsername}/{newUsername}")
    public String updateDoctorUsername(@PathVariable String currentUsername, @PathVariable String newUsername) {
        try {
            return doctorService.updateDoctorUsername(currentUsername, newUsername);
        } catch (ExecutionException | InterruptedException e) {
            return "Error updating username: " + e.getMessage();
        }
    }
}
