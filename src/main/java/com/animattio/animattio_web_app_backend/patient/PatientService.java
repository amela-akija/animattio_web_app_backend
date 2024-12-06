package com.animattio.animattio_web_app_backend.patient;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
    public List<Patient> getPatientsByAge(String doctorId, int minAge, int maxAge) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference patientsCollection = dbFirestore.collection("patients");
        Query query = patientsCollection
                .whereEqualTo("doctorUsername", doctorId)
                .whereGreaterThanOrEqualTo("age", minAge)
                .whereLessThanOrEqualTo("age", maxAge);

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

    public List<Patient> getPatientsByUsername(String doctorId, String partialUsername) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference patientsCollection = dbFirestore.collection("patients");

        List<Patient> patients = new ArrayList<>();

        if (partialUsername != null && !partialUsername.trim().isEmpty()) {
            String lowercaseFirst = partialUsername.substring(0, 1).toLowerCase() + partialUsername.substring(1);
            String uppercaseFirst = partialUsername.substring(0, 1).toUpperCase() + partialUsername.substring(1);

//            Query query1 = patientsCollection.whereEqualTo("doctorUsername", doctorId)
//                    .orderBy("patientUsername")
//                    .startAt(partialUsername)
//                    .endAt(partialUsername + "\uf8ff");

            Query query2 = patientsCollection.whereEqualTo("doctorUsername", doctorId)
                    .orderBy("patientUsername")
                    .startAt(lowercaseFirst)
                    .endAt(lowercaseFirst + "\uf8ff");

            Query query3 = patientsCollection.whereEqualTo("doctorUsername", doctorId)
                    .orderBy("patientUsername")
                    .startAt(uppercaseFirst)
                    .endAt(uppercaseFirst + "\uf8ff");

//            patients.addAll(fetchPatientsFromQuery(query1));
            patients.addAll(fetchPatientsFromQuery(query2));
            patients.addAll(fetchPatientsFromQuery(query3));

            patients = patients.stream().distinct().collect(Collectors.toList());
        } else {
            Query query = patientsCollection.whereEqualTo("doctorUsername", doctorId);
            patients = fetchPatientsFromQuery(query);
        }

        return patients;
    }

    private List<Patient> fetchPatientsFromQuery(Query query) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<Patient> patients = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
            patients.add(document.toObject(Patient.class));
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

    public String deletePatientByUsername(String username) throws ExecutionException, InterruptedException, FirebaseAuthException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> queryFuture = dbFirestore.collection("patients")
                .whereEqualTo("patientUsername", username)
                .get();

        QuerySnapshot querySnapshot = queryFuture.get();

        if (querySnapshot.isEmpty()) {
            return "Patient with username '" + username + "' not found.";
        }

        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
//            String uid = document.getId();

            document.getReference().delete();

//            FirebaseAuth.getInstance().deleteUser(uid);
        }

        return "Patient with username '" + username + "' deleted successfully.";
    }

}
