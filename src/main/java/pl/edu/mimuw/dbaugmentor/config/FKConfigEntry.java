package pl.edu.mimuw.dbaugmentor.config;

import java.util.ArrayList;

public class FKConfigEntry {
    private String sourceTableName;
    private final ArrayList<String> sourceColumnNames = new ArrayList<>();
    private String destinationTableName;
    private final ArrayList<String> destinationColumnNames = new ArrayList<>();

    public String getSourceTableName() {
        return sourceTableName;
    }

    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }

    public ArrayList<String> getSourceColumnNames() {
        return sourceColumnNames;
    }

    public void addSourceColumnName(String name) {
        sourceColumnNames.add(name);
    }

    public String getDestinationTableName() {
        return destinationTableName;
    }

    public void setDestinationTableName(String destinationTableName) {
        this.destinationTableName = destinationTableName;
    }

    public ArrayList<String> getDestinationColumnNames() {
        return destinationColumnNames;
    }

    public void addDestinationColumnName(String name) {
        destinationColumnNames.add(name);
    }
}
