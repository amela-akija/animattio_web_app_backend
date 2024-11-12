package com.animattio.animattio_web_app_backend.patient;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/patients")
@CrossOrigin(origins = "http://localhost:3000")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/create-patient")
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) throws InterruptedException, ExecutionException {
        return patientService.createPatient(patient);
    }
    @PreAuthorize("hasRole('doctor')")
    @GetMapping("/get-patient")
    public ResponseEntity<?> getPatient(@RequestParam String documentId) throws ExecutionException, InterruptedException {
        Patient patient = patientService.getPatient(documentId);
        if (patient != null) {
            return ResponseEntity.ok(patient);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Patient not found"));
        }
    }
    @PreAuthorize("hasRole('doctor')")
    @PutMapping("/update-patient")
    public ResponseEntity<?> updatePatient(@RequestParam String documentId, @RequestBody Patient updatedPatient) {
        try {
            patientService.updatePatient(documentId, updatedPatient);
            return ResponseEntity.ok(Collections.singletonMap("message", "Patient updated successfully"));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error updating patient: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }


    @GetMapping("/{documentId}/age")
    public Long getPatientAge(@PathVariable String documentId) {
        try {
            return patientService.getPatientAge(documentId);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving patient age");
        }
    }

    @GetMapping("/{documentId}/gender")
    public String getPatientGender(@PathVariable String documentId) {
        try {
            return patientService.getPatientGender(documentId);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving patient gender");
        }
    }
    @GetMapping("/{documentId}/type")
    public String getPatientType(@PathVariable String documentId) {
        try {
            return patientService.getPatientType(documentId);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving patient type");
        }
    }


    @GetMapping("/get-patient-id")
    public ResponseEntity<?> getPatientIdByUsername(@RequestParam String username) {
        try {
            String documentId = patientService.getPatientDocumentIdByUsername(username);
            if (documentId != null) {
                return ResponseEntity.ok(Collections.singletonMap("documentId", documentId));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "No patient found for the provided username"));
            }
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An error occurred while retrieving the patient ID"));
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
    @GetMapping("/get-patients-by-age")
    public ResponseEntity<?> getPatientsByAge(@RequestParam String doctorId, int age) {
        try {
            List<Patient> patients = patientService.getPatientsByAge(doctorId, age);
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

    @GetMapping("/get-patients-by-gender")
    public ResponseEntity<?> getPatientsByGender(@RequestParam String doctorId, String gender) {
        try {
            List<Patient> patients = patientService.getPatientsByGender(doctorId, gender);
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

    @GetMapping("/get-patients-by-username")
    public ResponseEntity<?> getPatientsByUsername(@RequestParam String doctorId, String username) {
        try {
            List<Patient> patients = patientService.getPatientsByUsername(doctorId, username);
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


    @GetMapping("/get-patients-by-type")
    public ResponseEntity<?> getPatientsByType(@RequestParam String doctorId, String type) {
        try {
            List<Patient> patients = patientService.getPatientsByType(doctorId, type);
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

    @GetMapping("/patient-exists")
    public ResponseEntity<?> checkIfPatientExists(@RequestParam String username) {
        try {
            boolean exists = patientService.doesPatientExist(username);
            return ResponseEntity.ok(Collections.singletonMap("exists", exists));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error checking patient existence: " + e.getMessage()));
        }
    }
}
