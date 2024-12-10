// not used
package com.animattio.animattio_web_app_backend.authentication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/signin")
public class SignInController {

    @Autowired
    private FirebaseAuth firebaseAuth;

    @PostMapping
    public ResponseEntity<?> handleRequest(@RequestHeader("Authorization") String token) {
        try {
            String idToken = token.replace("Bearer ", "");
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String uid = decodedToken.getUid();
            return ResponseEntity.ok("Authenticated successfully");
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }
}
