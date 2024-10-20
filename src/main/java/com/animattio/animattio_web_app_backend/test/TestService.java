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


    public Map<String, Object> sumErrorsForTest(String testId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference testDocRef = dbFirestore.collection("tests").document(testId);
        ApiFuture<DocumentSnapshot> future = testDocRef.get();
        DocumentSnapshot testDoc = future.get();

        int totalCommissionErrors = 0;
        int totalOmissionErrors = 0;
        Timestamp startDate = null;
        Timestamp endDate = null;

        if (testDoc.exists()) {
            List<Map<String, Object>> gamesInTest = (List<Map<String, Object>>) testDoc.get("gamesInTest");

            if (gamesInTest != null && !gamesInTest.isEmpty()) {
                startDate = (Timestamp) gamesInTest.get(0).get("timestamp");
                endDate = (Timestamp) gamesInTest.get(0).get("timestamp");

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
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found for the provided ID: " + testId);
        }

        ZoneId zoneId = ZoneId.of("UTC+2");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM, yyyy");

        String formattedStartDate = startDate != null ?
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(startDate.getSeconds(), startDate.getNanos()), zoneId).format(formatter) : null;
        String formattedEndDate = endDate != null ?
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(endDate.getSeconds(), endDate.getNanos()), zoneId).format(formatter) : null;

        Map<String, Object> response = new HashMap<>();
        response.put("totalCommissionErrors", totalCommissionErrors);
        response.put("totalOmissionErrors", totalOmissionErrors);
        response.put("startDate", formattedStartDate);
        response.put("endDate", formattedEndDate);

        return response;
    }




}
