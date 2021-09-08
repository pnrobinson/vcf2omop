package org.monarchinitiative.omop.command;

import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.*;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import org.monarchinitiative.omop.analysis.Omopulator;
import org.monarchinitiative.omop.data.OmopEntry;
import org.monarchinitiative.omop.data.OmopMapParser;
import org.monarchinitiative.omop.except.Vcf2OmopRuntimeException;
import org.monarchinitiative.omop.stage.OmopStageFileParser;
import org.monarchinitiative.omop.stage.OmopStagedVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Generate table with all transcript-annotations linked to a given chromosomal change in the OMOP table
 */
@CommandLine.Command(name = "synonyms", mixinStandardHelpOptions = true, description = "Generate table with all transcript \"synonyms\"")
public class SynonymsCommand extends GenomicDataCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-p", "--prefix"}, description = "Outfile prefix")
    String prefix = "vcf2omop";

    private final static Logger logger = LoggerFactory.getLogger(Omopulator.class);
    private JannovarData jannovarData;
    private List<OmopEntry> entries;
    /**
     * Reference dictionary that is part of {@link #jannovarData}.
     */
    private ReferenceDictionary refDict;
    /**
     * Map of Chromosomes, used in the annotation.
     */
    private ImmutableMap<Integer, Chromosome> chromosomeMap;

    public SynonymsCommand() {
    }


    private final static String[] header = {"omop.id", "chrom", "pos", "ref", "alt", "gene.symbol", "hgvs.genomic", "hgvs.transcript", "hgvs.protein"};


    @Override
    public Integer call() {
        String outname = String.format("synonyms-%s-%s.tsv", this.prefix, this.assembly);
        String jannovarPath = getJannovarPath();
        try {
            this.jannovarData = new JannovarDataSerializer(jannovarPath).load();
            this.refDict = jannovarData.getRefDict();
        } catch (SerializationException se) {
            throw new Vcf2OmopRuntimeException(se.getMessage());
        }
        //OmopMapParser parser = new OmopMapParser(assembly);
        List<OmopStagedVariant> stagedVariantList = stagedVariantList(omopStageFilePath);

        File f;

        final VariantAnnotator annotator = new VariantAnnotator(this.refDict, chromosomeMap, new AnnotationBuilderOptions());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outname))) {
            writer.write(String.join("\t", header) + "\n");
            for (OmopStagedVariant entry : stagedVariantList) {
                String chrom = entry.chromToString();
                int omopId = entry.getOmopId();
                int pos = entry.getPos();
                String ref = entry.getRef();
                String alt = entry.getAlt();
                int chr = jannovarData.getRefDict().getContigNameToID().get(chrom);
                GenomeVariant genomeChange = new GenomeVariant(new GenomePosition(this.refDict, Strand.FWD, chr, pos, PositionType.ONE_BASED), ref, alt);
                VariantAnnotations annoList;
                try {
                    annoList=annotator.buildAnnotations(genomeChange);
                } catch (Exception e) {
                    System.err.println("[ERROR] Could not annotate " + genomeChange);
                    continue;
                }
                for (Annotation annot : annoList.getAnnotations()) {
                    try {
                        String symbol = annot.getGeneSymbol();
                        String genomicHgvs = annot.getGenomicNTChangeStr();
                        List<String> fields = new ArrayList<>();
                        fields.add(String.valueOf(omopId));
                        fields.add(chrom);
                        fields.add(String.valueOf(pos));
                        fields.add(ref);
                        fields.add(alt);
                        fields.add(symbol);
                        fields.add(genomicHgvs);
                        String hgvsTranscript = String.format("%s:%s",annot.getTranscript().getAccession(),annot.getCDSNTChangeStr());
                        fields.add(hgvsTranscript);
                        fields.add(annot.getProteinChangeStr());
                        writer.write(String.join("\t", fields) + "\n");
                    } catch (Exception e) {
                        System.err.printf("[ERROR] Could not annotate entry %s!\n", entry);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }


}
