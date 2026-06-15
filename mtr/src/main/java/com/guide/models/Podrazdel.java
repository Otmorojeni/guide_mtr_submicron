package com.guide.models;

import javafx.beans.property.*;

public class Podrazdel {
    
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty idRazdel = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty mark = new SimpleStringProperty();

    public Podrazdel(int id, int idRazdel, String name, String mark) {

        this.id.set(id);
        this.idRazdel.set(idRazdel);
        this.name.set(name);
        this.mark.set(mark);
    }

    // Геттеры свойств
    public IntegerProperty idProperty() {

        return id;
    }

    public IntegerProperty idRazdelProperty() {

        return idRazdel;
    }

    public StringProperty nameProperty() {

        return name;
    }

    public StringProperty markProperty() {

        return mark;
    }

    // Геттеры и сеттеры объекта
    public int getId() {

        return id.get();
    }

    public void setId(int id) {

        this.id.set(id);
    }

    public int getIdRazdel() {

        return idRazdel.get();
    }

    public void setIdRazdel(int idRazdel) {

        this.idRazdel.set(idRazdel);
    }

    public String getName() {

        return name.get();
    }

    public void setName(String name) {

        this.name.set(name);
    }

    public String getMark() {

        return mark.get();
    }

    public void setMark(String mark) {

        this.mark.set(mark);
    }
}
