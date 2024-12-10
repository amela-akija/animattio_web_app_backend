package com.animattio.animattio_web_app_backend.test;
import com.google.cloud.Timestamp;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Service class for managing test-related operations in the application.
 * This class provides methods to interact with Firestore for fetching,
 * aggregating, and processing test data.
 */
@Service
public class TestService {
    /**
     * Retrieves a single test document by its ID.
     *
     * @param documentId the ID of the test document to retrieve.
     * @return the {@link Test} object if found, or null if not found.
     * @throws ExecutionException   if the Firestore operation fails.
     * @throws InterruptedException if the Firestore operation is interrupted.
     */
    public Test getTest(String documentId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("tests").document(documentId);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot documentSnapshot = future.get();

        if (documentSnapshot.exists()) {
            Test test = documentSnapshot.toObject(Test.class);
            return test;
        }

        return null;
    }

    /**
     * Retrieves all tests associated with a specific user ID.
     *
     * @param userId the user ID for which to fetch tests.
     * @return a list of {@link Test} objects.
     * @throws ExecutionException   if the Firestore operation fails.
     * @throws InterruptedException if the Firestore operation is interrupted.
     */
    public List<Test> getTestsByUser(String userId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference testsCollection = dbFirestore.collection("tests");
        Query query = testsCollection.whereEqualTo("userId", userId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        List<Test> tests = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            Test test = document.toObject(Test.class);
            tests.add(test);
        }

        return tests;
    }

    /**
     * Aggregates errors for all tests of a user, including commission and omission errors,
     * along with test start and end dates and game modes.
     *
     * @param userId the user ID for which to aggregate test errors.
     * @return a list of maps representing aggregated error data.
     * @throws ExecutionException   if the Firestore operation fails.
     * @throws InterruptedException if the Firestore operation is interrupted.
     */
    public List<Map<String, Object>> sumErrorsForUserTests(String userId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = dbFirestore.collection("tests")
                .whereEqualTo("userId", userId)
                .get();

        List<QueryDocumentSnapshot> testDocuments = future.get().getDocuments();
        List<Map<String, Object>> testResults = new ArrayList<>();

        if (testDocuments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No tests found for the provided user ID: " + userId);
        }

        ZoneId zoneId = ZoneId.of("UTC+2");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM, yyyy"); // Sets the time zone to UTC+2 and defines a date
        // formatter to format dates in a "day month, year" format

        for (DocumentSnapshot testDoc : testDocuments) {
            Map<String, Object> testResult = new HashMap<>();

            int totalCommissionErrors = 0;
            int totalOmissionErrors = 0;
            Timestamp startDate = null;
            Timestamp endDate = null;
            String firstGameMode = null;

            List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");
            // Retrieves the list of games (gamesInTest) within the current test document

            if (gamesInTest != null && !gamesInTest.isEmpty()) {
                startDate = (Timestamp) gamesInTest.get(0).get("timestamp");
                endDate = (Timestamp) gamesInTest.get(0).get("timestamp");
                firstGameMode = (String) gamesInTest.get(0).get("mode"); // Parameters from the first game in list

                for (Map<String, Object> game : gamesInTest) {
                    totalCommissionErrors += ((Long) game.get("commissionErrors")).intValue();
                    totalOmissionErrors += ((Long) game.get("omissionErrors")).intValue(); // Summed errors for the test

                    Timestamp gameTimestamp = (Timestamp) game.get("timestamp"); // Finds the earliest and latest dates
                    if (gameTimestamp != null) {
                        if (gameTimestamp.compareTo(startDate) < 0) {
                            startDate = gameTimestamp;
                        }
                        if (gameTimestamp.compareTo(endDate) > 0) {
                            endDate = gameTimestamp;
                        }
                    }
                }
            }

            String formattedStartDate = startDate != null ?
                    ZonedDateTime.ofInstant(Instant.ofEpochSecond(startDate.getSeconds(), startDate.getNanos()), zoneId).format(formatter) : null;
            String formattedEndDate = endDate != null ?
                    ZonedDateTime.ofInstant(Instant.ofEpochSecond(endDate.getSeconds(), endDate.getNanos()), zoneId).format(formatter) : null;
            // Converts the Firestore Timestamp to a readable date format in UTC+2

            testResult.put("testId", testDoc.getId());
            testResult.put("commissions", totalCommissionErrors);
            testResult.put("omissions", totalOmissionErrors);
            testResult.put("startDate", formattedStartDate);
            testResult.put("endDate", formattedEndDate);
            testResult.put("gameMode", firstGameMode); // Stores the aggregated data for this test into the map
            testResults.add(testResult);
        }

        return testResults; // Returns a list of user tests with summed errors
    }

