package com.animattio.animattio_web_app_backend.doctor;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class DoctorService {

    public String createDoctor(Doctor doctor) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture =  dbFirestore.collection("doctors").document(doctor.getDoctorUsername()).set(doctor);
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
}
