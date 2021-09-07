package org.monarchinitiative.omop.stage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OmopStageFileParserTest {

    private final static String basename = "small_stage_genomic.csv";
    private static OmopStageFileParser parser;

    @BeforeAll
    private static void init() {
        String fname = Thread.currentThread().getContextClassLoader().getResource(basename).getPath();
        File f = new File(fname);
        parser = new OmopStageFileParser(f);
    }

    /** Check we can retrieve the stage file for JUnit. */
    @Test
    void checkConstruction() {
        assertNotNull(parser);
    }

    @Test
    void if_nine_entries_parsed_then_ok() {
        List<StagedVariant> variants = parser.getStagedVariantList();
        assertEquals(9, variants.size());
    }

    @Test
    void if_all_variants_are_hg19_then_ok() {
        for (StagedVariant var : parser.getStagedVariantList()) {
            assertEquals(Assembly.GRCh19, var.getAssembly());
        }
    }
}
