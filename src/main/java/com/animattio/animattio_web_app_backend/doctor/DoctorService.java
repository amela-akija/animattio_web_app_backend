package com.animattio.animattio_web_app_backend.doctor;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Service class for managing doctor-related operations in Firestore.
 */
@Service
public class DoctorService {
    private final FirebaseAuth firebaseAuth;

    public DoctorService() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Creates a new doctor in the Firestore database.
     *
     * @param doctor The doctor object to create.
     * @return The update time of the created document as a string.
     * @throws ExecutionException   If an exception occurs during execution.
     * @throws InterruptedException If the operation is interrupted.
     */
    public String createDoctor(Doctor doctor) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore(); // retrieves a Firestore instance to interact with the database
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("doctors").document(doctor.getUsername()).set(doctor);
        return collectionsApiFuture.get().getUpdateTime().toString(); // once the document is successfully created or updated,
        // it retrieves the timestamp when the document was last modified
    }

    /**
     * Retrieves a doctor's details based on their document ID.
     *
     * @param documentId The ID of the document to retrieve.
     * @return The doctor object, or null if not found.
     * @throws ExecutionException   If an exception occurs during execution.
     * @throws InterruptedException If the operation is interrupted.
     */
    public Doctor getDoctor(String documentId) throws ExecutionException, InterruptedException {
        // ExcecutionError - Thrown if there is an issue during the execution of the Firestore operation (e.g., network errors or database issues
        // InterruptedException - Thrown if the thread waiting for the Firestore operation is interrupted
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("doctors").document(documentId); // reference of the document
        // with provided documentId
        ApiFuture<DocumentSnapshot> future = documentReference.get(); // retrieves document, Snapshot represents the sata stored in the document
        // it contains both fields and metadata
        DocumentSnapshot documentSnapshot = future.get(); // future.get() call blocks until the operation is complete,
        // returning a DocumentSnapshot object that contains the document’s data
        if (documentSnapshot.exists()) {
            return documentSnapshot.toObject(Doctor.class); // converts document to a Doctor object
        }
        return null;
    }

    /**
     * Retrieves a doctor's username based on their document ID.
     *
     * @param documentId The ID of the document to retrieve.
     * @return The username of the doctor, or null if not found.
     * @throws ExecutionException   If an exception occurs during execution.
     * @throws InterruptedException If the operation is interrupted.
     */
    public String getDoctorUsername(String documentId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("doctors").document(documentId);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot documentSnapshot = future.get();
        if (documentSnapshot.exists()) {
            return documentSnapshot.getString("username"); // returns the username field of the document
        }
        return null;
    }

    /**
     * Checks if a doctor exists based on their username.
     *
     * @param username The username to check.
     * @return True if the doctor exists, false otherwise.
     * @throws ExecutionException   If an exception occurs during execution.
     * @throws InterruptedException If the operation is interrupted.
     */
    public boolean doesDoctorExist(String username) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("doctors")
                .whereEqualTo("username", username)
                .get(); // queries the doctors collection for documents where the username field matches the given username
        List<QueryDocumentSnapshot> documents = future.get().getDocuments(); // extracts the list of matching documents from the QuerySnapshot
        // QuerySnapshot holds a collection of documents (DocumentSnapshot) that satisfy the query conditions
        return !documents.isEmpty(); // checks whether the list of matching documents is non-empty
        // returns true if at least one matching document is found
        // returns false if no documents match the query
    }

    /**
     * Deletes a doctor based on their username.
     *
     * @param username The username of the doctor to delete.
     * @return A message indicating the result of the deletion.
     * @throws ExecutionException   If an exception occurs during execution.
     * @throws InterruptedException If the operation is interrupted.
     * @throws FirebaseAuthException If an error occurs with Firebase authentication.
     */
    public String deleteDoctorByUsername(String username) throws ExecutionException, InterruptedException, FirebaseAuthException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> queryFuture = dbFirestore.collection("doctors")
                .whereEqualTo("username", username)
                .get();
        QuerySnapshot querySnapshot = queryFuture.get();

        if (querySnapshot.isEmpty()) {
            return "Doctor with username '" + username + "' not found.";
        }

        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) { // iterates over all matching documents
            String uid = document.getId(); // retrieves uid of the document that matches the query
            document.getReference().delete(); // deletes document from firestore
            FirebaseAuth.getInstance().deleteUser(uid); // deletes user from Firebase Authentication based on retrieved uid
        }

        return "Doctor with username '" + username + "' deleted successfully.";
    }

    /**
     * Updates a doctor's profile information.
     *
     * @param username The current username of the doctor.
     * @param email    The new email to update (if any).
     * @param password The new password to update (if any).
     * @throws ExecutionException   If an exception occurs during execution.
     * @throws InterruptedException If the operation is interrupted.
     * @throws FirebaseAuthException If an error occurs with Firebase authentication.
     */
    public void updateDoctorProfile(String username, String email, String password) throws ExecutionException, InterruptedException, FirebaseAuthException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> query = dbFirestore.collection("doctors")
                .whereEqualTo("username", username).get();

        List<QueryDocumentSnapshot> documents = query.get().getDocuments(); // retrieves the matching documents from query as a List<QueryDocumentSnapshot>

        if (documents.isEmpty()) {// if no documents match query
            throw new IllegalArgumentException("Doctor not found with username: " + username);
        }

        String uid = documents.get(0).getId(); // retrieves the uid of the only document that matches the query  because usernames are unique
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid);

        if (email != null && !email.isEmpty()) {
            request.setEmail(email);
        }
        if (password != null && !password.isEmpty()) {
            request.setPassword(password);
        }
        // updates the email or password if new values are provided

        FirebaseAuth.getInstance().updateUser(request); // updates Authentication user

    }

    /**
     * Deletes a doctor based on their document ID.
     *
     * @param documentId The ID of the doctor document to delete.
     * @return A message indicating the result of the deletion.
     */
    public String deleteDoctor(String documentId) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = dbFirestore.collection("doctors").document(documentId).delete();
        return "Successfully deleted " + documentId;
    }

    /**
     * Retrieves all doctors from the Firestore database except those with the "admin" role.
     *
     * @return A list of doctor objects.
     * @throws ExecutionException   If an exception occurs during execution.
     * @throws InterruptedException If the operation is interrupted.
     */
    public List<Doctor> getAllDoctors() throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        CollectionReference doctorsCollection = dbFirestore.collection("doctors");

        Query query = doctorsCollection.whereNotEqualTo("role", "admin");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        List<Doctor> doctors = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            Doctor doctor = document.toObject(Doctor.class);
            doctors.add(doctor);
        }

        return doctors;
    }

    /**
     * Updates a doctor's username and associated patient records.
     *
     * @param currentUsername The current username of the doctor.
     * @param newUsername     The new username to set.
     * @return A message indicating the result of the update.
     * @throws ExecutionException   If an exception occurs during execution.
     * @throws InterruptedException If the operation is interrupted.
     */
    public String updateDoctorUsername(String currentUsername, String newUsername) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        DocumentReference newUsernameRef = dbFirestore.collection("doctors").document(newUsername);
        ApiFuture<DocumentSnapshot> newUsernameSnapshot = newUsernameRef.get();
        if (newUsernameSnapshot.get().exists()) {
            return "Username already exists.";
        }

        Query query = dbFirestore.collection("doctors")
                .whereEqualTo("username", currentUsername);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

        if (!documents.isEmpty()) {
            DocumentSnapshot currentDoctorSnapshot = documents.get(0);
            Doctor doctor = currentDoctorSnapshot.toObject(Doctor.class);
            doctor.setUsername(newUsername);

            ApiFuture<WriteResult> updateResult = currentDoctorSnapshot.getReference().set(doctor);

            Query patientQuery = dbFirestore.collection("patients")
                    .whereEqualTo("doctorUsername", currentUsername);

            ApiFuture<QuerySnapshot> patientQuerySnapshot = patientQuery.get();
            List<QueryDocumentSnapshot> patientDocuments = patientQuerySnapshot.get().getDocuments();

            for (QueryDocumentSnapshot patientDoc : patientDocuments) {
                ApiFuture<WriteResult> updatePatientResult = patientDoc.getReference().update("doctorUsername", newUsername);
            }

            return "Username updated successfully.";
        } else {
            return "Doctor not found.";
        }
    }
}
