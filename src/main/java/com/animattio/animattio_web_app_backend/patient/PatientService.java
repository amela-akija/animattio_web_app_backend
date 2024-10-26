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

    public void updatePatient(String documentId, Patient updatedPatient) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        String newDocumentId = updatedPatient.getPatientUsername();
        if (newDocumentId == null || newDocumentId.isEmpty()) {
            throw new IllegalArgumentException("Patient username cannot be null or empty");
        }

        DocumentReference oldDocumentReference = dbFirestore.collection("patients").document(documentId);
        ApiFuture<DocumentSnapshot> future = oldDocumentReference.get();
        DocumentSnapshot documentSnapshot = future.get();

        if (documentSnapshot.exists()) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("patientUsername", newDocumentId);
            updates.put("doctorUsername", documentSnapshot.getString("doctorUsername"));
            updates.put("gender", updatedPatient.getGender());
            updates.put("age", updatedPatient.getAge());
            updates.put("type", updatedPatient.getType());

            DocumentReference newDocumentReference = dbFirestore.collection("patients").document(newDocumentId);
            ApiFuture<WriteResult> writeResult = newDocumentReference.set(updates);
            writeResult.get();

            ApiFuture<QuerySnapshot> usersQuery = dbFirestore.collection("users")
                    .whereEqualTo("username", documentId).get();

            for (DocumentSnapshot userDoc : usersQuery.get().getDocuments()) {
                DocumentReference userDocumentReference = dbFirestore.collection("users").document(userDoc.getId());
                userDocumentReference.update("username", newDocumentId).get();
            }

            if (!documentId.equals(newDocumentId)) {
                ApiFuture<WriteResult> deleteResult = oldDocumentReference.delete();
                deleteResult.get();
            }
        } else {
            throw new RuntimeException("Patient document not found");
        }
    }






    public String getPatientDocumentIdByUsername(String username) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference usersCollection = dbFirestore.collection("users");
        Query query = usersCollection.whereEqualTo("username", username).limit(1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

        if (!documents.isEmpty()) {
            return documents.get(0).getId();
        } else {
            return null;
        }
    }

    public Long getPatientAge(String documentId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("patients").document(documentId);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot documentSnapshot = future.get();

        if (documentSnapshot.exists()) {
            return documentSnapshot.getLong("age");
        }
        return null;
    }

    public String getPatientGender(String documentId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("patients").document(documentId);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot documentSnapshot = future.get();

        if (documentSnapshot.exists()) {
            return documentSnapshot.getString("gender");
        }
        return null;
    }
    public String getPatientType(String documentId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("patients").document(documentId);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot documentSnapshot = future.get();

        if (documentSnapshot.exists()) {
            return documentSnapshot.getString("type");
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
    public List<Patient> getPatientsByAge(String doctorId, int age) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference patientsCollection = dbFirestore.collection("patients");
        Query query = patientsCollection.whereEqualTo("doctorUsername", doctorId).whereEqualTo("age",age);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        List<Patient> patients = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            Patient patient = document.toObject(Patient.class);
            patients.add(patient);
        }
        return patients;
    }

    public List<Patient> getPatientsByGender(String doctorId, String gender) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference patientsCollection = dbFirestore.collection("patients");
        Query query = patientsCollection.whereEqualTo("doctorUsername", doctorId).whereEqualTo("gender",gender);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        List<Patient> patients = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            Patient patient = document.toObject(Patient.class);
            patients.add(patient);
        }
        return patients;
    }

    public List<Patient> getPatientsByUsername(String doctorId, String username) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference patientsCollection = dbFirestore.collection("patients");
        Query query = patientsCollection.whereEqualTo("doctorUsername", doctorId).whereEqualTo("patientUsername",username);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        List<Patient> patients = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            Patient patient = document.toObject(Patient.class);
            patients.add(patient);
        }
        return patients;
    }
    public List<Patient> getPatientsByType(String doctorId, String type) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference patientsCollection = dbFirestore.collection("patients");
        Query query = patientsCollection.whereEqualTo("doctorUsername", doctorId).whereEqualTo("type",type);
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

    public boolean doesPatientExist(String username) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<DocumentSnapshot> future = dbFirestore.collection("patients").document(username).get();
        DocumentSnapshot documentSnapshot = future.get();
        return documentSnapshot.exists();
    }

}
