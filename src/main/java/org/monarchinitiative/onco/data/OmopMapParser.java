package org.monarchinitiative.onco.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OmopMapParser {

    private final List<OmopEntry> entries;

    public OmopMapParser() {
        String path = "src/main/resources/omopmap.csv";
        entries = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line= br.readLine()) != null) {
                //System.out.println(line);
                String [] fields = line.split("\t");
                OmopEntry entry = new OmopEntry(fields[0], Integer.parseInt(fields[1]), fields[2], fields[3], fields[4]);
                entries.add(entry);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<OmopEntry> getEntries() {
        return entries;
    }
}
