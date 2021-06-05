package com.example.clientchessapp;

import android.app.Application;

public class App extends Application {

    private int game_id;
    private Boolean white;

    public Boolean getWhite() {
        return white;
    }

    public void setWhite(Boolean white) {
        this.white = white;
    }

    public void setGame_id(int game_id) {
        this.game_id = game_id;
    }

    public int getGame_id() {
        return game_id;
    }

}
