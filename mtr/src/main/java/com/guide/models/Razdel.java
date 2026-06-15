package com.guide.models;

import javafx.beans.property.*;

public class Razdel {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();

    public Razdel (int id, String name) {

        this.id.set(id);
        this.name.set(name);
    }

    // Геттеры свойст (для JavaFX)
    public IntegerProperty idProperty() {

        return id;
    }

    public StringProperty nameProperty() {

        return name;
    }

    // Геттеры и сеттеры объекта
    public int getId() { 
        
        return id.get();
    }

    public void setId(int id) {

        this.id.set(id);
    }

    public String getName() {

        return name.get();
    }

    public void setName(String name) {

        this.name.set(name);
    }
}
