package org.example.finalproject;
//create lecture object
public class Lecture {
    private final String name;
    private final String room;

    public Lecture(String name, String room) {
        this.name = name;
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public String getRoom() {
        return room;
    }
}

