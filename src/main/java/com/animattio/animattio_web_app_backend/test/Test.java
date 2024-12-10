package com.animattio.animattio_web_app_backend.test;

import com.animattio.animattio_web_app_backend.game.Game;

import java.util.List;

/**
 * Represents a test consisting of multiple games.
 * Each test is associated with a specific user ID and contains a list of games.
 */
public class Test {
    /**
     * The unique identifier of the user associated with this test.
     */
    private String userId;

    /**
     * The list of games included in this test.
     */
    private List<Game> gamesInTest;

    /**
     * Retrieves the user ID associated with this test.
     *
     * @return the user ID of the test.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID for this test.
     *
     * @param userId the user ID to associate with the test.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the list of games included in this test.
     *
     * @return the list of games.
     */
    public List<Game> getGamesInTest() {
        return gamesInTest;
    }

    /**
     * Sets the list of games for this test.
     *
     * @param gamesInTest the list of games to include in the test.
     */
    public void setGamesInTest(List<Game> gamesInTest) {
        this.gamesInTest = gamesInTest;
    }
}
