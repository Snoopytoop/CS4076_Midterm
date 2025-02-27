package com.example.cs4076;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javafx.application.Application;

public class Client {

    // set stage width and height
    public static final int WIDTH = ScreenResolution.getScreenWidth()-200;
    public static final int HEIGHT = ScreenResolution.getScreenHeight()-200;

    public static void main(String[] args) throws Exception {
        Application.launch(Homepage.class, args);
    }
}


