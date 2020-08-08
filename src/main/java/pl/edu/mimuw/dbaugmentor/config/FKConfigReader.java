package pl.edu.mimuw.dbaugmentor.config;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FKConfigReader {
    @SuppressWarnings("unchecked")
    public static ArrayList<FKConfigEntry> readConfig(String filePath) throws IOException, ParseException {
        ArrayList<FKConfigEntry> entries = new ArrayList<>();
        JSONParser parser = new JSONParser();
        JSONArray jsonEntries = (JSONArray) parser.parse(new FileReader(filePath));

        for (JSONObject jsonEntry : (Iterable<JSONObject>) jsonEntries) {
            FKConfigEntry entry = new FKConfigEntry();
            for (String sourceColumn: (Iterable<String>) jsonEntry.get("sourceColumns")) {
                entry.addSourceColumnName(sourceColumn);
            }
            entry.setSourceTableName(jsonEntry.get("sourceTable").toString());
            for (String destinationColumn: (Iterable<String>) jsonEntry.get("destinationColumns")) {
                entry.addDestinationColumnName(destinationColumn);
            }
            entry.setDestinationTableName(jsonEntry.get("destinationTable").toString());
            entries.add(entry);
        }
        return entries;
    }
}