package com.fileforge.model;

import javafx.beans.property.SimpleStringProperty;

public class ActivityLogEntry {

    private final SimpleStringProperty operation;
    private final SimpleStringProperty file;

    public ActivityLogEntry(String operation, String file) {
        this.operation = new SimpleStringProperty(operation);
        this.file = new SimpleStringProperty(file);
    }

    public SimpleStringProperty operationProperty() { return operation; }
    public SimpleStringProperty fileProperty() { return file; }
}