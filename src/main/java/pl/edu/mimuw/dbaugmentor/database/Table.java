package pl.edu.mimuw.dbaugmentor.database;

import pl.edu.mimuw.dbaugmentor.copier.Entity;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import static java.sql.JDBCType.valueOf;

public class Table {
    private String name;
    private final UniqueKey primaryKey;
    private final ArrayList<UniqueKey> uniqueKeys = new ArrayList<>();
    private final Map<String, Column> columnNameToColumn = new HashMap<>();
    private final ArrayList<ForeignKeyDefinition> incomingReferences = new ArrayList<>();
    private final ArrayList<ForeignKeyDefinition> outgoingReferences = new ArrayList<>();
    private final Map<ForeignKeyReference, ArrayList<Entity>> foreignKeyReferenceToEntities = new HashMap<>();
    private final Map<Object, Entity> rows = new HashMap<>();
    private final Set<String> cachedColumnNames = new HashSet<>();
    private final Set<Column> notCachedColumns = new HashSet<>();
    private final Set<Column> pkColumns = new HashSet<>();
    private final Set<String> notCachedUniqueKeyColumnNames = new HashSet<>();
    private final boolean optimizeUniqueValueSearch;
    private boolean cacheFkReferences = false;

    public Table(boolean optimizeUniqueValueSearch) {
        this.optimizeUniqueValueSearch = optimizeUniqueValueSearch;
        this.primaryKey = new UniqueKey(optimizeUniqueValueSearch);
        this.primaryKey.markAsPrimaryKey();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Collection<Entity> getEntities() {
        return this.rows.values();
    }

    public ArrayList<UniqueKey> getUniqueKeys() {
        return uniqueKeys;
    }

    public UniqueKey getPrimaryKey() {
        return primaryKey;
    }

    public Collection<Column> getColumns() {
        return this.columnNameToColumn.values();
    }

    public Set<String> getColumnNamesSet() {
        return this.columnNameToColumn.keySet();
    }

    public ArrayList<Column> columnNamesToColumns(ArrayList<String> columnNames) {
        ArrayList<Column> columns = new ArrayList<>();
        for (String columnName : columnNames) {
            columns.add(columnNameToColumn.get(columnName));
        }
        return columns;
    }

    private void addColumn(Column column) {
        columnNameToColumn.put(column.getName(), column);
        notCachedColumns.add(column);
    }

    public Set<String> getCachedColumnNames() {
        return cachedColumnNames;
    }

    private void cacheColumn(Column column) {
        notCachedColumns.remove(column);
        cachedColumnNames.add(column.getName());
    }

    public Set<Column> getNotCachedColumns() {
        return notCachedColumns;
    }

    public Set<Column> getPkColumns() {
        return pkColumns;
    }

    public void readTableSchema(
            Connection connection, Map<String, Table> tables, ArrayList<String> uniqueConstraintsToSkip)
            throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        addForeignKeyDefinitions(metaData, tables);
        addPrimaryKeyDefinition(metaData);
        addUniqueIndexDefinition(metaData, uniqueConstraintsToSkip);
        finalizeUniqueKeys(connection);
    }

    public void fillData(
            Connection connection, boolean caching, Logger logger, boolean uniqueKeyCache) throws Exception {
        String query = getSelectQuery(caching);
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery();
        int done = 0;
        while (resultSet.next()) {
            Entity entity = new Entity();
            entity.setTable(this);
            if (caching) {
                for (Column column : getColumns()) {
                    entity.setColumnValueFromResultSet(resultSet, column);
                }
            } else {
                for (String columnName : cachedColumnNames) {
                    entity.setColumnValueFromResultSet(resultSet, columnNameToColumn.get(columnName));
                }
                for (String columnName : notCachedUniqueKeyColumnNames) {
                    entity.setColumnValueFromResultSet(resultSet, columnNameToColumn.get(columnName));
                }
            }
            rows.put(getPrimaryKeyValue(entity), entity);
            addEntityToFKReferenceMap(entity);
            for (UniqueKey uniqueKey : uniqueKeys) {
                uniqueKey.addKeyValueOfEntity(entity, uniqueKeyCache);
            }
            primaryKey.addKeyValueOfEntity(entity, uniqueKeyCache);
            if (!caching) {
                removeUniqueKeyColumnValues(entity);
            }
            done++;
            if (done % 10000 == 0) {
                logger.info("       " + done + " rows read");
            }
        }
        logger.info("       all " + done + " rows read");
    }

    private Object getPrimaryKeyValue(Entity entity) {
        ArrayList<Column> columns = primaryKey.getColumns();
        if (columns.size() == 1) {
            return entity.getColumnValue(columns.get(0).getName());
        }
        Object[] pkValue = new Object[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            pkValue[i] = entity.getColumnValue(columns.get(i).getName());
        }
        return pkValue;
    }

