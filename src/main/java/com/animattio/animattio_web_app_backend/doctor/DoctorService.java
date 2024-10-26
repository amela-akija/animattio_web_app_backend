package com.animattio.animattio_web_app_backend.doctor;

import com.animattio.animattio_web_app_backend.patient.Patient;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class DoctorService {

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




