package com.example.cs4076;

import javafx.application.Application;

public class Client {

    // set stage width and height
    public static final int WIDTH = ScreenResolution.getScreenWidth()-100;
    public static final int HEIGHT = ScreenResolution.getScreenHeight()-100;

    public static void main(String[] args) throws Exception {
        Application.launch(Homepage.class, args);
    }
}


