package pl.edu.mimuw.dbaugmentor.database;

import pl.edu.mimuw.dbaugmentor.copier.Entity;

import java.util.ArrayList;
import java.util.Objects;

public class ForeignKeyReference {
    private final ForeignKeyDefinition definition;
    private final ArrayList<Object> fkColumnValues = new ArrayList<>();

    public ForeignKeyReference(ForeignKeyDefinition fkDefinition) {
        this.definition = fkDefinition;
    }

    public void setFkColumnValuesFromSourceTableEntity(Entity entity) {
        setFkColumnValuesFromEntity(entity, getDefinition().getSourceColumns());
    }

    public void setFkColumnValuesFromDestinationTableEntity(Entity entity) {
        setFkColumnValuesFromEntity(entity, getDefinition().getDestinationColumns());
    }

    private void setFkColumnValuesFromEntity(Entity entity, ArrayList<Column> columns) {
        for (Column column : columns) {
            fkColumnValues.add(entity.getColumnValue(column.getName()));
        }
    }

    public ForeignKeyDefinition getDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        return "ForeignKeyReference{" +
                "definition=" + definition +
                ", fkColumnValues=" + fkColumnValues +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForeignKeyReference that = (ForeignKeyReference) o;
        return Objects.equals(definition, that.definition) &&
                Objects.equals(fkColumnValues, that.fkColumnValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(definition, Objects.hashCode(fkColumnValues));
    }
}
