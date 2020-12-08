package org.monarchinitiative.omop.data;

import org.monarchinitiative.omop.except.Vcf2OmopRuntimeException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse the file from OMOP with the list of interesting variants. The format of this file is
 * <pre>
 *    chromosome,pos,ref,alt,concept_id,assembly
 *    1,68405,G,T,36740309,
 *   1,11188519,G,A,35979032,GRCh37
 *   1,16137994,C,T,35979132,GRCh38
 * (...)
 * </pre>
 * The file is CSV. We only use entries that have a matching genome assembly.
 * @author Peter N Robinson
 */
public class OmopMapParser {

    private final List<OmopEntry> entries;

    /** Must be one of GRCh37 or GRCh38. */
    private final String genomeAssembly;

    public OmopMapParser(String assembly) {
        this.genomeAssembly = assembly;
        String path = "src/main/resources/table_omop.csv";
        entries = new ArrayList<>();
        final int EXPECTED_FIELD_COUNT = 6;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            line = br.readLine(); // first line expected to be a header line
            if (! line.equals("chromosome,pos,ref,alt,concept_id,assembly")) {
                throw new Vcf2OmopRuntimeException("Unexpected format of OMOP mapping file: " + line);
            }
            while ((line= br.readLine()) != null) {
                String [] fields = line.split(",");
                if (fields.length != EXPECTED_FIELD_COUNT) {
                    continue; // some fields for build do not have an entry
                }
                String chrom = fields[0];
                int position = Integer.parseInt(fields[1]);
                String ref = fields[2];
                String alt = fields[3];
                int id = Integer.parseInt(fields[4]);
                String build = fields[5];
                if (build != null && build.equals(genomeAssembly)) {
                    entries.add(new OmopEntry(chrom, position, ref, alt, id));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("[INFO] We ingested %d OMOP-encoded variants for genome assembly %s.\n", entries.size(), this.genomeAssembly);
    }

    public List<OmopEntry> getEntries() {
        return entries;
    }
}
