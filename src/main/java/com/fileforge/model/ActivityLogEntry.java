package com.fileforge.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ActivityLogEntry {

    private final SimpleIntegerProperty logId;
    private final SimpleStringProperty operation;
    private final SimpleStringProperty file;
    private final SimpleStringProperty notes;

    public ActivityLogEntry(int logId, String operation, String file, String notes) {
        this.logId = new SimpleIntegerProperty(logId);
        this.operation = new SimpleStringProperty(operation);
        this.file = new SimpleStringProperty(file);
        this.notes = new SimpleStringProperty(notes != null ? notes : "");
    }

    public int getLogId() { return logId.get(); }
    public SimpleIntegerProperty logIdProperty() { return logId; }

    public SimpleStringProperty operationProperty() { return operation; }
    public SimpleStringProperty fileProperty() { return file; }

    public String getNotes() { return notes.get(); }
    public void setNotes(String value) { notes.set(value); }
    public SimpleStringProperty notesProperty() { return notes; }
}