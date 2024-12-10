package com.animattio.animattio_web_app_backend.doctor;

/**
 * Represents a Doctor entity in the system.
 * This class contains doctor attributes.
 */
public class Doctor {

    /**
     * The unique username of the doctor.
     */
    private String username;

    /**
     * The role of the doctor (e.g., "doctor", "admin").
     */
    private String role;

    /**
     * Retrieves the username of the doctor.
     *
     * @return The username of the doctor.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the doctor.
     *
     * @param username The username to be assigned to the doctor.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieves the role of the doctor.
     *
     * @return The role of the doctor (e.g., "doctor", "admin").
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role of the doctor.
     *
     * @param role The role to be assigned to the doctor (e.g., "doctor", "admin").
     */
    public void setRole(String role) {
        this.role = role;
    }
}
