package com.animattio.animattio_web_app_backend.patient;

import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
/**
 * PatientController
 *
 * This class provides REST endpoints for managing patients in the system.
 * Operations include creating, updating, retrieving, and deleting patients.
 * Role-based security is applied.
 */
@RestController
@RequestMapping("/patients")
//@CrossOrigin(origins = "http://localhost:3000")
public class PatientController {
    private final PatientService patientService;
    /**
     * Constructor to inject PatientService dependency.
     *
     * @param patientService the service handling patient operations.
     */
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }
    /**
     * Creates a new patient record.
     *
     * @param patient The Patient object to create.
     * @return A ResponseEntity containing success or error message.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @PostMapping("/create-patient")
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) throws InterruptedException, ExecutionException {
        return patientService.createPatient(patient);
    }

    /**
     * Retrieves a patient by document ID.
     *
     * @param documentId The ID of the patient document.
     * @return The Patient object or an error message if not found.
     * @throws ExecutionException
     * @throws InterruptedException
     */
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

    /**
     * Updates a patient record by document ID.
     *
     * @param documentId The ID of the patient document to update.
     * @param updatedPatient The updated Patient object.
     * @return A ResponseEntity containing success or error message.
     */
    @PreAuthorize("hasRole('doctor')")
    @PutMapping("/update-patient")
    public ResponseEntity<?> updatePatient(@RequestParam String documentId, @RequestBody Patient updatedPatient) {
        try {
            patientService.updatePatient(documentId, updatedPatient);
            return ResponseEntity.ok(Collections.singletonMap("message", "Patient updated successfully"));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error while updating patient: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * Retrieves the age of a patient by document ID.
     *
     * @param documentId The ID of the patient document.
     * @return The age of the patient.
     */
    @GetMapping("/{documentId}/age")
    public Long getPatientAge(@PathVariable String documentId) {
        try {
            return patientService.getPatientAge(documentId);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving patient age");
        }
    }

    /**
     * Retrieves the gender of a patient by document ID.
     *
     * @param documentId The ID of the patient document.
     * @return The gender of the patient.
     */
    @GetMapping("/{documentId}/gender")
    public String getPatientGender(@PathVariable String documentId) {
        try {
            return patientService.getPatientGender(documentId);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving patient gender");
        }
    }

    /**
     * Retrieves the type of a patient by document ID.
     *
     * @param documentId The ID of the patient document.
     * @return The type of the patient.
     */
    @GetMapping("/{documentId}/type")
    public String getPatientType(@PathVariable String documentId) {
        try {
            return patientService.getPatientType(documentId);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving patient type");
        }
    }

    /**
     * Retrieves a patient document ID by username.
     *
     * @param username The username of the patient.
     * @return The document ID or an error message if not found.
     */
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

    /**
     * Retrieves all patients for a specific doctor.
     *
     * @param doctorId The ID of the doctor.
     * @return A list of patients or an error message if none found.
     */
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

    /**
     * Retrieves patients by age range for a specific doctor.
     *
     * @param doctorId The ID of the doctor.
     * @param minAge The minimum age.
     * @param maxAge The maximum age.
     * @return A list of patients within the specified age range or an error message if none found.
     */
    @PreAuthorize("hasRole('doctor')")
    @GetMapping("/get-patients-by-age")
    public ResponseEntity<?> getPatientsByAgeRange(@RequestParam String doctorId,
                                                   @RequestParam int minAge,
                                                   @RequestParam int maxAge) {
        try {
            List<Patient> patients = patientService.getPatientsByAge(doctorId, minAge, maxAge);
            if (!patients.isEmpty()) {
                return ResponseEntity.ok(patients);
            } else {
                System.out.println("No patients found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "No patients found for this doctor in the given age range"));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An error occurred while retrieving patients: " + e.getMessage()));
        }
    }


    /**
     * Retrieves patients by gender for a specific doctor.
     *
     * @param doctorId The ID of the doctor.
     * @param gender The gender of the patients to filter.
     * @return A list of patients with the specified gender or an error message if none found.
     */
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

    /**
     * Retrieves patients by username for a specific doctor.
     *
     * @param doctorId The ID of the doctor.
     * @param username The username of the patient (optional).
     * @return A list of patients matching the username or an error message if none found.
     */
    @GetMapping("/get-patients-by-username")
    public ResponseEntity<?> getPatientsByUsername(
            @RequestParam String doctorId,
            @RequestParam(required = false) String username) {
        try {
            List<Patient> patients = patientService.getPatientsByUsername(doctorId, username);

            if (!patients.isEmpty()) {
                return ResponseEntity.ok(patients);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "No patients found for this doctor"));
            }
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "An error occurred while retrieving patients"));
        }
    }


    /**
     * Retrieves patients by type for a specific doctor.
     *
     * @param doctorId The ID of the doctor.
     * @param type The type of the patients to filter.
     * @return A list of patients with the specified type or an error message if none found.
     */
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

    /**
     * Deletes a patient by document ID.
     *
     * @param documentId The ID of the patient document to delete.
     * @return A ResponseEntity containing success or error message.
     */
    @DeleteMapping("/delete-patient")
    public ResponseEntity<?> deletePatient(@RequestParam String documentId) {
        return patientService.deletePatient(documentId);
    }

    /**
     * Checks if a patient exists by username.
     *
     * @param username The username to check.
     * @return A boolean indicating if the patient exists.
     */
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
    /**
     * Deletes a patient by username.
     *
     * @param username The username of the patient to delete.
     * @return A success message upon deletion.
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws FirebaseAuthException
     */
//    @PreAuthorize("hasRole('doctor')")
    @DeleteMapping("/delete-by-username")
    public String deletePatientByUsername(@RequestParam String username) throws ExecutionException, InterruptedException, FirebaseAuthException, FirebaseAuthException {
        return patientService.deletePatientByUsername(username);
    }

}
