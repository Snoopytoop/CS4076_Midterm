package org.example.javafx;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javafx.application.Application;

public class Client {

    // Define width and height variables
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 1000;
    // initialising IP address
    private static InetAddress IPAddress;
    // setting port number
    private static int PORT = 1234;
    // initialising Socket
    private static Socket link = null;
    // initialising BufferedReaders
    private static BufferedReader in = null;
    private static BufferedReader userEntry = null;
    // initialising Printwriter
    private static PrintWriter out = null;


    public static void main(String[] args) throws Exception {
        Application.launch(Homepage.class, args);
    }
}


