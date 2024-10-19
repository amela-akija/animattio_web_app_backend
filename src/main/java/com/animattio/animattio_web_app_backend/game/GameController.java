package com.animattio.animattio_web_app_backend.game;

import com.animattio.animattio_web_app_backend.patient.Patient;
import com.animattio.animattio_web_app_backend.patient.PatientService;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
@RestController
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/games/{documentId}")
    public Game getGame(@PathVariable String documentId) throws ExecutionException, InterruptedException {
        Game game = gameService.getGame(documentId);
        if (game != null) {
            return game;
        } else {
            throw new ResourceNotFoundException("Game not found");
        }
    }
}
