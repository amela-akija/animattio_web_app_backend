package com.animattio.animattio_web_app_backend.patient;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Service class for managing patient-related operations.
 * Provides methods to create, retrieve, update, delete, and query patient data
 * stored in a Firestore database.
 */
@Service
public class PatientService {

    /**
     * Creates a new patient in the Firestore database.
     *
     * @param patient The patient object to create.
     * @return A ResponseEntity with the creation status and updated timestamp.
     */
    public ResponseEntity<?> createPatient(Patient patient) {
        Firestore dbFirestore = FirestoreClient.getFirestore(); // retrieves the Firestore database instance
        try {
            ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("patients").document(patient.getPatientUsername()).set(patient);
            // Creates or references a document in the patients collection and writes the patient object to the specified document in Firestore
            WriteResult writeResult = collectionsApiFuture.get(); // get() method waits for the Firestore write operation to complete
            // it returns a WriteResult object, which contains details about the write operation (update time)

            Map<String, Object> response = new HashMap<>(); // Map object created to structure the HTTP response
            response.put("message", "Patient created successfully");
            response.put("updatedTime", writeResult.getUpdateTime());

            return ResponseEntity.ok(response); // HTTP 200 (OK) status with the response map as the body
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * Retrieves a patient by their document ID.
     *
     * @param documentId The ID of the document in Firestore.
     * @return The retrieved Patient object or null if not found.
     * @throws ExecutionException, InterruptedException if Firestore operations fail.
     */
    public Patient getPatient(String documentId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("patients").document(documentId);
        ApiFuture<DocumentSnapshot> future = documentReference.get(); // .get() on documentReference initiates an asynchronous request to fetch the document from Firestore
        DocumentSnapshot documentSnapshot = future.get();
        if (documentSnapshot.exists()) { // Checks if the document was found in Firestore
            return documentSnapshot.toObject(Patient.class); // documentSnapshot.toObject(Patient.class) maps the document's fields
            // into a Patient object using the provided Patient class
        }
        return null;
    }

    /**
     * Updates an existing patient's data.
     *
     * @param documentId    The current document ID.
     * @param updatedPatient The updated patient object.
     * @throws ExecutionException, InterruptedException if Firestore operations fail.
     */
    public void updatePatient(String documentId, Patient updatedPatient) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        String newDocumentId = updatedPatient.getPatientUsername();
        if (newDocumentId == null || newDocumentId.isEmpty()) {
            throw new IllegalArgumentException("Patient username cannot be null or empty");
        } // Ensures that the new username (patientUsername) provided in the updatedPatient object is not null or empty

        DocumentReference oldDocumentReference = dbFirestore.collection("patients").document(documentId);
        ApiFuture<DocumentSnapshot> future = oldDocumentReference.get();
        DocumentSnapshot documentSnapshot = future.get();
        // Retrieves the existing document from the patients collection by its documentId

        if (documentSnapshot.exists()) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("patientUsername", newDocumentId);
            updates.put("doctorUsername", documentSnapshot.getString("doctorUsername"));
            updates.put("gender", updatedPatient.getGender());
            updates.put("age", updatedPatient.getAge());
            updates.put("type", updatedPatient.getType());
            // Creates a Map with updated fields while retaining the doctorUsername from old document

            DocumentReference newDocumentReference = dbFirestore.collection("patients").document(newDocumentId);
            ApiFuture<WriteResult> writeResult = newDocumentReference.set(updates);
            writeResult.get(); // Writes the updated data to the new document ID

            ApiFuture<QuerySnapshot> usersQuery = dbFirestore.collection("users")
                    .whereEqualTo("username", documentId).get(); // Queries the users collection to find all documents
            // where the username field matches the old documentId

            for (DocumentSnapshot userDoc : usersQuery.get().getDocuments()) {
                DocumentReference userDocumentReference = dbFirestore.collection("users").document(userDoc.getId());
                userDocumentReference.update("username", newDocumentId).get(); // Iterates through the matching documents
                // and updates the username field to the new username
            }

            if (!documentId.equals(newDocumentId)) {
                ApiFuture<WriteResult> deleteResult = oldDocumentReference.delete();
                deleteResult.get(); // Deletes the old document from the patients collection
            }
        } else {
            throw new RuntimeException("Patient document not found");
        }
    }


    /**
     * Retrieves a patient's document ID by their username.
     *
     * @param username The username of the patient.
     * @return The document ID or null if not found.
     */
    public String getPatientDocumentIdByUsername(String username) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference usersCollection = dbFirestore.collection("users");
        Query query = usersCollection.whereEqualTo("username", username).limit(1); // Query to search for documents in the users
        // collection where the username field equals the provided username. The limit is to ensure only one patient is retrieved
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments(); // .getDocuments() retrieves a list of QueryDocumentSnapshot objects,
        // each representing a document matching the query

