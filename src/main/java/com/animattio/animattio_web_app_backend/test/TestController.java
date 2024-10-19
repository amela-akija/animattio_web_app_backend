package com.animattio.animattio_web_app_backend.test;

import com.animattio.animattio_web_app_backend.patient.Patient;
import com.animattio.animattio_web_app_backend.patient.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@RestController
public class TestController {
    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/get-test")
    public ResponseEntity<?> getTest(@RequestParam String documentId) throws ExecutionException, InterruptedException {
        Test test = testService.getTest(documentId);
        if (test != null) {
            return ResponseEntity.ok(test);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Test not found"));
        }
    }
}
