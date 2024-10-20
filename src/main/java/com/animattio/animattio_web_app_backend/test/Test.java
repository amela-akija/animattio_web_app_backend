package com.animattio.animattio_web_app_backend.test;

import com.animattio.animattio_web_app_backend.game.Game;

import java.util.List;

public class Test {
    private List<Game> gamesInTest;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String userId;

    public List<Game> getGamesInTest() {
        return gamesInTest;
    }

    public void setGamesInTest(List<Game> gamesInTest) {
        this.gamesInTest = gamesInTest;
    }
}