        if (!documents.isEmpty()) {
            return documents.get(0).getId(); // If the documents list is not empty, retrieve the ID of the first document in the list and return it
        } else {
            return null;
        }
    }

    /**
     * Retrieves a patient's age by their document ID.
     *
     * @param documentId The ID of the document in Firestore.
     * @return The patient's age or null if not found.
     */
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

    /**
     * Retrieves a patient's gender by their document ID.
     *
     * @param documentId The ID of the document in Firestore.
     * @return The patient's gender or null if not found.
     */
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

    /**
     * Retrieves the type of a patient based on their document ID.
     *
     * @param documentId The ID of the patient's document in Firestore.
     * @return The patient's type or null if not found.
     * @throws ExecutionException, InterruptedException if Firestore operations fail.
     */
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


    /**
     * Retrieves all patients from a specific doctor.
     *
     * @param doctorId The ID of the doctor.
     * @return A list of Patient objects associated with the doctor.
     * @throws ExecutionException, InterruptedException if Firestore operations fail.
     */
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

    /**
     * Retrieves patients from a specific doctor by their age range.
     *
     * @param doctorId The ID of the doctor.
     * @param minAge   The minimum age of the patients.
     * @param maxAge   The maximum age of the patients.
     * @return A list of Patient objects within the specified age range.
     * @throws ExecutionException, InterruptedException if Firestore operations fail.
     */
    public List<Patient> getPatientsByAge(String doctorId, int minAge, int maxAge) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference patientsCollection = dbFirestore.collection("patients");
        Query query = patientsCollection
                .whereEqualTo("doctorUsername", doctorId)
                .whereGreaterThanOrEqualTo("age", minAge)
                .whereLessThanOrEqualTo("age", maxAge); // Query to get all documents where age fields are within the range

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        List<Patient> patients = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            Patient patient = document.toObject(Patient.class);
            patients.add(patient);
        }

        return patients;
    }

    /**
     * Retrieves patients from a specific doctor by their gender.
     *
     * @param doctorId The ID of the doctor.
     * @param gender   The gender of the patients to filter by.
     * @return A list of Patient objects with the specified gender.
     * @throws ExecutionException, InterruptedException if Firestore operations fail.
     */
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
    /**
     * Retrieves patients from a specific doctor by a partial username match.
     *
     * @param doctorId The ID of the doctor.
     * @param partialUsername The partial username to filter by.
     * @return A list of Patient objects matching the partial username.
     * @throws ExecutionException, InterruptedException if Firestore operations fail.
     */
    public List<Patient> getPatientsByUsername(String doctorId, String partialUsername) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference patientsCollection = dbFirestore.collection("patients");

        List<Patient> patients = new ArrayList<>();

        if (partialUsername != null && !partialUsername.trim().isEmpty()) {
            String lowercaseFirst = partialUsername.substring(0, 1).toLowerCase() + partialUsername.substring(1); // Converts the first letter to lowercase
            String uppercaseFirst = partialUsername.substring(0, 1).toUpperCase() + partialUsername.substring(1); // Converts the first letter to uppercase

            Query query2 = patientsCollection.whereEqualTo("doctorUsername", doctorId)
                    .orderBy("patientUsername")
                    .startAt(lowercaseFirst)
                    .endAt(lowercaseFirst + "\uf8ff"); // startAt(lowercaseFirst) and endAt(lowercaseFirst + "\uf8ff") creates
            // a range for all strings starting with lowercaseFirst

            Query query3 = patientsCollection.whereEqualTo("doctorUsername", doctorId)
                    .orderBy("patientUsername")
                    .startAt(uppercaseFirst)
                    .endAt(uppercaseFirst + "\uf8ff"); // startAt(uppercaseFirst) and endAt(uppercaseFirst + "\uf8ff") creates
            // a range for all strings starting with uppercaseFirst

            patients.addAll(fetchPatientsFromQuery(query2));
            patients.addAll(fetchPatientsFromQuery(query3));
            // fetchPatientsFromQuery executes each query and returns the results as a list of Patient objects

            patients = patients.stream().distinct().collect(Collectors.toList()); // removes duplicates if there are any with didtinct() and converts back to list
        } else {
            Query query = patientsCollection.whereEqualTo("doctorUsername", doctorId);
            patients = fetchPatientsFromQuery(query); // If partialUsername is null or empty, retrieves all patients associated with the given doctorId
        }

        return patients;
    }

    private List<Patient> fetchPatientsFromQuery(Query query) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<Patient> patients = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) { // Iterates through the matching documents in the query snapshot
            patients.add(document.toObject(Patient.class)); // List of patients that match the query
        }
        return patients;
    }




    /**
     * Retrieves patients from a specific doctor by their type.
     *
     * @param doctorId The ID of the doctor.
     * @param type     The type of the patients to filter by.
     * @return A list of Patient objects with the specified type.
     * @throws ExecutionException, InterruptedException if Firestore operations fail.
     */
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

    /**
     * Deletes a patient based on their document ID.
     *
     * @param documentId The ID of the patient's document to delete.
     * @return A ResponseEntity indicating the success or failure of the deletion.
     */
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

    /**
     * Checks if a patient exists based on their username.
     *
     * @param username The username of the patient.
     * @return True if the patient exists, false otherwise.
     * @throws ExecutionException, InterruptedException if Firestore operations fail.
     */
    public boolean doesPatientExist(String username) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<DocumentSnapshot> future = dbFirestore.collection("patients").document(username).get();
        DocumentSnapshot documentSnapshot = future.get();
        return documentSnapshot.exists(); // Returns true if the document exists and false if not
    }

    /**
     * Deletes a patient by their username.
     *
     * @param username The username of the patient to delete.
     * @return A message indicating the result of the deletion.
     * @throws ExecutionException, InterruptedException, FirebaseAuthException if Firestore operations fail.
     */
    public String deletePatientByUsername(String username) throws ExecutionException, InterruptedException, FirebaseAuthException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> queryFuture = dbFirestore.collection("patients")
                .whereEqualTo("patientUsername", username)
                .get(); // Creates a query to find documents where the patientUsername field matches the provided username

        QuerySnapshot querySnapshot = queryFuture.get(); // Retrieves a QuerySnapshot, which contains the documents matching the query

        if (querySnapshot.isEmpty()) {
            return "Patient with username '" + username + "' not found."; // If no documents match the query
            // the method returns a message indicating that no patient was found
        }

        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {

            document.getReference().delete();
        } // Iterates over all documents in the QuerySnapshot using getDocuments() and deletes each document by calling delete() on its DocumentReference

        return "Patient with username '" + username + "' deleted successfully.";
    }

}
