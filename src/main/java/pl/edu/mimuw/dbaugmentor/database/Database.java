package pl.edu.mimuw.dbaugmentor.database;

import pl.edu.mimuw.dbaugmentor.copier.Entity;
import pl.edu.mimuw.dbaugmentor.copier.EntityCopier;
import pl.edu.mimuw.dbaugmentor.copier.SqlScriptWriter;
import pl.edu.mimuw.dbaugmentor.config.ApplicationProperties;
import pl.edu.mimuw.dbaugmentor.config.FKConfigEntry;

import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

public class Database {
    private Connection connection;
    private final HashMap<String, Table> tables = new HashMap<>();
    private final SqlScriptWriter sqlScriptWriter;
    private final ApplicationProperties applicationProperties;
    private final Logger logger;

    public Database(ApplicationProperties properties, Logger logger) throws FileNotFoundException {
        this.applicationProperties = properties;
        this.logger = logger;
        String sqlOutputFile = properties.getSqlOutputFile();
        if (sqlOutputFile == null || sqlOutputFile.isEmpty()) {
            this.sqlScriptWriter = new SqlScriptWriter();
        } else {
            this.sqlScriptWriter = new SqlScriptWriter(sqlOutputFile);
        }
    }

    public void connect(String url, String user, String password) throws SQLException {
        connection = DriverManager.getConnection(url, user, password);
        if (connection == null) {
            throw new SQLException("Failed to make a connection to " + url);
        }
        connection.setAutoCommit(false);
    }

    public boolean hasTable(String tableName) {
        return tables.containsKey(tableName);
    }

    public Table getTable(String tableName) {
        return tables.get(tableName);
    }

    public void readTables() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String[] types = {"TABLE"};
        ResultSet tables = metaData.getTables(null, null, "%", types);

        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            boolean ignore =
                    Arrays.stream(applicationProperties.getTablesToSkip())
                            .parallel().anyMatch(tableName::contains);
            if (!ignore) {
                Table table = new Table(applicationProperties.isOptimizingUniqueValueSearch());
                table.setName(tableName);
                this.tables.put(table.getName(), table);
            }
        }
    }

    public void readSchema() throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        for (Table table : this.tables.values()) {
            table.addColumnMetaData(metaData);
        }

