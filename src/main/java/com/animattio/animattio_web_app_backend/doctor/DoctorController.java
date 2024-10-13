package com.animattio.animattio_web_app_backend.doctor;

import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
public class DoctorController {
    public DoctorService doctorService;
    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping("/create-doctor")
    public String createDoctor(@RequestBody Doctor doctor) throws InterruptedException, ExecutionException {
        return doctorService.createDoctor(doctor);
    }

    @GetMapping("/get-doctor")
    public Doctor getDoctor(@RequestParam String documentId) throws InterruptedException, ExecutionException {
        return doctorService.getDoctor(documentId);
    }
    @DeleteMapping("/delete-doctor")
    public String deleteDoctor(@RequestParam String documentId) throws InterruptedException, ExecutionException {
        return doctorService.deleteDoctor(documentId);
    }

}
