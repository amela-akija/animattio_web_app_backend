package com.animattio.animattio_web_app_backend.test;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/summed-errors/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getSummedErrorsForUserTests(@PathVariable String userId) {
        try {
            List<Map<String, Object>> result = testService.sumErrorsForUserTests(userId);
            return ResponseEntity.ok(result);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{testId}/processed-games")
    public ResponseEntity<Map<String, List<Long>>> getProcessedGames(@PathVariable String testId) {
        try {
            Map<String, List<Long>> processedGamesData = testService.processTappedImagesForTest(testId);
            return ResponseEntity.ok(processedGamesData);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }
}