    private void removeUniqueKeyColumnValues(Entity entity) {
        for (String uniqueKeyColumn : notCachedUniqueKeyColumnNames) {
            entity.removeColumnValue(uniqueKeyColumn);
        }
    }

    public void addIncomingReferencesToAllEntities(HashMap<String, Table> tables) {
        ForeignKeyReference fkReference;
        for (ForeignKeyDefinition incomingReference : incomingReferences) {
            Table foreignTable = tables.get(incomingReference.getSourceTableName());
            for (Entity entity : rows.values()) {
                fkReference = new ForeignKeyReference(incomingReference);
                fkReference.setFkColumnValuesFromDestinationTableEntity(entity);
                ArrayList<Entity> referencedEntities = foreignTable.foreignKeyReferenceToEntities.get(fkReference);
                entity.addIncomingReferences(referencedEntities, incomingReference);
            }
        }
    }

    public void addColumnMetaData(DatabaseMetaData metaData) throws SQLException {
        ResultSet columns = metaData.getColumns(null, null, getName(), "%");
        while (columns.next()) {
            Column column = new Column();
            column.setName(columns.getString("COLUMN_NAME"));
            column.setNullable(columns.getString("IS_NULLABLE"));
            column.setAutoIncrement(columns.getString("IS_AUTOINCREMENT"));
            column.setType(valueOf(columns.getInt("DATA_TYPE")));
            addColumn(column);
        }
    }

    public void addColumnSizes(ResultSetMetaData resultSetMetaData) throws SQLException {
        int columnsNumber = resultSetMetaData.getColumnCount();
        for (int column = 1; column <= columnsNumber; column++) {
            String name = resultSetMetaData.getColumnName(column);
            int size = resultSetMetaData.getColumnDisplaySize(column);
            columnNameToColumn.get(name).setSize(size);
        }
    }

    public String getSelectQuery(boolean caching) {
        if (caching) {
            return String.format("SELECT * FROM %s;", getName());
        }
        StringBuilder query = new StringBuilder("SELECT ");
        boolean first = true;
        for (String columnName : cachedColumnNames) {
            if (first) {
                first = false;
            } else {
                query.append(", ");
            }
            query.append(columnName);
        }
        for (String columnName : notCachedUniqueKeyColumnNames) {
            query.append(", ").append(columnName);
        }
        query.append(" FROM ").append(getName()).append(";");
        return query.toString();
    }

