package pl.edu.mimuw.dbaugmentor.database;

import java.util.ArrayList;
import java.util.Objects;

public class ForeignKeyDefinition {
    private String fkName = "";
    private String sourceTableName = "";
    private String destinationTableName = "";
    private final ArrayList<Column> sourceColumns = new ArrayList<>();
    private final ArrayList<Column> destinationColumns = new ArrayList<>();

    public void setFkName(String fkName) {
        if (fkName != null) {
            this.fkName = fkName;
        }
    }

    public String getFkName() {
        return this.fkName;
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }

    public String getDestinationTableName() {
        return destinationTableName;
    }

    public void setDestinationTableName(String destinationTableName) {
        this.destinationTableName = destinationTableName;
    }

    public ArrayList<Column> getSourceColumns() {
        return sourceColumns;
    }

    public void addSourceColumn(Column column) {
        sourceColumns.add(column);
    }

    public ArrayList<Column> getDestinationColumns() {
        return destinationColumns;
    }

    public void addDestinationColumn(Column column) {
        destinationColumns.add(column);
    }

    @Override
    public String toString() {
        return "ForeignKeyDefinition{" +
                "sourceTableName='" + sourceTableName + '\'' +
                ", destinationTableName='" + destinationTableName + '\'' +
                ", sourceColumnNames=" + sourceColumns +
                ", destinationColumnNames=" + destinationColumns +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForeignKeyDefinition that = (ForeignKeyDefinition) o;
        return Objects.equals(sourceTableName, that.sourceTableName) &&
                Objects.equals(destinationTableName, that.destinationTableName) &&
                columnNamesEqual(sourceColumns, that.sourceColumns) &&
                columnNamesEqual(destinationColumns, that.destinationColumns);
    }

    private boolean columnNamesEqual(ArrayList<Column> columns, ArrayList<Column> columns2) {
        return Objects.equals(columnNames(columns), columnNames(columns2));
    }

    private ArrayList<String> columnNames(ArrayList<Column> columns) {
        ArrayList<String> names = new ArrayList<>();
        for (Column column : columns) {
            names.add(column.getName());
        }
        return names;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sourceTableName,
                destinationTableName,
                Objects.hashCode(columnNames(sourceColumns)),
                Objects.hashCode(columnNames(destinationColumns)));
    }
}
