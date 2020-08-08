package pl.edu.mimuw.dbaugmentor.config;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class ApplicationProperties {
    private String url;
    private String user;
    private String password;
    private boolean caching;
    private boolean ignoreFkConstraints;
    private String tableName;
    private int multiplier;
    private String missingFkFile;
    private String sqlOutputFile;
    private ArrayList<String> uniqueConstraintsToSkip;
    private String[] tablesToSkip;
    private boolean commit;
    private boolean optimizeUniqueValueSearch;
    private boolean readableStrings;
    private boolean uniqueKeyCache;
    private boolean guiEnabled;

    public ApplicationProperties() {
        try {
            Properties prop = new Properties();
            String propFileName = "config.properties";

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("Missing property file " + propFileName);
            }

            url = prop.getProperty("url", "");
            user = prop.getProperty("user", "");
            password = prop.getProperty("password", "");
            caching = readBooleanProperty(prop, "caching");
            ignoreFkConstraints = readBooleanProperty(prop, "ignore.fk.constraints");
            tableName = prop.getProperty("table.name", "");
            multiplier = Integer.parseInt(prop.getProperty("multiplier", "2"));
            missingFkFile = prop.getProperty("missing.fk.file", "");
            sqlOutputFile = prop.getProperty("sql.output.file", "");
            uniqueConstraintsToSkip =
                    new ArrayList<>(Arrays.asList(
                            readStringListProperty(prop, "unique.constraints.to.skip")));
            tablesToSkip = readStringListProperty(prop, "tables.to.skip");
            commit = readBooleanProperty(prop, "commit");
            optimizeUniqueValueSearch = readBooleanProperty(prop, "optimize.id.search");
            readableStrings = readBooleanProperty(prop, "readable.strings");
            uniqueKeyCache = readBooleanProperty(prop, "unique.key.cache");
            guiEnabled = readBooleanProperty(prop, "enable.gui");

            inputStream.close();
        } catch (Exception e) {
            System.err.println("Error while reading properties: " + e);
        }
    }

    private String[] readStringListProperty(Properties prop, String propertyName) {
        String propertyValueStr = prop.getProperty(propertyName, "");
        if (propertyValueStr.isEmpty()) {
            return new String[0];
        }
        return propertyValueStr.split(",");
    }

    private boolean readBooleanProperty(Properties prop, String propertyName) throws Exception {
        String propValue = prop.getProperty(propertyName, "false");
        switch(propValue) {
            case "true":
                return true;
            case "false":
                return false;
            default:
                throw new Exception(
                        "Property " + propertyName + " must have value either true or false, not " + propValue);
        }

    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean isCaching() {
        return caching;
    }

    public boolean isIgnoringFkConstraints() {
        return ignoreFkConstraints;
    }

    public boolean isGeneratingReadableString() {
        return readableStrings;
    }

    public String getTableName() {
        return tableName;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public String getMissingFkFile() {
        return missingFkFile;
    }

    public String getSqlOutputFile() {
        return sqlOutputFile;
    }

    public ArrayList<String> getUniqueConstraintsToSkip() {
        return this.uniqueConstraintsToSkip;
    }

    public String[] getTablesToSkip() {
        return tablesToSkip;
    }

    public boolean isCachingUniqueKey() {
        return uniqueKeyCache;
    }

    public boolean isGuiEnabled() {
        return guiEnabled;
    }

    public boolean isCommit() {
        return commit;
    }

    public boolean isOptimizingUniqueValueSearch() {
        return optimizeUniqueValueSearch;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCaching(boolean caching) {
        this.caching = caching;
    }

    public void setIgnoreFkConstraints(boolean ignoreFkConstraints) {
        this.ignoreFkConstraints = ignoreFkConstraints;
    }

    public void setReadableStrings(boolean readableStrings) {
        this.readableStrings = readableStrings;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public void setMissingFkFile(String missingFkFile) {
        this.missingFkFile = missingFkFile;
    }

    public void setSqlOutputFile(String sqlOutputFile) {
        this.sqlOutputFile = sqlOutputFile;
    }

    public void setUniqueConstraintsToSkip(ArrayList<String> uniqueConstraintsToSkip) {
        this.uniqueConstraintsToSkip = uniqueConstraintsToSkip;
    }

    public void setCommit(boolean commit) {
        this.commit = commit;
    }

    public void setOptimizeUniqueValueSearch(boolean value) {
        this.optimizeUniqueValueSearch = value;
    }
}
