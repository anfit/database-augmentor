package pl.edu.mimuw.dbaugmentor.database;

import java.util.Collection;

public class SizeQueryHelper {
    static final String prefix = "SELECT sum(size) as columns_size, sum(reltuples) as rows FROM (";
    static final String separator = "  UNION ALL";
    static final String suffix = ") a;";

    static String getPrefix() {
        return prefix;
    }

    static String getSeparator() {
        return separator;
    }

    static String getSuffix() {
        return suffix;
    }

    static String partForColumnsInOneRow(Collection<String> columnNames) {
        StringBuilder columnsSizePerRow = new StringBuilder();
        boolean first = true;
        for (String columnName : columnNames) {
            if (!first) {
                columnsSizePerRow.append(" + ");
            } else {
                first = false;
            }
            columnsSizePerRow.append("COALESCE(pg_column_size(").append(columnName).append("), 8)");
        }
        return columnsSizePerRow.toString();
    }

    static String partForTable(String partForColumnsInOneRow, String tableName) {
        return "SELECT reltuples::BIGINT, CASE WHEN reltuples < 10000 THEN (\n" +
                "  select sum(" + partForColumnsInOneRow + ") from " + tableName + ")\n" +
                "ELSE reltuples * (select avg(" + partForColumnsInOneRow + ") from " + tableName + " TABLESAMPLE SYSTEM (1))\n" +
                "END as size\n" +
                "FROM pg_class WHERE relname = '" + tableName + "'\n";
    }
}
