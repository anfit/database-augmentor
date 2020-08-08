package pl.edu.mimuw.dbaugmentor.database;

import java.sql.JDBCType;
import java.util.concurrent.atomic.AtomicReference;

public class Column {
    private JDBCType type;
    private String name;
    private int size;
    private boolean nullable;
    private boolean autoIncrement;
    private boolean isFkColumn;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void markAsFkColumn() {
        this.isFkColumn = true;
    }

    public boolean isForeignKeyColumn() {
        return isFkColumn;
    }

    public String toString() {
        String nullableString;
        if (nullable) {
            nullableString = "nullable";
        }
        else {
            nullableString = "not nullable";
        }
        AtomicReference<String> autoIncrementString = new AtomicReference<>("");
        if (autoIncrement) {
            autoIncrementString.set(", auto incremented");
        }
        return "Column " + name + ": type " + type + ", " + nullableString + autoIncrementString;
    }

    public JDBCType getType() {
        return type;
    }

    public void setType(JDBCType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(String nullable) {
        this.nullable = !nullable.equals("NO");
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(String autoIncrement) {
        this.autoIncrement = autoIncrement.equals("YES");
    }
}
