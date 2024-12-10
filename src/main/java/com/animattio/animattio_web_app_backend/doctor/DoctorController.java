package com.animattio.animattio_web_app_backend.doctor;

import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * REST Controller for managing doctor-related operations.
 */
@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * Constructor to inject DoctorService dependency.
     *
     * @param doctorService the service handling doctor operations.
     */
    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    /**
     * Creates a new doctor.
     *
     * Accessible only to users with the "admin" role.
     *
     * @param doctor The doctor object to be created.
     * @return A string indicating the success of the operation.
     * @throws InterruptedException If the thread is interrupted.
     * @throws ExecutionException   If an exception occurs during Firestore operations.
     */
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/create-doctor")
    public String createDoctor(@RequestBody Doctor doctor) throws InterruptedException, ExecutionException {
        return doctorService.createDoctor(doctor);
    }

    /**
     * Retrieves the username of a doctor by document ID.
     *
     * @param documentId The document ID of the doctor.
     * @return A ResponseEntity containing the username or an error message.
     */
    @GetMapping("/username/{documentId}")
    public ResponseEntity<String> getDoctorUsername(@PathVariable String documentId) {
        try {
            String username = doctorService.getDoctorUsername(documentId);
            if (username != null) {
                return ResponseEntity.ok(username); // returns an HTTP 200 (OK) response with the retrieved username as the response body
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found"); // HTTP 404 (Not Found) response status when username is null
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching username"); // HTTP 500 (Internal Server Error) response status
        }
        // @PathVariable for values that are part of the URL structure and uniquely identify a resource
        // @RequestParam for optional query parameters or additional data not critical to the resource path
    }

    /**
     * Updates a doctor's profile.
     *
     * Accessible to users with the "doctor" or "admin" role.
     *
     * @param username The username of the doctor.
     * @param email    The new email to update (optional).
     * @param password The new password to update (optional).
     * @return A ResponseEntity with a success or error message.
     */
    @PutMapping("/update-profile")
    @PreAuthorize("hasRole('doctor') or hasRole('admin')")
    public ResponseEntity<String> updateDoctorProfile(@RequestParam String username,
                                                      @RequestParam(required = false) String email,
                                                      @RequestParam(required = false) String password) {
        try {
            doctorService.updateDoctorProfile(username, email, password);
            return ResponseEntity.ok("Profile updated successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating profile: " + e.getMessage());
        }
    }

    /**
     * Checks if a doctor exists by username.
     *
     * Accessible only to users with the "admin" role.
     *
     * @param username The username to check.
     * @return A ResponseEntity with a map containing the existence status or an error message.
     */
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/doctor-exists")
    public ResponseEntity<?> checkIfDoctorExists(@RequestParam String username) {
        try {
            boolean exists = doctorService.doesDoctorExist(username);
            return ResponseEntity.ok(Collections.singletonMap("exists", exists));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error checking doctor existence: " + e.getMessage()));
        }
    }

    /**
     * Deletes a doctor by username.
     *
     * Accessible only to users with the "admin" role.
     *
     * @param username The username of the doctor to delete.
     * @return A string indicating the result of the operation.
     * @throws ExecutionException   If an exception occurs during Firestore operations.
     * @throws InterruptedException If the thread is interrupted.
     * @throws FirebaseAuthException If there is an error with Firebase Authentication.
     */
    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/delete-by-username")
    public String deleteDoctorByUsername(@RequestParam String username) throws ExecutionException, InterruptedException, FirebaseAuthException {
        return doctorService.deleteDoctorByUsername(username);
    }

    /**
     * Retrieves a doctor's details by document ID.
     *
     * @param documentId The document ID of the doctor.
     * @return The Doctor object.
     * @throws InterruptedException If the thread is interrupted.
     * @throws ExecutionException   If an exception occurs during Firestore operations.
     */
    @GetMapping("/get-doctor")
    public Doctor getDoctor(@RequestParam String documentId) throws InterruptedException, ExecutionException {
        return doctorService.getDoctor(documentId);
    }

    /**
     * Deletes a doctor by document ID.
     *
     * Accessible only to users with the "doctor" role.
     *
     * @param documentId The document ID of the doctor to delete.
     * @return A string indicating the success of the deletion.
     * @throws InterruptedException If the thread is interrupted.
     * @throws ExecutionException   If an exception occurs during Firestore operations.
     */
    @PreAuthorize("hasRole('doctor')")
    @DeleteMapping("/delete-doctor")
    public String deleteDoctor(@RequestParam String documentId) throws InterruptedException, ExecutionException {
        return doctorService.deleteDoctor(documentId);
    }

    /**
     * Retrieves a list of all doctors except those with the "admin" role.
     *
     * Accessible only to users with the "doctor" role.
     *
     * @return A ResponseEntity containing the list of doctors or an error response.
     */
    @PreAuthorize("hasRole('doctor')")
    @GetMapping("/get-doctor-list")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        try {
            List<Doctor> doctors = doctorService.getAllDoctors();
            return ResponseEntity.ok(doctors);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Updates a doctor's username.
     *
     * @param currentUsername The current username of the doctor.
     * @param newUsername     The new username to assign.
     * @return A string indicating the success or error of the operation.
     */
    @PutMapping("/update-username/{currentUsername}/{newUsername}")
    public String updateDoctorUsername(@PathVariable String currentUsername, @PathVariable String newUsername) {
        try {
            return doctorService.updateDoctorUsername(currentUsername, newUsername);
        } catch (ExecutionException | InterruptedException e) {
            return "Error updating username: " + e.getMessage();
        }
    }
}
