package com.animattio.animattio_web_app_backend.test;

import com.animattio.animattio_web_app_backend.game.Game;
import com.animattio.animattio_web_app_backend.patient.Patient;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

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
            System.out.println("Test ID: " + test.getId());
            System.out.println("Games: " + test.getGames());
            return test;
        }

        return null; // Return null if the document does not exist
    }
}
