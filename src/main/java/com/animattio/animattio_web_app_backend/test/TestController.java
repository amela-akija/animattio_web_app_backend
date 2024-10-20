package com.animattio.animattio_web_app_backend.test;

import com.animattio.animattio_web_app_backend.game.Game;
import com.animattio.animattio_web_app_backend.patient.Patient;
import com.animattio.animattio_web_app_backend.patient.PatientService;
import com.google.api.gax.rpc.NotFoundException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
public class TestController {
    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/tests/{documentId}")
    public Test getTest(@PathVariable String documentId) throws ExecutionException, InterruptedException {
        Test test = testService.getTest(documentId);
        if (test != null) {
            return test;
        } else {
            throw new ResourceNotFoundException("Test not found");
        }
    }

    @GetMapping("/get-all-tests")
    public ResponseEntity<?> getAllPatients(@RequestParam String userId) {
        try {
            List<Test> tests = testService.getTestsByUser(userId);
            if (!tests.isEmpty()) {
                return ResponseEntity.ok(tests);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "No tests found for this user"));
            }
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An error occurred while retrieving tests"));
        }
    }

    @GetMapping("/sum-errors/{testId}")
    public ResponseEntity<Map<String, ?>> sumErrorsForTest(@PathVariable String testId) {
        try {
            Map<String, Object> errorSums = testService.sumErrorsForTest(testId);
            return ResponseEntity.ok(errorSums);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An error occurred while summing the errors"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
