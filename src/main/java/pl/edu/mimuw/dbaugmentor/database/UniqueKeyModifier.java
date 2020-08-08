package pl.edu.mimuw.dbaugmentor.database;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.JDBCType;
import java.util.*;

import static java.sql.JDBCType.*;

public class UniqueKeyModifier {
    private int columnToModifyNumber;
    private Column columnToModify = null;
    private Object lastUsed = null;
    private final Set<ArrayList<Object>> existingValues = new HashSet<>();
    private final boolean optimizedSearch;
    private UniqueKeyChecker uniqueKeyChecker;

    protected UniqueKeyModifier(boolean optimizedSearch) {
        this.optimizedSearch = optimizedSearch;
    }

    public boolean hasColumnToModify() {
        return columnToModify != null;
    }

    protected void setColumnToModify(int columnNumber, Column column) {
        this.columnToModifyNumber = columnNumber;
        this.columnToModify = column;
    }

    protected void setUniqueKeyChecker(UniqueKeyChecker uniqueKeyChecker) {
        this.uniqueKeyChecker = uniqueKeyChecker;
    }

    private void updateLastUsed(Object value) {
        if (lastUsed == null) {
            lastUsed = value;
            return;
        }

        switch(columnToModify.getType()) {
            case SMALLINT:
                if ((Short) value > (Short) lastUsed) {
                    lastUsed = value;
                }
                break;
            case INTEGER:
                if ((Integer) value > (Integer) lastUsed) {
                    lastUsed = value;
                }
                break;
            case BIGINT:
                if ((Long) value > (Long) lastUsed) {
                    lastUsed = value;
                }
                break;
        }
    }

    protected void addKeyValue(ArrayList<Object> keyValue, boolean cache) {
        if (cache) {
            existingValues.add(keyValue);
        }
        updateLastUsed(keyValue.get(columnToModifyNumber));
    }

    public void makeKeyValueUnique(ArrayList<Object> keyValue, boolean cache) throws Exception {
        makeKeyValueUnique(keyValue, "", cache);
    }

    public void makeKeyValueUnique(ArrayList<Object> keyValue, String suffix, boolean cache) throws Exception {
        if (keyValue.get(columnToModifyNumber) == null && existingValues.contains(keyValue)) {
            keyValue.set(columnToModifyNumber, 0);
        }
        if (cache) {
            while (existingValues.contains(keyValue)) {
                makeValueOfColumnToModifyUnique(keyValue, suffix);
            }
        } else {
            if (!uniqueKeyChecker.isUniqueInDatabase(keyValue)) {
                makeValueOfColumnToModifyUnique(keyValue, suffix);
            }
        }
    }

    private void makeValueOfColumnToModifyUnique(ArrayList<Object> keyValue, String suffix) throws Exception {
        switch (columnToModify.getType()) {
            case CHAR:
            case VARCHAR:
                changeStringValue(keyValue, suffix);
                break;
            case TINYINT:
                keyValue.set(columnToModifyNumber, (Byte)keyValue.get(columnToModifyNumber) + 1);
                break;
            case REAL:
                keyValue.set(columnToModifyNumber, (Float)keyValue.get(columnToModifyNumber) + 1);
                break;
            case FLOAT:
            case DOUBLE:
                keyValue.set(columnToModifyNumber, (Double)keyValue.get(columnToModifyNumber) + 1);
                break;
            case DATE:
                ((Date) keyValue.get(columnToModifyNumber)).setTime(
                        ((Date) keyValue.get(columnToModifyNumber)).getTime() + 1);
                break;
            case DECIMAL:
            case NUMERIC:
                keyValue.set(columnToModifyNumber, ((BigDecimal)keyValue.get(columnToModifyNumber)).add(BigDecimal.ONE));
                break;
            case SMALLINT:
            case INTEGER:
            case BIGINT:
                changeKeyValueOfInteger(keyValue);
                break;
            default:
                throw new Exception("Unsupported data type " + columnToModify.getType() +
                        " while generating new primary key value");
        }
    }

    private void changeStringValue(ArrayList<Object> keyValue, String suffix) throws Exception {
        if (!suffix.isEmpty()) {
            String newValue = ((String) keyValue.get(columnToModifyNumber)).concat("#").concat(suffix);
            keyValue.set(columnToModifyNumber, newValue);
            if (newValue.length() == columnToModify.getSize()) {
                throw new Exception(
                        "Not enough space in column " + columnToModify.getName() + ", " +
                        "try again without \"Generate readable strings\" option if set");
            }
            return;
        }
        String curr = ((String) keyValue.get(columnToModifyNumber));
        char last = curr.charAt(curr.length() - 1);
        if (last + 1 < 128) {
            curr = curr.substring(0, curr.length() - 1);
            curr = curr.concat(String.valueOf((char) (last + 1)));
        } else {
            if (curr.length() == columnToModify.getSize()) {
                curr = "";
            }
            curr = curr.concat("!");
        }
        keyValue.set(columnToModifyNumber, curr);
    }

    private void changeKeyValueOfInteger(ArrayList<Object> keyValue) {
        if (optimizedSearch) {
            switch (columnToModify.getType()) {
                case SMALLINT:
                    lastUsed = (Short) lastUsed + 1;
                    break;
                case INTEGER:
                    lastUsed = (Integer) lastUsed + 1;
                    break;
                case BIGINT:
                    lastUsed = (Long) lastUsed + 1;
                    break;
            }
            keyValue.set(columnToModifyNumber, lastUsed);
        }
        else {
            switch (columnToModify.getType()) {
                case SMALLINT:
                    keyValue.set(columnToModifyNumber, (Short) keyValue.get(columnToModifyNumber) + 1);
                    break;
                case INTEGER:
                    keyValue.set(columnToModifyNumber, (Integer) keyValue.get(columnToModifyNumber) + 1);
                    break;
                case BIGINT:
                    keyValue.set(columnToModifyNumber, (Long) keyValue.get(columnToModifyNumber) + 1);
                    break;
            }
        }
    }

    protected boolean isTypeSupported(JDBCType type) {
        List<JDBCType> supportedTypes =
                List.of(CHAR, VARCHAR, SMALLINT, DECIMAL, NUMERIC, BIGINT, REAL, FLOAT, DOUBLE, DATE);
        return supportedTypes.contains(type);
    }
}
