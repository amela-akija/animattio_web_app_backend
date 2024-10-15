package com.animattio.animattio_web_app_backend.patient;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class PatientService {

    public ResponseEntity<?> createPatient(Patient patient) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        try {
            ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("patients").document(patient.getPatientUsername()).set(patient);
            WriteResult writeResult = collectionsApiFuture.get();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Patient created successfully");
            response.put("updatedTime", writeResult.getUpdateTime());

            return ResponseEntity.ok(response);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    public Patient getPatient(String documentId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("patients").document(documentId);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot documentSnapshot = future.get();
        if (documentSnapshot.exists()) {
            return documentSnapshot.toObject(Patient.class);
        }
        return null;
    }


    public List<Patient> getAllPatients(String doctorId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference patientsCollection = dbFirestore.collection("patients");
        Query query = patientsCollection.whereEqualTo("doctorUsername", doctorId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        List<Patient> patients = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            Patient patient = document.toObject(Patient.class);
            patients.add(patient);
        }

        return patients;
    }

    public ResponseEntity<?> deletePatient(String documentId) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        try {
            ApiFuture<WriteResult> writeResult = dbFirestore.collection("patients").document(documentId).delete();
            return ResponseEntity.ok("Successfully deleted: " + documentId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
