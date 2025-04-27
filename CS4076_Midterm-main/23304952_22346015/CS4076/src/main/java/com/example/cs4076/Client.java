package com.example.cs4076;

import javafx.application.Application;

public class Client {

    // set stage width and height
    public static final int WIDTH = ScreenResolution.getScreenWidth()-1600   ;
    public static final int HEIGHT = ScreenResolution.getScreenHeight()-800;

    public static void main(String[] args) throws Exception {
        Application.launch(Homepage.class, args);
    }
}


