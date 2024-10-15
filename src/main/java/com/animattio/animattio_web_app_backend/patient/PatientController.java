package com.animattio.animattio_web_app_backend.patient;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/create-patient")
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) throws InterruptedException, ExecutionException {
        return patientService.createPatient(patient);
    }

    @GetMapping("/get-patient")
    public ResponseEntity<?> getPatient(@RequestParam String documentId) throws InterruptedException, ExecutionException {
        Patient patient = patientService.getPatient(documentId);
        if (patient != null) {
            return ResponseEntity.ok(patient);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Patient not found"));
        }
    }
    @GetMapping("/get-all-patients")
    public ResponseEntity<?> getAllPatients(@RequestParam String doctorId) {
        try {
            List<Patient> patients = patientService.getAllPatients(doctorId);
            if (!patients.isEmpty()) {
                return ResponseEntity.ok(patients);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "No patients found for this doctor"));
            }
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An error occurred while retrieving patients"));
        }
    }

    @DeleteMapping("/delete-patient")
    public ResponseEntity<?> deletePatient(@RequestParam String documentId) {
        return patientService.deletePatient(documentId);
    }
}
