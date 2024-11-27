package com.animattio.animattio_web_app_backend.doctor;

import com.animattio.animattio_web_app_backend.patient.Patient;
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

@Service
public class DoctorService {
    private final FirebaseAuth firebaseAuth;

    public DoctorService() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public String createDoctor(Doctor doctor) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture =  dbFirestore.collection("doctors").document(doctor.getUsername()).set(doctor);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }
    public Doctor getDoctor(String documentId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("doctors").document(documentId);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot documentSnapshot = future.get();
        Doctor doctor;
        if(documentSnapshot.exists()) {
            doctor = documentSnapshot.toObject(Doctor.class);
            return doctor;
        }
        return null;
    }

    public String getDoctorUsername(String documentId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("doctors").document(documentId);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot documentSnapshot = future.get();

        if (documentSnapshot.exists()) {
            return documentSnapshot.getString("username");
        }
        return null;
    }

    public boolean doesDoctorExist(String username) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("doctors")
                .whereEqualTo("username", username)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return !documents.isEmpty();
    }



    public void updateDoctorProfile(String username, String email, String password) throws ExecutionException, InterruptedException, FirebaseAuthException {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> query = dbFirestore.collection("doctors")
                .whereEqualTo("username", username).get();

        List<QueryDocumentSnapshot> documents = query.get().getDocuments();

        if (documents.isEmpty()) {
            throw new IllegalArgumentException("Doctor not found with username: " + username);
        }

        String uid = documents.get(0).getId();

        System.out.println("Retrieved UID: " + uid);

        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid);

        if (email != null && !email.isEmpty()) {
            request.setEmail(email);
        }
        if (password != null && !password.isEmpty()) {
            request.setPassword(password);
        }

        FirebaseAuth.getInstance().updateUser(request);

        if (email != null && !email.isEmpty()) {
            documents.get(0).getReference().update("email", email);
        }
    }



    public String deleteDoctor(String documentId){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = dbFirestore.collection("doctors").document(documentId).delete();
        return "Successfully deleted" + documentId;
    }

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