    private void addPrimaryKeyDefinition(DatabaseMetaData metaData) throws SQLException {
        ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, getName());
        while (primaryKeys.next()) {
            Column column = columnNameToColumn.get(primaryKeys.getString("COLUMN_NAME"));
            primaryKey.addColumn(column);
            column.markAsFkColumn();
            cacheColumn(column);
            pkColumns.add(column);
            primaryKey.setName(primaryKeys.getString("PK_NAME"));
        }
    }

    private void addUniqueIndexDefinition(
            DatabaseMetaData metaData, ArrayList<String> uniqueConstraintsToSkip) throws SQLException {
        ResultSet uniqueIndexes = metaData.getIndexInfo(null, null, getName(), true, false);
        UniqueKey uniqueKey = new UniqueKey(optimizeUniqueValueSearch);
        while (uniqueIndexes.next()) {
            String name = uniqueIndexes.getString("INDEX_NAME");
            if (!uniqueConstraintsToSkip.contains(name) && !name.equals(primaryKey.getName())) {
                if (!uniqueKey.getName().equals(name)) {
                    uniqueKey = new UniqueKey(optimizeUniqueValueSearch);
                    uniqueKey.setName(name);
                    uniqueKey.setFilterCondition(uniqueIndexes.getString("FILTER_CONDITION"));
                    uniqueKeys.add(uniqueKey);
                }
                String columnName = uniqueIndexes.getString("COLUMN_NAME");
                if (columnNameToColumn.containsKey(columnName)) {
                    uniqueKey.addColumn(columnNameToColumn.get(columnName));
                    if (!cachedColumnNames.contains(columnName)) {
                        notCachedUniqueKeyColumnNames.add(columnName);
                    }
                }
            }
        }
    }

    private void finalizeUniqueKeys(Connection connection) {
        for (UniqueKey uniqueKey : uniqueKeys) {
            uniqueKey.finalizeColumns(name, connection);
        }
        primaryKey.finalizeColumns(name, connection);
    }

    private void addForeignKeyDefinitions(DatabaseMetaData metaData, Map<String, Table> tables) throws SQLException {
        setIncomingReferencesFromResultSet(metaData.getExportedKeys(null, null, getName()), tables);
        setOutgoingReferencesFromResultSet(metaData.getImportedKeys(null, null, getName()), tables);
    }

    private void addEntityToFKReferenceMap(Entity entity) {
        if (cacheFkReferences) {
            for (ForeignKeyDefinition fkDefinition : outgoingReferences) {
                ForeignKeyReference fkReference = new ForeignKeyReference(fkDefinition);
                fkReference.setFkColumnValuesFromSourceTableEntity(entity);
                if (!foreignKeyReferenceToEntities.containsKey(fkReference)) {
                    foreignKeyReferenceToEntities.put(fkReference, new ArrayList<>());
                }
                foreignKeyReferenceToEntities.get(fkReference).add(entity);
            }
        }
    }

    private void setIncomingReferencesFromResultSet(
            ResultSet resultSet, Map<String, Table> tables) throws SQLException {
        addReferencesFromResultSet(resultSet, incomingReferences, tables);
    }

    private void setOutgoingReferencesFromResultSet(
            ResultSet resultSet, Map<String, Table> tables) throws SQLException {
        addReferencesFromResultSet(resultSet, outgoingReferences, tables);
    }

    private void addReferencesFromResultSet(
            ResultSet resultSet, ArrayList<ForeignKeyDefinition> listToAdd,
            Map<String, Table> tables) throws SQLException {
        ForeignKeyDefinition currentFK = new ForeignKeyDefinition();
        while (resultSet.next()) {
            String fkTableName = resultSet.getString("FKTABLE_NAME");
            String pkTableName = resultSet.getString("PKTABLE_NAME");

            if (tables.containsKey(fkTableName) && tables.containsKey(pkTableName)) {
                if (!currentFK.getSourceTableName().equals(fkTableName) ||
                        !currentFK.getDestinationTableName().equals(pkTableName) ||
                        !currentFK.getFkName().equals(resultSet.getString("FK_NAME"))
                ) {
                    if (!currentFK.getSourceTableName().isEmpty()) {
                        listToAdd.add(currentFK);
                        currentFK = new ForeignKeyDefinition();
                    }
                    currentFK.setSourceTableName(fkTableName);
                    currentFK.setDestinationTableName(pkTableName);
                }
                currentFK.setFkName(resultSet.getString("FK_NAME"));
                Column sourceColumn =
                        tables.get(currentFK.getSourceTableName())
                                .columnNameToColumn.get(resultSet.getString("FKCOLUMN_NAME"));
                currentFK.addSourceColumn(sourceColumn);
                Column destinationColumn =
                        tables.get(currentFK.getDestinationTableName())
                                .columnNameToColumn.get(resultSet.getString("PKCOLUMN_NAME"));
                currentFK.addDestinationColumn(destinationColumn);
                if (fkTableName.equals(name) &&
                        columnNameToColumn.containsKey(resultSet.getString("FKCOLUMN_NAME"))) {
                    Column fkColumn = columnNameToColumn.get(resultSet.getString("FKCOLUMN_NAME"));
                    cacheColumn(fkColumn);
                    fkColumn.markAsFkColumn();
                }
            }
        }
        if (!currentFK.getSourceTableName().isEmpty()) {
            listToAdd.add(currentFK);
        }
    }

    public void addReference(
            String sourceTableName, ArrayList<Column> sourceColumns,
            String destinationTableName, ArrayList<Column> destinationColumns) {
        if (!sourceTableName.equals(name) && !destinationTableName.equals(name)) {
            return;
        }
        ForeignKeyDefinition foreignKeyDefinition = new ForeignKeyDefinition();
        foreignKeyDefinition.setSourceTableName(sourceTableName);
        for (Column sourceColumn : sourceColumns) {
            foreignKeyDefinition.addSourceColumn(sourceColumn);
            sourceColumn.markAsFkColumn();
        }
        foreignKeyDefinition.setDestinationTableName(destinationTableName);
        for (Column destinationColumn : destinationColumns) {
            foreignKeyDefinition.addDestinationColumn(destinationColumn);
        }
        if (sourceTableName.equals(name)) {
            outgoingReferences.add(foreignKeyDefinition);
        }
        if (destinationTableName.equals(name)) {
            incomingReferences.add(foreignKeyDefinition);
        }
    }

    public ArrayList<String> getModifiableUniqueKeyColumns() {
        ArrayList<String> modifiableUniqueKeyColumns = new ArrayList<>();
        for (UniqueKey uniqueKey : uniqueKeys) {
            if (uniqueKey.isModifiable()) {
                modifiableUniqueKeyColumns.addAll(uniqueKey.getColumnNames());
            }
        }
        modifiableUniqueKeyColumns.addAll(primaryKey.getColumnNames());
        return modifiableUniqueKeyColumns;
    }
}
