package com.DataFinancial.NoteJackal;

public class NoteGroup {

    private int id;
    private String name;


    public NoteGroup() {

        this.name = "";
    }

    public NoteGroup(String name) {

      this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {

        return name;
    }
}
