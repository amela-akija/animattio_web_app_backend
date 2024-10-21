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
                    totalCommissionErrors += ((Long) game.get("comissionErrors")).intValue();
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

    public Map<String, Object> sumErrorsForTest(String testId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference testDocRef = dbFirestore.collection("tests").document(testId);
        ApiFuture<DocumentSnapshot> future = testDocRef.get();
        DocumentSnapshot testDoc = future.get();
        int totalCommissionErrors = 0;
        int totalOmissionErrors = 0;
        if (testDoc.exists()) {
            List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");
            if (gamesInTest != null && !gamesInTest.isEmpty()) {

                for (Map<String, Object> game : gamesInTest) {
                    totalCommissionErrors += ((Long) game.get("comissionErrors")).intValue();
                    totalOmissionErrors += ((Long) game.get("omissionErrors")).intValue();
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found for the provided ID: " + testId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalCommissionErrors", totalCommissionErrors);
        response.put("totalOmissionErrors", totalOmissionErrors);
        return response;
    }

    public List<Map<String, Object>> processTappedImagesForTest(String testId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference testDocRef = dbFirestore.collection("tests").document(testId);
        ApiFuture<DocumentSnapshot> future = testDocRef.get();
        DocumentSnapshot testDoc = future.get();

        List<Map<String, Object>> allGamesData = new ArrayList<>();

        if (testDoc.exists()) {
            List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");

            if (gamesInTest != null && !gamesInTest.isEmpty()) {
                for (Map<String, Object> game : gamesInTest) {
                    List<Boolean> results = (List<Boolean>) game.get("result");
                    List<Long> reactionTimes = (List<Long>) game.get("reactionTimes");
                    List<Long> intervals = (List<Long>) game.get("intervals");

                    List<Map<String, Object>> tappedImagesWithTimes = processGame(results, reactionTimes, intervals);

                    Map<String, Object> gameData = new HashMap<>();
//                    gameData.put("userId", game.get("id"));
                    gameData.put("tappedImages", tappedImagesWithTimes);

                    allGamesData.add(gameData);
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found for the provided ID: " + testId);
        }

        return allGamesData;
    }

    private List<Map<String, Object>> processGame(List<Boolean> result, List<Long> reactionTimes, List<Long> intervals) {
        List<Map<String, Object>> tappedImagesWithTimes = new ArrayList<>();
        List<Integer> tappedIndices = new ArrayList<>();

        for (int i = 0; i < result.size(); i++) {
            if (result.get(i)) {
                tappedIndices.add(i);
            }
        }

        for (int tappedImageCount = 0; tappedImageCount < tappedIndices.size(); tappedImageCount++) {
            int index = tappedIndices.get(tappedImageCount);
            Long assignedInterval;

            if (index < 20) {
                assignedInterval = intervals.get(0);
            } else if (index < 40) {
                assignedInterval = intervals.get(1);
            } else {
                assignedInterval = intervals.get(2);
            }

            Map<String, Object> tappedImageData = new HashMap<>();
            tappedImageData.put("index", index);
            tappedImageData.put("reactionTime", reactionTimes.get(tappedImageCount));
            tappedImageData.put("interval", assignedInterval);

            tappedImagesWithTimes.add(tappedImageData);
        }

        return tappedImagesWithTimes;
    }









}
