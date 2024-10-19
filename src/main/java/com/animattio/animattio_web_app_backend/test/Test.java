package com.animattio.animattio_web_app_backend.test;

import com.animattio.animattio_web_app_backend.game.Game;
import java.util.List;

public class Test {

    private String id;
    private List<Game> games;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Game> getGames() {
        return games;
    }

    public void setGames(List<Game> games) {
        this.games = games;
    }
}
