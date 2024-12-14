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
@Service
public class TestService {
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM, yyyy");

        for (DocumentSnapshot testDoc : testDocuments) {
            Map<String, Object> testResult = new HashMap<>();

            int totalCommissionErrors = 0;
            int totalOmissionErrors = 0;
            Timestamp startDate = null;
            Timestamp endDate = null;
            String firstGameMode = null;

            List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");

            if (gamesInTest != null && !gamesInTest.isEmpty()) {
                startDate = (Timestamp) gamesInTest.get(0).get("timestamp");
                endDate = (Timestamp) gamesInTest.get(0).get("timestamp");
                firstGameMode = (String) gamesInTest.get(0).get("mode");

                for (Map<String, Object> game : gamesInTest) {
                    totalCommissionErrors += ((Long) game.get("commissionErrors")).intValue();
                    totalOmissionErrors += ((Long) game.get("omissionErrors")).intValue();

                    Timestamp gameTimestamp = (Timestamp) game.get("timestamp");
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

            testResult.put("testId", testDoc.getId());
            testResult.put("commissions", totalCommissionErrors);
            testResult.put("omissions", totalOmissionErrors);
            testResult.put("startDate", formattedStartDate);
            testResult.put("endDate", formattedEndDate);
            testResult.put("gameMode", firstGameMode);

            testResults.add(testResult);
        }

        return testResults;
    }

    public Map<String, Object> sumCommisions(String testId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference testDocRef = dbFirestore.collection("tests").document(testId);
        ApiFuture<DocumentSnapshot> future = testDocRef.get();
        DocumentSnapshot testDoc = future.get();
        int totalCommissionErrors = 0;
//        int totalOmissionErrors = 0;
//        String mode = "";
        if (testDoc.exists()) {
            List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");
            if (gamesInTest != null && !gamesInTest.isEmpty()) {

                for (Map<String, Object> game : gamesInTest) {
                    totalCommissionErrors += ((Long) game.get("commissionErrors")).intValue();
//                    totalOmissionErrors += ((Long) game.get("omissionErrors")).intValue();
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found for the provided ID: " + testId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalCommissionErrors", totalCommissionErrors);
//        response.put("totalOmissionErrors", totalOmissionErrors);
        return response;
    }

    public Map<String, Object> sumOmisions(String testId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference testDocRef = dbFirestore.collection("tests").document(testId);
        ApiFuture<DocumentSnapshot> future = testDocRef.get();
        DocumentSnapshot testDoc = future.get();
//        int totalCommissionErrors = 0;
        int totalOmissionErrors = 0;
//        String mode = "";
        if (testDoc.exists()) {
            List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");
            if (gamesInTest != null && !gamesInTest.isEmpty()) {

                for (Map<String, Object> game : gamesInTest) {
//                    totalCommissionErrors += ((Long) game.get("comissionErrors")).intValue();
                    totalOmissionErrors += ((Long) game.get("omissionErrors")).intValue();
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found for the provided ID: " + testId);
        }

        Map<String, Object> response = new HashMap<>();
//        response.put("totalCommissionErrors", totalCommissionErrors);
        response.put("totalOmissionErrors", totalOmissionErrors);
        return response;
    }

    public Map<String, List<Long>> processTappedImagesForTest(String testId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference testDocRef = dbFirestore.collection("tests").document(testId);
        ApiFuture<DocumentSnapshot> future = testDocRef.get();
        DocumentSnapshot testDoc = future.get();

        Map<String, List<Long>> groupedData = new HashMap<>();
        groupedData.put("1250", new ArrayList<>());
        groupedData.put("2250", new ArrayList<>());
        groupedData.put("4250", new ArrayList<>());

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

        return groupedData;
    }

    private void processGame(List<Boolean> result, List<Long> reactionTimes, List<Long> intervals, Map<String, List<Long>> groupedData) {
        List<Integer> tappedIndices = new ArrayList<>();

        for (int i = 0; i < result.size(); i++) {
            if (result.get(i)) {
                tappedIndices.add(i);
            }
        }

        for (int tappedImageCount = 0; tappedImageCount < tappedIndices.size(); tappedImageCount++) {
            int index = tappedIndices.get(tappedImageCount);

            if (tappedImageCount >= reactionTimes.size()) {
                break;
            }

            Long reactionTime = reactionTimes.get(tappedImageCount);

            if (intervals.size() < 3) {
                break;
            }

            Long assignedInterval;
            if (index < 20) {
                assignedInterval = intervals.get(0);
            } else if (index < 40) {
                assignedInterval = intervals.get(1);
            } else {
                assignedInterval = intervals.get(2);
            }

            groupedData.get(String.valueOf(assignedInterval)).add(reactionTime);
        }
    }
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
            return 0;
        }

        String firstGameMode = (String) gamesInTest.get(0).get("mode");
        int totalCount = 0;

        if ("mode1".equals(firstGameMode)) {
            for (Map<String, Object> game : gamesInTest) {
                String stimuli = (String) game.get("stimuli");
                List<String> shownImages = (List<String>) game.get("shownImages");

                if (shownImages != null && stimuli != null) {
                    int count = Collections.frequency(shownImages, stimuli);
                    totalCount += count;
                }
            }
        } else if ("mode2".equals(firstGameMode)) {
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
        }

        return totalCount;
    }


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
    public List<Map<String, Object>> aggregateErrorsByMonthAndMode(String userId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("tests")
                .whereEqualTo("userId", userId)
                .get();

        List<QueryDocumentSnapshot> testDocuments = future.get().getDocuments();
        Map<String, Map<String, Map<String, Object>>> monthlyAggregatedResults = new HashMap<>();

        if (testDocuments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No tests found for the provided user ID: " + userId);
        }

        ZoneId zoneId = ZoneId.of("UTC+2");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM.yyyy");

        Map<String, Integer> modeTestCounts = new HashMap<>();

        for (DocumentSnapshot testDoc : testDocuments) {
            List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");
            if (gamesInTest != null && !gamesInTest.isEmpty()) {
                Map<String, Object> lastGame = gamesInTest.get(gamesInTest.size() - 1);
                String testMode = (String) lastGame.get("mode");

                modeTestCounts.put(testMode, modeTestCounts.getOrDefault(testMode, 0) + 1);
            }
        }

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
                }

                Timestamp gameTimestamp = (Timestamp) lastGame.get("timestamp");
                if (gameTimestamp != null) {
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(gameTimestamp.getSeconds(), gameTimestamp.getNanos()), zoneId);
                    String month = zonedDateTime.format(monthFormatter);

                    Map<String, Map<String, Object>> modeResults = monthlyAggregatedResults.getOrDefault(month, new HashMap<>());
                    Map<String, Object> monthlyResult = modeResults.getOrDefault(testMode, new HashMap<>());

                    monthlyResult.put("month", month);
                    monthlyResult.put("mode", testMode);
                    monthlyResult.put("commissions", (int) monthlyResult.getOrDefault("commissions", 0) + commissionErrors);
                    monthlyResult.put("omissions", (int) monthlyResult.getOrDefault("omissions", 0) + omissionErrors);

                    int testCountForMode = modeTestCounts.getOrDefault(testMode, 0);

                    if ("mode1".equals(testMode)) {
                        monthlyResult.put("targetStimuli", 36 * testCountForMode);
                        monthlyResult.put("nonTargetStimuli", 324 * testCountForMode);
                    } else if ("mode2".equals(testMode)) {
                        monthlyResult.put("targetStimuli", 324 * testCountForMode);
                        monthlyResult.put("nonTargetStimuli", 36 * testCountForMode);
                    }

                    modeResults.put(testMode, monthlyResult);
                    monthlyAggregatedResults.put(month, modeResults);
                }
            }
        }

