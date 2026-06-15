package com.guide.models;

import javafx.beans.property.*;

public class Material {
    
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty idRazdel = new SimpleIntegerProperty();
    private final IntegerProperty idPodrazdel = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty mark = new SimpleStringProperty();
    private final IntegerProperty idUnit = new SimpleIntegerProperty();
    private final DoubleProperty limitReserve = new SimpleDoubleProperty();
    private final IntegerProperty storageTime = new SimpleIntegerProperty();
    private final IntegerProperty idClassClean = new SimpleIntegerProperty();
    private final BooleanProperty deleted = new SimpleBooleanProperty();
    private final StringProperty tu = new SimpleStringProperty();

    public Material(int id, int idRazdel, int idPodrazdel, String name, 
                    String mark, int idUnit, Double limitReserve, int storageTime, 
                    int idClassClean, Boolean deleted, String tu) {

        this.id.set(id);
        this.idRazdel.set(idRazdel);
        this.idPodrazdel.set(idPodrazdel);
        this.name.set(name);
        this.mark.set(mark);
        this.idUnit.set(idUnit);
        this.limitReserve.set(limitReserve);
        this.storageTime.set(storageTime);
        this.idClassClean.set(idClassClean);
        this.deleted.set(deleted);
        this.tu.set(tu);
    }

    // Геттеры JavaFX
    public IntegerProperty idProperty() {return id;}
    public IntegerProperty idRazdelProperty() {return idRazdel;}
    public IntegerProperty idPodrazdelProperty() {return idPodrazdel;}
    public StringProperty nameProperty() {return name;}
    public StringProperty markProperty() {return mark;}
    public IntegerProperty idUnitProperty() {return idUnit;}
    public DoubleProperty limitReserveProperty() {return limitReserve;}
    public IntegerProperty storageTimeProperty() {return storageTime;}
    public IntegerProperty idClassCleanProperty() {return idClassClean;}
    public BooleanProperty deletedProperty() {return deleted;}
    public StringProperty tuProperty() {return tu;}

    // Геттеры и сеттеры
    public int getId() {return id.get();}
    public void setId(int id) {this.id.set(id);}

    public int getIdRazdel() {return idRazdel.get();}
    public void setIdRazdel(int idRazdel) {this.idRazdel.set(idRazdel);}

    public int getIdPodrazdel() {return idPodrazdel.get();}
    public void setIdPodrazdel(int idPodrazdel) {this.idPodrazdel.set(idPodrazdel);}

    public String getName() {return name.get();}
    public void setName(String name) {this.name.set(name);}

    public String getMark() {return mark.get();}
    public void setMark(String mark) {this.mark.set(mark);}

    public int getIdUnit() {return idUnit.get();}
    public void setIdUnit(int idUnit) {this.idUnit.set(idUnit);}

    public Double getLimitReserve() {return limitReserve.get();}
    public void setLimitReserve(Double limitReserve) {this.limitReserve.set(limitReserve);}

    public int getStorageTime() {return storageTime.get();}
    public void setStorageTime(int storageTime) {this.storageTime.set(storageTime);}

    public int getIdClassClean() {return idClassClean.get();}
    public void setIdClassClean(int idClassClean) {this.idClassClean.set(idClassClean);}

    public Boolean getDeleted() {return deleted.get();}
    public void setDeleted(Boolean deleted) {this.deleted.set(deleted);}
    
    public String getTu() {return tu.get();}
    public void setTu(String tu) {this.tu.set(tu);}
}
