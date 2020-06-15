package org.monarchinitiative.onco.data;

import org.monarchinitiative.onco.analysis.CivicVariant;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CivicParser {

    List<CivicVariant> varlist;

    public CivicParser(String path) {
        String line;
        varlist = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            line = br.readLine();//header
            if (! line.startsWith("variant_id")) {
                throw new RuntimeException("Bad format of CIVIC file");
            }
            while ((line=br.readLine()) != null) {
                System.out.println(line);
                String [] fields = line.split("\t");
                if (fields.length < 12) {
                    System.err.println("[WARNING] Malformed civic line: " + line);
                    continue;
                }
                try {
                    CivicVariant var = CivicVariant.fromArray(fields);
                    varlist.add(var);
                } catch (Exception e) {
                    System.err.println("[WARNING] " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<CivicVariant> getVarlist() {
        return varlist;
    }
}