        List<Map<String, Object>> aggregatedResults = new ArrayList<>();
        for (Map<String, Map<String, Object>> modeResults : monthlyAggregatedResults.values()) {
            aggregatedResults.addAll(modeResults.values());
        }

        return aggregatedResults;
    }




    public List<Map<String, Object>> aggregateErrorsByFullDateAndMode(String userId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("tests")
                .whereEqualTo("userId", userId)
                .get();

        List<QueryDocumentSnapshot> testDocuments = future.get().getDocuments();
        Map<String, Map<String, Map<String, Object>>> dailyAggregatedResults = new HashMap<>();

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
                }

                Timestamp gameTimestamp = (Timestamp) lastGame.get("timestamp");
                if (gameTimestamp != null) {
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(
                            Instant.ofEpochSecond(gameTimestamp.getSeconds(), gameTimestamp.getNanos()), zoneId);
                    String fullDate = zonedDateTime.format(fullDateFormatter);

                    Map<String, Map<String, Object>> modeResults = dailyAggregatedResults.getOrDefault(fullDate, new HashMap<>());
                    Map<String, Object> dailyResult = modeResults.getOrDefault(testMode, new HashMap<>());

                    dailyResult.put("date", fullDate);
                    dailyResult.put("mode", testMode);
                    dailyResult.put("commissions", (int) dailyResult.getOrDefault("commissions", 0) + commissionErrors);
                    dailyResult.put("omissions", (int) dailyResult.getOrDefault("omissions", 0) + omissionErrors);

                    int testCount = (int) dailyResult.getOrDefault("testCount", 0) + 1;
                    dailyResult.put("testCount", testCount);

                    if ("mode1".equals(testMode)) {
                        dailyResult.put("targetStimuli", 36 * testCount);
                        dailyResult.put("nonTargetStimuli", 324 * testCount);
                    } else if ("mode2".equals(testMode)) {
                        dailyResult.put("targetStimuli", 324 * testCount);
                        dailyResult.put("nonTargetStimuli", 36 * testCount);
                    }

                    modeResults.put(testMode, dailyResult);
                    dailyAggregatedResults.put(fullDate, modeResults);
                }
            }
        }

        List<Map<String, Object>> aggregatedResults = new ArrayList<>();
        for (Map<String, Map<String, Object>> modeResults : dailyAggregatedResults.values()) {
            aggregatedResults.addAll(modeResults.values());
        }

        aggregatedResults.sort(Comparator.comparing(result -> result.get("date").toString()));

        return aggregatedResults;
    }





}