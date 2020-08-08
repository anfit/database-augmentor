package pl.edu.mimuw.dbaugmentor.copier;

import pl.edu.mimuw.dbaugmentor.database.*;

import java.sql.*;
import java.util.*;

public class Entity {
    private EntityCopy entityCopy = null;
    private Entity original;
    private Table table;
    private JDBCType idType;
    private final Map<String, Object> columnToValue = new HashMap<>();
    private final ArrayList<IncomingReference> incomingReferences = new ArrayList<>();
    private int copyNumber;

    public Entity() {
        original = this;
    }

    public int getCopyNumber() {
        return copyNumber;
    }

    public void setCopyNumber(int copyNumber) {
        this.copyNumber = copyNumber;
    }

    public JDBCType getIdType() {
        return idType;
    }

    public void setIdType(JDBCType type) {
        idType = type;
    }

    public Map<String, Object> getColumnToValue() {
        return columnToValue;
    }

    public void removeColumnValue(String columnName) {
        columnToValue.remove(columnName);
    }

    public void fixUniqueKeyValue(UniqueKey uniqueKey, boolean readableString, boolean cache) throws Exception {
        ArrayList<Object> uniqueKeyValue = uniqueKey.getNewUniqueKeyValue(this, readableString, cache);
        ArrayList<Column> uniqueKeyColumns = uniqueKey.getColumns();
        for (int i = 0; i < uniqueKeyColumns.size(); i++) {
            setColumnValue(uniqueKeyColumns.get(i).getName(), uniqueKeyValue.get(i));
        }
    }

    public void setEntityCopy(EntityCopy entityCopy) {
         this.entityCopy = entityCopy;
    }

    public EntityCopy getEntityCopy() {
        return this.entityCopy;
    }

    public void setOriginal(Entity entity) {
        original = entity;
    }

    public Entity getOriginal() {
        return original;
    }

    public boolean isCopied() {
        return entityCopy != null;
    }

    public void setColumnValue(String name, Object valueToSet) {
        // TODO usunąć
//        try {
//            if (original != this && columnToValue.containsKey(name) &&
//                    !getColumnValue(name).toString().equals(valueToSet.toString()) &&
//                    getColumnValue(name) != original.getColumnValue(name)) {
//                System.err.println(
//                        "There should not be second modification of column " + name +
//                                " from " + getColumnValue(name) + " to " + valueToSet +
//                                " when original is " + original.getColumnValue(name));
//                System.err.println(" entity " + this + " with original " + original);
//                System.err.println(getTable().getName());
//            }
//        } catch (NullPointerException ignored) {}
        columnToValue.put(name, valueToSet);
    }

    public Object getColumnValue(String columnName) {
        return columnToValue.get(columnName);
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public void setColumnValueFromResultSet(ResultSet resultSet, Column column) throws Exception {
        Object valueToSet = getSingleValueFromResultSet(resultSet, column.getName(), column.getType());
        if (column.getName().equals("id")) {
            setIdType(column.getType());
        }
        setColumnValue(column.getName(), valueToSet);
    }

    public String getFillDataSql() {
        StringBuilder sqlBuilder = new StringBuilder(String.format("SELECT * FROM %s WHERE", getTable().getName()));
        boolean first = true;
        for (Column pkColumn : getTable().getPkColumns()) {
            if (!first) {
                sqlBuilder.append(" AND");
            }
            first = false;
            sqlBuilder.append(" ").append(pkColumn.getName()).append(" = (?)");
        }
        sqlBuilder.append(";");
        return sqlBuilder.toString();
    }

    public String getSqlInsert() {
        Set<String> columnNames = getTable().getColumnNamesSet();
        if (columnNames.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder("INSERT INTO " + getTable().getName());
        StringBuilder values = new StringBuilder(" (");
        StringBuilder columnNamesSQL = new StringBuilder(" (");
        for (String columnName : columnNames) {
            columnNamesSQL.append(columnName).append(", ");
            values.append("?, ");
        }
        values.delete(values.length() - 2, values.length());
        values.append(")");
        columnNamesSQL.delete(columnNamesSQL.length() - 2, columnNamesSQL.length());
        columnNamesSQL.append(")");
        stringBuilder.append(columnNamesSQL).append(" VALUES").append(values).append(";");
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return "Entity{" +
                "table=" + table.getName() +
                ", columnToValue=" + columnToValue +
                '}';
    }

    public void addIncomingReferences(ArrayList<Entity> entities, ForeignKeyDefinition foreignKeyDefinition) {
        if (entities != null) {
            for (Entity entity : entities) {
                this.incomingReferences.add(new IncomingReference(entity, foreignKeyDefinition));
            }
        }
    }

    public ArrayList<IncomingReference> getIncomingReferences() {
        return incomingReferences;
    }

    public void addIncomingReference(IncomingReference incomingReference) {
        incomingReferences.add(incomingReference);
    }

    public void setInsertParameters(JDBCStatement jdbcStmt) throws Exception {
        Column[] columns = getTable().getColumns().toArray(new Column[0]);
        for (int i = 1; i <= columns.length; i++) {
            jdbcStmt.setParameter(columns[i - 1].getType(), i, getColumnValue(columns[i - 1].getName()));
        }
    }

    private Object getSingleValueFromResultSet(ResultSet resultSet, String name, JDBCType type) throws Exception {
        Object result;
        switch (type) {
            case BIT:
            case BOOLEAN:
                result = resultSet.getBoolean(name);
                break;
            case CHAR:
            case VARCHAR:
                result = resultSet.getString(name);
                break;
            case LONGVARCHAR:
                result = resultSet.getAsciiStream(name);
                break;
            case TINYINT:
                result = resultSet.getByte(name);
                break;
            case SMALLINT:
                result = resultSet.getShort(name);
                break;
            case INTEGER:
                result = resultSet.getInt(name);
                break;
            case DECIMAL:
            case NUMERIC:
                result = resultSet.getBigDecimal(name);
                break;
            case BIGINT:
                result = resultSet.getLong(name);
                break;
            case REAL:
                result = resultSet.getFloat(name);
                break;
            case FLOAT:
            case DOUBLE:
                result = resultSet.getDouble(name);
                break;
            case DATE:
                result = resultSet.getDate(name);
                break;
            case TIME:
                result = resultSet.getTime(name);
                break;
            case TIMESTAMP:
            case TIMESTAMP_WITH_TIMEZONE:
                result = resultSet.getTimestamp(name);
                break;
            case BINARY:
            case VARBINARY:
                result = resultSet.getBytes(name);
                break;
            case LONGVARBINARY:
                result = resultSet.getBinaryStream(name);
                break;
            case ARRAY:
                result = resultSet.getArray(name);
                break;
            default:
                throw new Exception(
                        "Unsupported data type " + type + " of column " + name + " in table " + table.getName());
        }
        if (resultSet.wasNull()) {
            return null;
        }
        return result;
    }
}
