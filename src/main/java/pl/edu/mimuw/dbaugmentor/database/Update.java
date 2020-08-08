package pl.edu.mimuw.dbaugmentor.database;

import java.sql.JDBCType;
import java.util.HashMap;
import java.util.Map;

public class Update {
    private final Table table;
    private final JDBCType idType;
    private final Object idValue;
    private final Map<Column, Object> columnToValue = new HashMap<>();

    public Update(Table table, JDBCType type, Object value) {
        this.table = table;
        this.idType = type;
        this.idValue = value;
    }

    public void setColumnValue(Column column, Object valueToSet) {
        columnToValue.put(column, valueToSet);
    }

    public String getSql() {
        if (columnToValue.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder("UPDATE " + table.getName() + " SET ");
        for (Column column: columnToValue.keySet()) {
            stringBuilder.append(column.getName()).append("= ?, ");
        }
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        stringBuilder.append(" WHERE id = ?;");
        return stringBuilder.toString();
    }

    public void setUpdateParameters(JDBCStatement jdbcStmt) throws Exception {
        Map.Entry<Column, Object>[] columnsAndValues = columnToValue.entrySet().toArray(new Map.Entry[0]);
        for (int i = 1; i <= columnsAndValues.length; i++) {
            jdbcStmt.setParameter(columnsAndValues[i - 1].getKey().getType(), i, columnsAndValues[i - 1].getValue());
        }
        jdbcStmt.setParameter(idType, columnsAndValues.length + 1, idValue);
    }
}
