package com.animattio.animattio_web_app_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

//    @Value("${custom.firebase_keys}")
//    private String firebaseKeysPath;

    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            String serviceAccountKey = System.getenv("FIREBASE_SERVICE_ACCOUNT");
            if (serviceAccountKey == null || serviceAccountKey.isEmpty()) {
                throw new IllegalStateException("Environment variable 'FIREBASE_SERVICE_ACCOUNT' is not set.");
            }

            ByteArrayInputStream serviceAccountStream =
                    new ByteArrayInputStream(serviceAccountKey.getBytes(StandardCharsets.UTF_8));

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .build();
            FirebaseApp.initializeApp(options);
        }
        return FirebaseAuth.getInstance();
    }
}
