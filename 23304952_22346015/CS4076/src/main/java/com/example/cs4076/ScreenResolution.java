package org.example.finalproject;

import java.awt.Dimension;
import java.awt.Toolkit;
//calculate screen resolution
public class ScreenResolution {

    //get screen width of computer
    public static int getScreenWidth() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return (int) screenSize.getWidth();
    }

    //get screen height of computer
    public static int getScreenHeight() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return (int) screenSize.getHeight();
    }

    //print width and height
    public static void main(String[] args) {
        int width = getScreenWidth();
        int height = getScreenHeight();

        System.out.println("Screen Width: " + width);
        System.out.println("Screen Height: " + height);
    }
}