//        for (Table table : this.tables.values()) {
//            Statement stmt = connection.createStatement();
//            ResultSet resultSet = stmt.executeQuery("SELECT * FROM " + table.getName());
//            table.addColumnSizes(resultSet.getMetaData());
//        }

        for (Table table : this.tables.values()) {
            table.readTableSchema(connection, this.tables, applicationProperties.getUniqueConstraintsToSkip());
        }
    }

    public void readData() throws Exception {
        int done = 0;
        int todo = tables.values().size();
        for (Table table: tables.values()) {
            logger.info(done + " / " + todo + " tables read, reading " + table.getName() + " in progress...");
            table.fillData(
                    connection, applicationProperties.isCaching(), logger, applicationProperties.isCachingUniqueKey());
            done++;
        }
        logger.info("all " + done + " tables read");
    }

    public void fillEntity(Entity entity) throws Exception {
        String query = entity.getFillDataSql();
        JDBCStatement jdbcStatement = new JDBCStatement(connection, query);
        int columnNo = 1;
        for (Column pkColumn : entity.getTable().getPkColumns()) {
            jdbcStatement.setParameter(
                    pkColumn.getType(), columnNo, entity.getOriginal().getColumnValue(pkColumn.getName()));
            columnNo++;
        }
        ResultSet resultSet = jdbcStatement.executeQuery();
        while (resultSet.next()) {
            for (Column column : entity.getTable().getNotCachedColumns()) {
                entity.setColumnValueFromResultSet(resultSet, column);
            }
        }
    }

    public void addFKReferences() {
        for (Table table: tables.values()) {
            table.addIncomingReferencesToAllEntities(tables);
        }
    }

    public void insertNewData(ArrayList<Entity> entities, boolean caching) throws Exception {
        int done = 0;
        int todo = entities.size();
        for (Entity entity: entities) {
            if (!caching) {
                fillEntity(entity);
                for (UniqueKey uniqueKey: entity.getTable().getUniqueKeys()) {
                    entity.fixUniqueKeyValue(
                            uniqueKey,
                            applicationProperties.isGeneratingReadableString(),
                            applicationProperties.isCachingUniqueKey());
                }
            }
            done++;
            if (done % 10000 == 0) {
                logger.info("Inserts " + done + " / " + todo);
            }

            JDBCStatement jdbcStmt = new JDBCStatement(connection, entity.getSqlInsert());

            entity.setInsertParameters(jdbcStmt);
            sqlScriptWriter.write(jdbcStmt.getSqlToExecute());
            jdbcStmt.execute();
        }
    }

    public void updateNewData(ArrayList<Update> updates) throws Exception {
        for (Update update: updates) {
            JDBCStatement jdbcStmt = new JDBCStatement(connection, update.getSql());
            update.setUpdateParameters(jdbcStmt);
            sqlScriptWriter.write(jdbcStmt.getSqlToExecute());
            jdbcStmt.execute();
        }
    }

    public void disconnect() throws SQLException {
        connection.close();
        sqlScriptWriter.end();
    }

    public void commitTransaction() throws SQLException {
        connection.commit();
    }

    public void setConstraintsAllDeferred() throws SQLException {
        JDBCStatement jdbcStmt = new JDBCStatement(connection, "SET CONSTRAINTS ALL DEFERRED");
        sqlScriptWriter.write(jdbcStmt.getSqlToExecute());
        jdbcStmt.execute();
    }

    public void addMissingFks(ArrayList<FKConfigEntry> fkConfigEntries) {
        for (FKConfigEntry fkConfigEntry : fkConfigEntries) {
            Table destinationTable = getTable(fkConfigEntry.getDestinationTableName());
            ArrayList<Column> destinationColumns =
                    destinationTable.columnNamesToColumns(fkConfigEntry.getDestinationColumnNames());
            Table sourceTable = getTable(fkConfigEntry.getSourceTableName());
            ArrayList<Column> sourceColumns =
                    sourceTable.columnNamesToColumns(fkConfigEntry.getSourceColumnNames());
            destinationTable.addReference(
                    fkConfigEntry.getSourceTableName(),
                    sourceColumns,
                    fkConfigEntry.getDestinationTableName(),
                    destinationColumns
            );
            sourceTable.addReference(
                    fkConfigEntry.getSourceTableName(),
                    sourceColumns,
                    fkConfigEntry.getDestinationTableName(),
                    destinationColumns
            );
        }
    }

    public void prepareFinalTransaction(EntityCopier entityCopier) throws Exception {
        sqlScriptWriter.write("BEGIN;");
        if (applicationProperties.isIgnoringFkConstraints()) {
            setConstraintsAllDeferred();
        }
        insertNewData(entityCopier.getEntitiesToInsert(), applicationProperties.isCaching());
        updateNewData(entityCopier.getUpdatesOnCopiedEntities());
        sqlScriptWriter.write("COMMIT;");
    }

    public void generateCachedColumnsSizeQuery(String filePath) throws FileNotFoundException {
        SqlScriptWriter sqlScriptWriter = new SqlScriptWriter(filePath);
        sqlScriptWriter.write(SizeQueryHelper.getPrefix());
        boolean first = true;
        for (Table table : tables.values()) {
            Set<String> columnNames = table.getCachedColumnNames();
            if (!columnNames.isEmpty()) {
                if (!first) {
                    sqlScriptWriter.write(SizeQueryHelper.getSeparator());
                } else {
                    first = false;
                }
                String columnsPart = SizeQueryHelper.partForColumnsInOneRow(columnNames);
                sqlScriptWriter.write(SizeQueryHelper.partForTable(columnsPart, table.getName()));
            }
        }
        sqlScriptWriter.write(SizeQueryHelper.getSuffix());
        sqlScriptWriter.end();
    }

    public void generateUniqueKeyColumnsSizeQuery(String filePath) throws FileNotFoundException {
        SqlScriptWriter sqlScriptWriter = new SqlScriptWriter(filePath);
        sqlScriptWriter.write(SizeQueryHelper.getPrefix());
        boolean first = true;
        for (Table table : tables.values()) {
            ArrayList<String> modifiableUniqueKeyColumns = table.getModifiableUniqueKeyColumns();
            if (!modifiableUniqueKeyColumns.isEmpty()) {
                if (!first) {
                    sqlScriptWriter.write(SizeQueryHelper.getSeparator());
                } else {
                    first = false;
                }
                String columnsPart = SizeQueryHelper.partForColumnsInOneRow(modifiableUniqueKeyColumns);
                sqlScriptWriter.write(SizeQueryHelper.partForTable(columnsPart, table.getName()));
            }
        }
        sqlScriptWriter.write(SizeQueryHelper.getSuffix());
        sqlScriptWriter.end();

    }
}
