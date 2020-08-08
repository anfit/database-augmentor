package pl.edu.mimuw.dbaugmentor;

import pl.edu.mimuw.dbaugmentor.config.ApplicationProperties;
import pl.edu.mimuw.dbaugmentor.config.FKConfigEntry;
import pl.edu.mimuw.dbaugmentor.config.FKConfigReader;
import pl.edu.mimuw.dbaugmentor.copier.EntityCopier;
import pl.edu.mimuw.dbaugmentor.database.Database;
import pl.edu.mimuw.dbaugmentor.messages.*;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Backend {
    private final ApplicationProperties properties;
    private final Database database;
    private final Queue<Message> msgQueue;
    private ArrayList<FKConfigEntry> fkConfigEntries;
    private final Logger logger;

    public Backend(ApplicationProperties applicationProperties, Queue<Message>msgQueue) throws FileNotFoundException {
        properties = applicationProperties;
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setLevel(Level.INFO);
        database = new Database(properties, logger);
        this.msgQueue = msgQueue;
    }

    private void sendStatusMessage(Status status) {
        sendMessage(new StatusMsg(status));
    }

    private void sendMessage(Message message) {
        if (msgQueue != null) {
            synchronized (msgQueue) {
                msgQueue.add(message);
                msgQueue.notify();
            }
        } else {
            logger.info(message.getTextOfMessage());
        }
    }

    public void runValidationWithoutGui() throws Exception {
        ValidationResponse response = validate();
        if (!response.isSuccess()) {
            throw new Exception(response.getErrorMsg());
        }
    }

    public void runValidationWithGui() {
        ValidationResponse response = validate();
        sendMessage(response);
    }

    private ValidationResponse validate() {
        if (!properties.isGeneratingReadableString() && properties.isCachingUniqueKey()) {
            return new ValidationResponse(ValidationStatus.CACHE_READABLE_STRINGS);
        }
        if (!properties.isCachingUniqueKey() && !properties.isOptimizingUniqueValueSearch()) {
            return new ValidationResponse(ValidationStatus.CACHE_OPTIMIZE_UNIQUE_KEY);
        }
        try {
            database.connect(properties.getUrl(), properties.getUser(), properties.getPassword());
        } catch (SQLException e) {
            return new ValidationResponse(
                            ValidationStatus.DATABASE, "Failed to connect to database: " + e.getMessage());
        }

        try {
            database.readTables();
        } catch (SQLException e) {
            return new ValidationResponse(
                    ValidationStatus.DATABASE, "Database error: " + e.getMessage());
        }

        if (!database.hasTable(properties.getTableName())) {
            return new ValidationResponse(ValidationStatus.TABLE);
        }

        String missingFkFile = properties.getMissingFkFile();
        if (!missingFkFile.isEmpty()) {
            try {
                fkConfigEntries = FKConfigReader.readConfig(missingFkFile);
            } catch (FileNotFoundException e) {
                return new ValidationResponse(ValidationStatus.NO_MISSING_FK_FILE);
            } catch (Exception e) {
                return new ValidationResponse(ValidationStatus.MISSING_FK_INVALID);
            }
        }

        return new ValidationResponse(ValidationStatus.OK);
    }

    public void runCopyProcess() {
        try {
            sendStatusMessage(Status.CONNECT);
            database.readSchema();
            sendStatusMessage(Status.SCHEMA);

            if (!properties.getMissingFkFile().isEmpty()) {
                database.addMissingFks(fkConfigEntries);
            }

            // generates scripts to estimate needed memory
//            database.generateCachedColumnsSizeQuery("cachedColumns.sql");
//            database.generateUniqueKeyColumnsSizeQuery("uniqueKeyColumns.sql");

            database.readData();
            sendStatusMessage(Status.DATA);
            database.addFKReferences();
            sendStatusMessage(Status.REFERENCES);

            EntityCopier entityCopier = new EntityCopier(database, properties);
            entityCopier.copyEntities(logger);
            sendStatusMessage(Status.COPYING);

            database.prepareFinalTransaction(entityCopier);
            sendStatusMessage(Status.TRANSACTION);

            if (properties.isCommit()) {
                database.commitTransaction();
                sendStatusMessage(Status.COMMIT);
            }
            database.disconnect();
            logger.info("ALL DONE");

            if (msgQueue != null) {
                synchronized (msgQueue) {
                    msgQueue.notify();
                }
            }
        } catch (Exception e) {
            sendMessage(new ErrorMsg(e.getMessage()));
            e.printStackTrace();
        }
    }
}
