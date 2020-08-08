package pl.edu.mimuw.dbaugmentor.copier;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class SqlScriptWriter {
    private PrintWriter sqlOutputFile;
    private final boolean enabled;

    public SqlScriptWriter() {
        enabled = false;
    }

    public SqlScriptWriter(String filePath) throws FileNotFoundException {
        enabled = true;
        sqlOutputFile = new PrintWriter(filePath);
    }

    public void write(String newLine) {
        if (enabled) {
            sqlOutputFile.println(newLine);
        }
    }

    public void end() {
        if (enabled) {
            sqlOutputFile.close();
        }
    }
}