    /**
     * Sums up the total number of commission errors for a specific test.
     *
     * @param testId the ID of the test to process.
     * @return a map containing the total commission errors for the test.
     * @throws ExecutionException   if the Firestore operation fails.
     * @throws InterruptedException if the Firestore operation is interrupted.
     */
    public Map<String, Object> sumCommisions(String testId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference testDocRef = dbFirestore.collection("tests").document(testId);
        ApiFuture<DocumentSnapshot> future = testDocRef.get();
        DocumentSnapshot testDoc = future.get();
        int totalCommissionErrors = 0;

        if (testDoc.exists()) {
            List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");
            if (gamesInTest != null && !gamesInTest.isEmpty()) {
                // Iterates through each game in the list, retrieves the value for the "commissionErrors",
                // and adds it to the total amount of commission errors

                for (Map<String, Object> game : gamesInTest) {
                    totalCommissionErrors += ((Long) game.get("commissionErrors")).intValue();

                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found for the provided ID: " + testId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalCommissionErrors", totalCommissionErrors);
        return response;
    }

    /**
     * Sums up the total number of omission errors for a specific test.
     *
     * @param testId the ID of the test to process.
     * @return a map containing the total omission errors for the test.
     * @throws ExecutionException   if the Firestore operation fails.
     * @throws InterruptedException if the Firestore operation is interrupted.
     */
    public Map<String, Object> sumOmisions(String testId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference testDocRef = dbFirestore.collection("tests").document(testId);
        ApiFuture<DocumentSnapshot> future = testDocRef.get();
        DocumentSnapshot testDoc = future.get();
        int totalOmissionErrors = 0;
        if (testDoc.exists()) {
            List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");
            if (gamesInTest != null && !gamesInTest.isEmpty()) {

                for (Map<String, Object> game : gamesInTest) {
                    totalOmissionErrors += ((Long) game.get("omissionErrors")).intValue();
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found for the provided ID: " + testId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalOmissionErrors", totalOmissionErrors);
        return response;
    }

    /**
     * Processes tapped images and groups reaction times by intervals for a specific test.
     *
     * @param testId the ID of the test to process.
     * @return a map where keys are intervals ("1250", "2250", "4250") and values are lists of reaction times.
     * @throws ExecutionException   if the Firestore operation fails.
     * @throws InterruptedException if the Firestore operation is interrupted.
     */
    public Map<String, List<Long>> processTappedImagesForTest(String testId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference testDocRef = dbFirestore.collection("tests").document(testId);
        ApiFuture<DocumentSnapshot> future = testDocRef.get();
        DocumentSnapshot testDoc = future.get();

        Map<String, List<Long>> groupedData = new HashMap<>();
        groupedData.put("1250", new ArrayList<>());
        groupedData.put("2250", new ArrayList<>());
        groupedData.put("4250", new ArrayList<>());
        // Creates a map where keys represent interval + 250 ms categories (1250, 2250, 4250) and values
        // are lists to store corresponding reaction times

        if (testDoc.exists()) {
            List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");

            if (gamesInTest != null && !gamesInTest.isEmpty()) {
                for (Map<String, Object> game : gamesInTest) {
                    List<Boolean> results = (List<Boolean>) game.get("result");
                    List<Long> reactionTimes = (List<Long>) game.get("reactionTimes");
                    List<Long> intervals = (List<Long>) game.get("intervals");

                    processGame(results, reactionTimes, intervals, groupedData);
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found for the provided ID: " + testId);
        }

        return groupedData; // Grouped reaction times
    }

    /**
     * Helper method to process a game's tapped images and group reaction times by intervals.
     *
     * @param result        the list of results indicating whether each image was tapped.
     * @param reactionTimes the list of reaction times for tapped images.
     * @param intervals     the intervals used for grouping.
     * @param groupedData   the map to store grouped reaction times.
     */
    private void processGame(List<Boolean> result, List<Long> reactionTimes, List<Long> intervals, Map<String, List<Long>> groupedData) {
        List<Integer> tappedIndices = new ArrayList<>();

        for (int i = 0; i < result.size(); i++) {
            if (result.get(i)) {
                tappedIndices.add(i);
            }
        } // Adds the indices of all elements where result.get(i) is true to the tappedIndices list

        for (int tappedImageCount = 0; tappedImageCount < tappedIndices.size(); tappedImageCount++) {
            int index = tappedIndices.get(tappedImageCount);

            if (tappedImageCount >= reactionTimes.size()) {
                break;
            }

            Long reactionTime = reactionTimes.get(tappedImageCount);
            // Retrieves the reaction time corresponding to the current tapped image

            if (intervals.size() < 3) {
                break; // All three intervals included
            }

            Long assignedInterval;
            if (index < 20) {
                assignedInterval = intervals.get(0);
            } else if (index < 40) {
                assignedInterval = intervals.get(1);
            } else {
                assignedInterval = intervals.get(2);
            } // Assigns interval to the tapped image

            groupedData.get(String.valueOf(assignedInterval)).add(reactionTime);
            // Adds the reaction time to the interval category in groupedData
        }
    }

    /**
     * Counts the total occurrences of the target stimulus in a test.
     *
     * @param testId the ID of the test to process.
     * @return the total number of occurrences of the target stimulus.
     * @throws ExecutionException   if the Firestore operation fails.
     * @throws InterruptedException if the Firestore operation is interrupted.
     */
    public int countTotalStimuliOccurrences(String testId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference testDocRef = dbFirestore.collection("tests").document(testId);
        ApiFuture<DocumentSnapshot> future = testDocRef.get();
        DocumentSnapshot testDoc = future.get();

        if (!testDoc.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found for the provided ID: " + testId);
        }

        List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");

        if (gamesInTest == null || gamesInTest.isEmpty()) {
            return 0; // Returns 0 if the list of game is null
        }

        String firstGameMode = (String) gamesInTest.get(0).get("mode"); // Retrieves the mode of the first game to determine how to count stimuli
        int totalCount = 0;

        if ("mode1".equals(firstGameMode)) {
            for (Map<String, Object> game : gamesInTest) {
                String stimuli = (String) game.get("stimuli");
                List<String> shownImages = (List<String>) game.get("shownImages");

                if (shownImages != null && stimuli != null) {
                    int count = Collections.frequency(shownImages, stimuli); // Counts how many times the stimuli appears in the shownImages
                    totalCount += count;
                }
            }
        } else if ("mode2".equals(firstGameMode)) {
            for (Map<String, Object> game : gamesInTest) {
                List<String> shownImages = (List<String>) game.get("shownImages");
                String stimuli = (String) game.get("stimuli");

                if (shownImages != null) {
                    for (String image : shownImages) {
                        if (!image.equals(stimuli)) { // Counts all shownImages that are not the stimuli
                            totalCount++;
                        }
                    }
                }
            }
        }

        return totalCount;
    }

    /**
     * Counts the number of non-stimuli occurrences in a test based on its ID.
     *
     * @param testId The ID of the test document in Firestore.
     * @return The total count of non-stimuli occurrences across all games in the test.
     * @throws ExecutionException If an error occurs during Firestore query execution.
     * @throws InterruptedException If the thread is interrupted while waiting for the Firestore query to complete.
     * @throws ResponseStatusException If the test is not found or contains no games.
     */
    public int countNonStimuliOccurrences(String testId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference testDocRef = dbFirestore.collection("tests").document(testId);
        ApiFuture<DocumentSnapshot> future = testDocRef.get();
        DocumentSnapshot testDoc = future.get();

        if (!testDoc.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found for the provided ID: " + testId);
        }

        List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");

        if (gamesInTest == null || gamesInTest.isEmpty()) {
            return 0;
        }

        String firstGameMode = (String) gamesInTest.get(0).get("mode");
        int totalCount = 0;

        if ("mode1".equals(firstGameMode)) {
            for (Map<String, Object> game : gamesInTest) {
                List<String> shownImages = (List<String>) game.get("shownImages");
                String stimuli = (String) game.get("stimuli");

                if (shownImages != null) {
                    for (String image : shownImages) {
                        if (!image.equals(stimuli)) {
                            totalCount++;
                        }
                    }
                }
            }
        } else if ("mode2".equals(firstGameMode)) {
            for (Map<String, Object> game : gamesInTest) {
                String stimuli = (String) game.get("stimuli");
                List<String> shownImages = (List<String>) game.get("shownImages");

                if (shownImages != null && stimuli != null) {
                    int count = Collections.frequency(shownImages, stimuli);
                    totalCount += count;
                }
            }
        }

        return totalCount;
    }

    /**
     * Aggregates errors (omission and commission) by month and mode for a user's tests.
     *
     * @param userId The ID of the user whose tests are to be aggregated.
     * @return A list of maps containing aggregated data for each month and mode, including
     *         the number of commission errors, omission errors, target stimuli, and non-target stimuli.
     * @throws ExecutionException If an error occurs during Firestore query execution.
     * @throws InterruptedException If the thread is interrupted while waiting for the Firestore query to complete.
     * @throws ResponseStatusException If no tests are found for the specified user ID.
     */
    public List<Map<String, Object>> aggregateErrorsByMonthAndMode(String userId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("tests")
                .whereEqualTo("userId", userId)
                .get();

        List<QueryDocumentSnapshot> testDocuments = future.get().getDocuments();
        Map<String, Map<String, Map<String, Object>>> monthlyAggregatedResults = new HashMap<>(); // Map for results

        if (testDocuments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No tests found for the provided user ID: " + userId);
        }

        int numberOfTests = testDocuments.size();
        ZoneId zoneId = ZoneId.of("UTC+2");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM.yyyy");

        for (DocumentSnapshot testDoc : testDocuments) {
            List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");

            if (gamesInTest != null && !gamesInTest.isEmpty()) {
                Map<String, Object> lastGame = gamesInTest.get(gamesInTest.size() - 1);
                String testMode = (String) lastGame.get("mode"); // Retrieves mode of the test from last game in gamesInTest

                int commissionErrors = 0;
                int omissionErrors = 0;

                for (Map<String, Object> game : gamesInTest) {
                    commissionErrors += ((Long) game.get("commissionErrors")).intValue(); //Summed errors from all the games in test
                    omissionErrors += ((Long) game.get("omissionErrors")).intValue();
                }

                Timestamp gameTimestamp = (Timestamp) lastGame.get("timestamp");
                if (gameTimestamp != null) {
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(gameTimestamp.getSeconds(), gameTimestamp.getNanos()), zoneId); // readable date from timestamp
                    String month = zonedDateTime.format(monthFormatter); // Retrieves month to aggregate the results

                    Map<String, Map<String, Object>> modeResults = monthlyAggregatedResults.getOrDefault(month, new HashMap<>()); // Creates new map for each month
                    Map<String, Object> monthlyResult = modeResults.getOrDefault(testMode, new HashMap<>()); // Creates new map for mode

                    monthlyResult.put("month", month);
                    monthlyResult.put("mode", testMode);
                    monthlyResult.put("commissions", (int) monthlyResult.getOrDefault("commissions", 0) + commissionErrors); // Adds all commissions in month
                    monthlyResult.put("omissions", (int) monthlyResult.getOrDefault("omissions", 0) + omissionErrors); // Adds all omissions in month

                    if ("mode1".equals(testMode)) {
                        monthlyResult.put("targetStimuli", 36 * numberOfTests);
                        monthlyResult.put("nonTargetStimuli", 324 * numberOfTests);
                    } else if ("mode2".equals(testMode)) {
                        monthlyResult.put("targetStimuli", 324 * numberOfTests);
                        monthlyResult.put("nonTargetStimuli", 36 * numberOfTests);
                    }

                    modeResults.put(testMode, monthlyResult);
                    monthlyAggregatedResults.put(month, modeResults);
                }
            }
        }

        List<Map<String, Object>> aggregatedResults = new ArrayList<>();
        for (Map<String, Map<String, Object>> modeResults : monthlyAggregatedResults.values()) {
            aggregatedResults.addAll(modeResults.values()); // returns the inner maps that store errors for each month for each mode
        }

        return aggregatedResults;
    }


    /**
     * Aggregates errors (omission and commission) by full date and mode for a user's tests.
     *
     * @param userId The ID of the user whose tests are to be aggregated.
     * @return A list of maps containing aggregated data for each date and mode, including
     *         the number of commission errors, omission errors, target stimuli, and non-target stimuli.
     *         The results are sorted by date.
     * @throws ExecutionException If an error occurs during Firestore query execution.
     * @throws InterruptedException If the thread is interrupted while waiting for the Firestore query to complete.
     * @throws ResponseStatusException If no tests are found for the specified user ID.
     */
    public List<Map<String, Object>> aggregateErrorsByFullDateAndMode(String userId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("tests")
                .whereEqualTo("userId", userId)
                .get();

        List<QueryDocumentSnapshot> testDocuments = future.get().getDocuments();
        Map<String, Map<String, Object>> dailyAggregatedResults = new HashMap<>(); // date as key

        if (testDocuments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No tests found for the provided user ID: " + userId);
        }

        ZoneId zoneId = ZoneId.of("UTC+2");
        DateTimeFormatter fullDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

        for (DocumentSnapshot testDoc : testDocuments) {
            List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");

            if (gamesInTest != null && !gamesInTest.isEmpty()) {
                Map<String, Object> lastGame = gamesInTest.get(gamesInTest.size() - 1);
                String testMode = (String) lastGame.get("mode");

                int commissionErrors = 0;
                int omissionErrors = 0;

                for (Map<String, Object> game : gamesInTest) {
                    commissionErrors += ((Long) game.get("commissionErrors")).intValue();
                    omissionErrors += ((Long) game.get("omissionErrors")).intValue();
                } // Sums up commissionErrors and omissionErrors for all games in the test

                Timestamp gameTimestamp = (Timestamp) lastGame.get("timestamp");
                if (gameTimestamp != null) {
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(
                            Instant.ofEpochSecond(gameTimestamp.getSeconds(), gameTimestamp.getNanos()), zoneId);
                    String fullDate = zonedDateTime.format(fullDateFormatter); // readable date from timestamp

                    Map<String, Object> dailyResult = dailyAggregatedResults.getOrDefault(fullDate, new HashMap<>());

                    dailyResult.put("date", fullDate);
                    dailyResult.put("mode", testMode);
                    dailyResult.put("commissions", (int) dailyResult.getOrDefault("commissions", 0) + commissionErrors);
                    dailyResult.put("omissions", (int) dailyResult.getOrDefault("omissions", 0) + omissionErrors);

                    int testCount = (int) dailyResult.getOrDefault("testCount", 0) + 1; // Number of tests with a certain date
                    dailyResult.put("testCount", testCount);

                    if ("mode1".equals(testMode)) {
                        dailyResult.put("targetStimuli", 36 * testCount);
                        dailyResult.put("nonTargetStimuli", 324 * testCount);
                    } else if ("mode2".equals(testMode)) {
                        dailyResult.put("targetStimuli", 324 * testCount);
                        dailyResult.put("nonTargetStimuli", 36 * testCount);
                    }

                    dailyAggregatedResults.put(fullDate, dailyResult);
                }
            }
        }

        List<Map<String, Object>> aggregatedResults = new ArrayList<>(dailyAggregatedResults.values());
        aggregatedResults.sort(Comparator.comparing(result -> result.get("date").toString())); // Sort by date

        return aggregatedResults;
    }




}