package pl.edu.mimuw.dbaugmentor.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

public class UniqueKeyChecker {
    private final ArrayList<Column> uniqueKeyColumns;
    private String query;
    private final Connection connection;
    private boolean savedResult;
    private boolean queryAsked = false;

    protected UniqueKeyChecker(Connection connection, String tableName, ArrayList<Column> columns) {
        uniqueKeyColumns = columns;
        this.connection = connection;
        generateQueryToCheckUniqueness(tableName);
    }

    protected boolean isUniqueInDatabase(ArrayList<Object> values) throws Exception {
        if (queryAsked) {
            return savedResult;
        }
        JDBCStatement jdbcStmt = new JDBCStatement(connection, query);
        Column[] columns = uniqueKeyColumns.toArray(new Column[0]);
        for (int i = 1; i <= columns.length; i++) {
            jdbcStmt.setParameter(columns[i - 1].getType(), i, values.get(i - 1));
        }
        ResultSet resultSet = jdbcStmt.executeQuery();
        if (resultSet.next()) {
            boolean result = resultSet.getBoolean(1);
            queryAsked = true;
            savedResult = !result;
            return !result;
        }
        throw new Exception("No result for query " + jdbcStmt.getSqlToExecute());
    }

    private void generateQueryToCheckUniqueness(String tableName) {
        StringBuilder sqlBuilder = new StringBuilder(String.format("SELECT EXISTS(SELECT 1 FROM %s WHERE", tableName));
        boolean first = true;
        for (Column column : uniqueKeyColumns) {
            if (!first) {
                sqlBuilder.append(" AND");
            }
            first = false;
            sqlBuilder.append(" ").append(column.getName()).append(" = (?)");
        }
        sqlBuilder.append(")");
        query =  sqlBuilder.toString();
    }
}
