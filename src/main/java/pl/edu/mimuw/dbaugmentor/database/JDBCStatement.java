package pl.edu.mimuw.dbaugmentor.database;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;

public class JDBCStatement {
    private final PreparedStatement preparedStatement;

    public JDBCStatement(Connection connection, String sql) throws SQLException {
         preparedStatement = connection.prepareStatement(sql);
    }

    public void setParameter(JDBCType type, int columnNumber, Object value) throws Exception {
        if (value == null) {
            preparedStatement.setNull(columnNumber, type.getVendorTypeNumber());
            return;
        }
        switch (type) {
            case BIT:
            case BOOLEAN:
                preparedStatement.setBoolean(columnNumber, (Boolean) value);
                return;
            case CHAR:
            case VARCHAR:
                preparedStatement.setString(columnNumber, (String) value);
                return;
            case LONGVARCHAR:
                preparedStatement.setAsciiStream(columnNumber, (InputStream) value);
                return;
            case TINYINT:
                preparedStatement.setByte(columnNumber, (Byte) value);
                return;
            case SMALLINT:
                preparedStatement.setShort(columnNumber, (Short) value);
                return;
            case INTEGER:
                preparedStatement.setInt(columnNumber, (Integer) value);
                return;
            case DECIMAL:
            case NUMERIC:
                preparedStatement.setBigDecimal(columnNumber, (BigDecimal) value);
                return;
            case BIGINT:
                preparedStatement.setLong(columnNumber, (Long) value);
                return;
            case REAL:
                preparedStatement.setFloat(columnNumber, (Float) value);
                return;
            case FLOAT:
            case DOUBLE:
                preparedStatement.setDouble(columnNumber, (Double) value);
                return;
            case DATE:
                preparedStatement.setDate(columnNumber, (Date) value);
                return;
            case TIME:
                preparedStatement.setTime(columnNumber, (Time) value);
                return;
            case TIMESTAMP:
            case TIMESTAMP_WITH_TIMEZONE:
                preparedStatement.setTimestamp(columnNumber, (Timestamp) value);
                return;
            case BINARY:
            case VARBINARY:
                preparedStatement.setBytes(columnNumber, (byte[]) value);
                return;
            case LONGVARBINARY:
                preparedStatement.setBinaryStream(columnNumber, (InputStream) value);
                return;
            case ARRAY:
                preparedStatement.setArray(columnNumber, (Array) value);
                return;
            default:
                throw new Exception("JDBCStatement parameter not set, unsupported data type " + type);
        }
    }

    @Override
    public String toString() {
        return "JDBCStatement{" + preparedStatement + '}';
    }

    public void execute() throws SQLException {
        preparedStatement.execute();
    }

    public ResultSet executeQuery() throws SQLException {
        return preparedStatement.executeQuery();
    }

    public String getSqlToExecute() {
        return preparedStatement.toString().concat(";");
    }
}
