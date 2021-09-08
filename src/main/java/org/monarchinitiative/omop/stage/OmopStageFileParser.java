package org.monarchinitiative.omop.stage;

import org.monarchinitiative.omop.except.Vcf2OmopRuntimeException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OmopStageFileParser {

    private final List<OmopStagedVariant> stagedVariantList;

    public OmopStageFileParser(File f) {
        stagedVariantList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            if (! line.startsWith("concept_id")) {
                throw new Vcf2OmopRuntimeException("Malformed header line (" + line
                        +") of stage file (we were expecting the first line to start with concept_id).");
            }
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                OmopStagedVariant var = OmopStagedVariant.fromLine(line);
                stagedVariantList.add(var);
            }
        } catch (IOException e) {
            throw new Vcf2OmopRuntimeException(e.getMessage());
        }
    }

    public List<OmopStagedVariant> getStagedVariantList() {
        return stagedVariantList;
    }
}
