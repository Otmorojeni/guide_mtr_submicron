package com.guide.models;

import javafx.beans.property.*;

public class MaterialProperty {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty idMtr = new SimpleIntegerProperty();
    private final StringProperty physicalChemicalProperty = new SimpleStringProperty();
    private final StringProperty chemicaAction = new SimpleStringProperty();
    private final StringProperty fireSafety = new SimpleStringProperty();
    private final StringProperty storageConditions = new SimpleStringProperty();
    private final StringProperty incompatibility = new SimpleStringProperty();
    private final StringProperty pdk = new SimpleStringProperty();
    private final IntegerProperty classRisk = new SimpleIntegerProperty();

    public MaterialProperty(int id, int idMtr, String physicalChemicalProperty, String chemicaAction, String fireSafety, String storageConditions, String incompatibility, String pdk, int classRisk) {

        this.id.set(id);
        this.idMtr.set(idMtr);
        this.physicalChemicalProperty.set(physicalChemicalProperty);
        this.chemicaAction.set(chemicaAction);
        this.fireSafety.set(fireSafety);
        this.storageConditions.set(storageConditions);
        this.incompatibility.set(incompatibility);
        this.pdk.set(pdk);
        this.classRisk.set(classRisk);
    }

    // Геттеры свойств
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty idMtrProperty() { return idMtr; }
    public StringProperty physicalChemicalPropertyProperty() { return physicalChemicalProperty; }
    public StringProperty chemicaActionProperty() { return chemicaAction; }
    public StringProperty fireSafetyProperty() { return fireSafety; }
    public StringProperty storageConditionsProperty() { return storageConditions; }
    public StringProperty incompatibilityProperty() { return incompatibility; }
    public StringProperty pdkProperty() { return pdk; }
    public IntegerProperty classRiskProperty() { return classRisk; }

    // Геттеры и сеттеры
    public int getId() { return id.get(); }
    public int getIdMtr() { return idMtr.get(); }
    
    public String getPhysicalChemicalProperty() { return physicalChemicalProperty.get(); }
    public void setPhysicalChemicalProperty(String val) { this.physicalChemicalProperty.set(val); }

    public String getChemicaAction() { return chemicaAction.get(); }
    public void setChemicaAction(String val) { this.chemicaAction.set(val); }

    public String getFireSafety() { return fireSafety.get(); }
    public void setFireSafety(String val) { this.fireSafety.set(val); }

    public String getStorageConditions() { return storageConditions.get(); }
    public void setStorageConditions(String val) { this.storageConditions.set(val); }

    public String getIncompatibility() { return incompatibility.get(); }
    public void setIncompatibility(String val) { this.incompatibility.set(val); }

    public String getPdk() { return pdk.get(); }
    public void setPdk(String val) { this.pdk.set(val); }

    public int getClassRisk() { return classRisk.get(); }
    public void setClassRisk(int val) { this.classRisk.set(val); }
    
}
