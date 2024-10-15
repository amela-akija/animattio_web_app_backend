package com.animattio.animattio_web_app_backend.patient;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Patient {
    private String patientUsername;
    private String doctorUsername;
    private String gender;
    private int age;
    private String type;
}