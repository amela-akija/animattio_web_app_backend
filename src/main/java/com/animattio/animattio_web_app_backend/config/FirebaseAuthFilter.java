package com.animattio.animattio_web_app_backend.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * FirebaseAuthFilter is a Spring Security filter that processes Firebase authentication tokens.
 * It validates the Firebase ID token, retrieves the user's role from Firestore,
 * and sets the authentication context for the request.
 */
@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

    private final Firestore firestore;
    private final FirebaseAuth firebaseAuth;

    /**
     * Constructor for FirebaseAuthFilter.
     *
     * @param firebaseAuth FirebaseAuth instance used for verifying ID tokens.
     */
    public FirebaseAuthFilter(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth; // FirebaseAuth instance used to verify Firebase ID tokens
        this.firestore = FirestoreClient.getFirestore(); // Firestore client to query the Firestore database
    }

    /**
     * Filters the incoming HTTP request, verifies the Firebase token, and sets the authentication context.
     *
     * @param request     the incoming HTTP request
     * @param response    the outgoing HTTP response
     * @param filterChain the filter chain to process the request
     * @throws ServletException if an error occurs during filtering
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getHeader("Authorization"); // Extracted Authorization header from the request


        if (token != null && token.startsWith("Bearer ")) { // Checks if the token is present and starts with "Bearer "
            token = token.substring(7); // Remove "Bearer " from the beginning
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token); // Verifying the Firebase ID token
                String uid = decodedToken.getUid(); // UID retrieved from token

                DocumentSnapshot userDoc = firestore.collection("doctors").document(uid).get().get(); // document with fetched UID

                String role = "doctor"; // Default role
                if (userDoc.exists()) {
                    role = (String) userDoc.get("role"); // role from the Firestore document
                    if (role == null) {
                        role = "doctor"; // Default role if there is no role in firestore
                    }
                }
                // Authentication Object
                // UsernamePasswordAuthenticationToken is a Spring Security class used to represent an authenticated user
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        decodedToken.getUid(), null, Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role))
                );
                // Credentials are not needed because the token already verified the user
                // SimpleGrantedAuthority is used to represent a single role or permission in Spring Security for example ROLE_doctor
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // Adds details about the current HTTP request to the authentication object
                SecurityContextHolder.getContext().setAuthentication(authentication); // SecurityContext contains all security-related information for the request, including the authenticated user
                // Calling the setAuthentication alerts Spring Security of the user and their role
            } catch (Exception e) {
                SecurityContextHolder.clearContext();// Clears the security context if token is not verified properly
                // to prevent unauthorized access
            }
        }
        filterChain.doFilter(request, response); // Passes the request and response objects to the next filter in the chain and
        // ensures that the request continues through the security and application filters
        // Filter chain is a sequence of filters through which every HTTP request passes
        // After a filter processes a request, it must pass control to the next filter in the chain using
    }
}
