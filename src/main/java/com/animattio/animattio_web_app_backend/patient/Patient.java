package com.animattio.animattio_web_app_backend.patient;

/**
 * Represents a patient in the system.
 * This class stores information about a patient, including their association with a doctor,
 * demographic details, and patient-specific type.
 */
public class Patient {

    /**
     * The unique username of the patient.
     */
    private String patientUsername;

    /**
     * The username of the doctor associated with the patient.
     */
    private String doctorUsername;

    /**
     * The gender of the patient.
     */
    private String gender;

    /**
     * The age of the patient.
     */
    private int age;

    /**
     * The type of the patient.
     */
    private String type;

    // Getters and Setters

    /**
     * Gets the patient's username.
     *
     * @return the patient's username.
     */
    public String getPatientUsername() {
        return patientUsername;
    }

    /**
     * Sets the patient's username.
     *
     * @param patientUsername the username to set for the patient.
     */
    public void setPatientUsername(String patientUsername) {
        this.patientUsername = patientUsername;
    }

    /**
     * Gets the doctor's username associated with the patient.
     *
     * @return the doctor's username.
     */
    public String getDoctorUsername() {
        return doctorUsername;
    }

    /**
     * Sets the doctor's username associated with the patient.
     *
     * @param doctorUsername the username to set for the doctor.
     */
    public void setDoctorUsername(String doctorUsername) {
        this.doctorUsername = doctorUsername;
    }

    /**
     * Gets the patient's gender.
     *
     * @return the gender of the patient.
     */
    public String getGender() {
        return gender;
    }

    /**
     * Sets the patient's gender.
     *
     * @param gender the gender to set for the patient.
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * Gets the patient's age.
     *
     * @return the age of the patient.
     */
    public int getAge() {
        return age;
    }

    /**
     * Sets the patient's age.
     *
     * @param age the age to set for the patient.
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Gets the patient's type.
     *
     * @return the type of the patient.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the patient's type.
     *
     * @param type the type to set for the patient.
     */
    public void setType(String type) {
        this.type = type;
    }
}
