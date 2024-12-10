package com.animattio.animattio_web_app_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Configuration class for initializing Firebase services in the application.
 * This class sets up the Firebase SDK using a service account key file and
 * provides a bean for {@link FirebaseAuth} to handle authentication tasks.
 */
@Configuration
public class FirebaseConfig {

    /**
     * Path to the Firebase service account key file.
     * This value is loaded from the application's properties file using the key 'custom.firebase_keys'.
     */
    @Value("${custom.firebase_keys}")
    private String firebaseKeysPath;

    /**
     * Initializes the Firebase SDK and provides a {@link FirebaseAuth} bean for authentication.
     * If no Firebase app is currently initialized, it creates one using the service account key file.
     *
     * @return {@link FirebaseAuth} instance to handle authentication tasks.
     * @throws IOException if the service account key file cannot be read or is invalid.
     */
    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) { // Ensures that a Firebase app is only initialized once to avoid
            // duplicate or conflicting configurations
            InputStream serviceAccount = new ClassPathResource(firebaseKeysPath).getInputStream(); // Loads the service
            // account key file from the classpath. GetInputStream() opens the file as an input stream to be read
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount)) // Loads the credentials from the service account key file to connect
                    // with the right project in Firebase
                    .build();
            FirebaseApp.initializeApp(options); // Sets up the Firebase SDK with the project configuration
        }
        return FirebaseAuth.getInstance(); // Returns the default FirebaseAuth instance linked to the initialized Firebase app
    }
}
