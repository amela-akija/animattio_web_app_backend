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
/**
 * Controller for managing and processing test-related operations.
 */
@RestController
@RequestMapping("/tests")
public class TestController {
    private final TestService testService;
    /**
     * Constructs a new instance of TestController with the given service.
     *
     * @param testService The service layer for test-related operations.
     */
    public TestController(TestService testService) {
        this.testService = testService;
    }

    /**
     * Retrieves a specific test document by its ID.
     *
     * @param documentId The ID of the test document.
     * @return The test object if found.
     * @throws ExecutionException If an error occurs during Firestore query execution.
     * @throws InterruptedException If the thread is interrupted while waiting for the Firestore query to complete.
     * @throws ResourceNotFoundException If the test is not found.
     */
    @GetMapping("/tests/{documentId}")
    public Test getTest(@PathVariable String documentId) throws ExecutionException, InterruptedException {
        Test test = testService.getTest(documentId);
        if (test != null) {
            return test;
        } else {
            throw new ResourceNotFoundException("Test not found");
        }
    }

    /**
     * Retrieves all tests for a given user ID.
     *
     * @param userId The ID of the user whose tests are to be retrieved.
     * @return A response entity containing the list of tests or an error message.
     */
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

    /**
     * Retrieves the summed errors for a user's tests.
     *
     * @param userId The ID of the user whose tests' errors are to be summed.
     * @return A response entity containing the list of summed errors by mode.
     */
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

    /**
     * Retrieves processed game data for a specific test.
     *
     * @param testId The ID of the test.
     * @return A response entity containing the processed game data.
     */
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

    /**
     * Retrieves the omission errors for a specific test.
     *
     * @param testId The ID of the test.
     * @return A response entity containing the omission errors.
     */
    @GetMapping("/{testId}/omissions")
    public ResponseEntity<Map<String, Object>> getOmissionErrors(@PathVariable String testId) {
        try {
            Map<String, Object> response = testService.sumOmisions(testId);
            return ResponseEntity.ok(response);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }

    /**
     * Retrieves the commission errors for a specific test.
     *
     * @param testId The ID of the test.
     * @return A response entity containing the commission errors.
     */
    @GetMapping("/{testId}/commissions")
    public ResponseEntity<Map<String, Object>> getCommissionErrors(@PathVariable String testId) {
        try {
            Map<String, Object> response = testService.sumCommisions(testId);
            return ResponseEntity.ok(response);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }

    /**
     * Counts the total occurrences of stimuli in a specific test.
     *
     * @param testId The ID of the test.
     * @return A response entity containing the count of stimuli occurrences.
     */
    @GetMapping("/{testId}/stimuli-count")
    public ResponseEntity<Integer> countTotalStimuliOccurrences(@PathVariable String testId) {
        try {
            int totalOccurrences = testService.countTotalStimuliOccurrences(testId);
            return new ResponseEntity<>(totalOccurrences, HttpStatus.OK);
        } catch (ExecutionException | InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Counts the total occurrences of non-stimuli in a specific test.
     *
     * @param testId The ID of the test.
     * @return A response entity containing the count of non-stimuli occurrences.
     */
    @GetMapping("/{testId}/non-stimuli-count")
    public ResponseEntity<Integer> countNonStimuliOccurrences(@PathVariable String testId) {
        try {
            int nonStimuliCount = testService.countNonStimuliOccurrences(testId);
            return new ResponseEntity<>(nonStimuliCount, HttpStatus.OK);
        } catch (ExecutionException | InterruptedException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Aggregates errors by month and mode for a specific user's tests.
     *
     * @param userId The ID of the user whose tests' errors are to be aggregated.
     * @return A list of maps containing aggregated errors by month and mode.
     * @throws ExecutionException If an error occurs during Firestore query execution.
     * @throws InterruptedException If the thread is interrupted while waiting for the Firestore query to complete.
     */
    @GetMapping("/aggregate-errors-monthly/{userId}")
    public List<Map<String, Object>> aggregateErrorsByMonthAndMode(@PathVariable String userId) throws ExecutionException, InterruptedException {
        return testService.aggregateErrorsByMonthAndMode(userId);
    }

    /**
     * Aggregates errors by full date and mode for a specific user's tests.
     *
     * @param userId The ID of the user whose tests' errors are to be aggregated.
     * @return A list of maps containing aggregated errors by full date and mode.
     * @throws ExecutionException If an error occurs during Firestore query execution.
     * @throws InterruptedException If the thread is interrupted while waiting for the Firestore query to complete.
     */
    @GetMapping("/aggregate-errors-daily/{userId}")
    public List<Map<String, Object>> aggregateErrorsByFullDateAndMode(@PathVariable String userId) throws ExecutionException, InterruptedException {
        return testService.aggregateErrorsByFullDateAndMode(userId);
    }
}

