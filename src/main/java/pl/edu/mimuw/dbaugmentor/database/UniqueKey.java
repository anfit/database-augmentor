package pl.edu.mimuw.dbaugmentor.database;

import pl.edu.mimuw.dbaugmentor.copier.Entity;

import java.sql.Connection;
import java.sql.JDBCType;
import java.util.ArrayList;

import static java.sql.JDBCType.CHAR;
import static java.sql.JDBCType.VARCHAR;

public class UniqueKey {
    private String name = "";
    private final ArrayList<Column> columns = new ArrayList<>();
    private boolean allColumnsFK = false;
    private boolean isPrimaryKey = false;
    private String filterCondition;
    private final UniqueKeyModifier modifier;
    private boolean modifiable = true;

    public UniqueKey(boolean optimizedSearch) {
        modifier = new UniqueKeyModifier(optimizedSearch);
    }

    public void finalizeColumns(String tableName, Connection connection) {
        setColumnToModify();
        modifier.setUniqueKeyChecker(new UniqueKeyChecker(connection, tableName, columns));
    }

    public ArrayList<String> getColumnNames() {
        ArrayList<String> columnNames = new ArrayList<>();
        for (Column column : columns) {
            columnNames.add(column.getName());
        }
        return columnNames;
    }

    public boolean isModifiable() {
        return modifiable;
    }

    public void setFilterCondition(String condition) {
        this.filterCondition = condition;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Column> getColumns() {
        return columns;
    }

    public void addColumn(Column column) {
        columns.add(column);
    }

    public void addKeyValueOfEntity(Entity entity, boolean cache) {
        ArrayList<Object> uniqueKeyValue = new ArrayList<>();
        if (modifiable) {
            for (Column column : columns) {
                uniqueKeyValue.add(entity.getColumnValue(column.getName()));
            }
            modifier.addKeyValue(uniqueKeyValue, cache);
        }
    }

    public void markAsPrimaryKey() {
        isPrimaryKey = true;
    }

    private boolean hasFilterCondition() {
        return filterCondition != null && !filterCondition.isEmpty();
    }

    private int columnToModify() {
        if (columns.size() == 0) {
            return -1;
        }
        int columnToModify = -1;
        for (int i = 0; i < columns.size(); i++) {
            JDBCType currType = columns.get(i).getType();
            if ((!columns.get(i).isForeignKeyColumn() || isPrimaryKey) &&
                    modifier.isTypeSupported(currType)) {
                if (currType != CHAR && currType != VARCHAR) {
                    return i;
                }
                columnToModify = i;
            }
        }
        if (columnToModify == -1) {
            allColumnsFK = true;
        }
        return columnToModify;
    }

    public void setColumnToModify() {
        if (modifiable && !modifier.hasColumnToModify()) {
            int columnNumber = columnToModify();
            if ((allColumnsFK && !isPrimaryKey) || hasFilterCondition() || columnNumber == -1) {
                modifiable = false;
            } else {
                modifier.setColumnToModify(columnNumber, columns.get(columnNumber));
            }
        }
    }

    public ArrayList<Object> getNewUniqueKeyValue(
            Entity entity, boolean readableStrings, boolean cache) throws Exception {
        ArrayList<Object> keyValue = getKeyValueOfEntity(entity);
        if (!modifiable || keyValue.contains(null)) {
            return keyValue;
        }
        if (readableStrings) {
            modifier.makeKeyValueUnique(keyValue, String.valueOf(entity.getCopyNumber()), cache);
        } else {
            modifier.makeKeyValueUnique(keyValue, cache);
        }
        modifier.addKeyValue(keyValue, cache);
        return keyValue;
    }

    private ArrayList<Object> getKeyValueOfEntity(Entity entity) {
        ArrayList<Object> columnValues = new ArrayList<>();
        for (Column column : columns) {
            columnValues.add(entity.getColumnValue(column.getName()));
        }
        return columnValues;
    }
}
