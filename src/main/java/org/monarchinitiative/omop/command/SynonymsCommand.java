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
import org.monarchinitiative.omop.analysis.OmopAnnotatedVariant;
import org.monarchinitiative.omop.analysis.Ompopulate;
import org.monarchinitiative.omop.data.OmopEntry;
import org.monarchinitiative.omop.data.OmopMapParser;
import org.monarchinitiative.omop.except.Vcf2OmopRuntimeException;
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
public class SynonymsCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-j", "--jannovar"}, description = "path to Jannovar transcript file")
    private String jannovarPath = null;
    @CommandLine.Option(names = {"-a", "--assembly"}, description = "genome assembly (hg19,hg38")
    private String assembly = "GRCh38";
    @CommandLine.Option(names = {"-d", "--data"}, description = "location of download directory (default: ${DEFAULT-VALUE})")
    private String downloadDir = "data";
    @CommandLine.Option(names = {"-p", "--prefix"}, description = "Outfile prefix")
    String prefix = "vcf2omop";


    private final static Logger logger = LoggerFactory.getLogger(Ompopulate.class);
    private final JannovarData jannovarData;
    private final List<OmopEntry> entries;
    /**
     * Reference dictionary that is part of {@link #jannovarData}.
     */
    private final ReferenceDictionary refDict;
    /**
     * Map of Chromosomes, used in the annotation.
     */
    private final ImmutableMap<Integer, Chromosome> chromosomeMap;

    public SynonymsCommand() {
        String jannovarPath = getJannovarPath();
        try {
            this.jannovarData = new JannovarDataSerializer(jannovarPath).load();
        } catch (SerializationException se) {
            throw new RuntimeException(se.getMessage());
        }
        this.refDict = jannovarData.getRefDict();
        this.chromosomeMap = jannovarData.getChromosomes();
        OmopMapParser parser = new OmopMapParser(assembly);
        this.entries = parser.getEntries();
    }

    private String getJannovarPath() {
        File f;
        if (jannovarPath != null) {
            f = new File(jannovarPath); // user specified the path to the Jannovar ser file
        } else {
            if (assembly.equalsIgnoreCase("GRCh38") || assembly.equalsIgnoreCase("hg38")) {
                f = new File(downloadDir + File.separator + "hg38_refseq_curated.ser");
            } else if (assembly.equalsIgnoreCase("GRCh37") || assembly.equalsIgnoreCase("hg19")) {
                f = new File(downloadDir + File.separator + "hg_refseq.ser");
            } else {
                throw new Vcf2OmopRuntimeException("Did not recognize assembly: " + assembly
                        + ", valid values include hg19,hg38");
            }
        }
        if (! f.exists()) {
            throw new Vcf2OmopRuntimeException("Could not find Jannovar file at " + f.getAbsolutePath());
        }
        return f.getAbsolutePath();
    }

    private final static String[] header = {"omop.id", "chrom", "pos", "ref", "alt", "gene.symbol", "hgvs.genomic", "hgvs.transcript", "hgvs.protein"};


    @Override
    public Integer call() throws Exception {
        String outname = String.format("synonyms-%s-%s.tsv", this.prefix, this.assembly);
        final VariantAnnotator annotator = new VariantAnnotator(this.refDict, chromosomeMap, new AnnotationBuilderOptions());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outname))) {
            writer.write(String.join("\t", header) + "\n");
            for (OmopEntry entry : this.entries) {
                String chrom = entry.getChromosome();
                int omopId = entry.getOmopId();
                int pos = entry.getPosition();
                String ref = entry.getRef();
                String alt = entry.getAlt();
                int chr = jannovarData.getRefDict().getContigNameToID().get(chrom);
                GenomeVariant genomeChange = new GenomeVariant(new GenomePosition(this.refDict, Strand.FWD, chr, pos, PositionType.ONE_BASED), ref, alt);
                VariantAnnotations annoList;
                try {
                    annoList=annotator.buildAnnotations(genomeChange);
                } catch (Exception e) {
                    e.printStackTrace();
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
                        fields.add(annot.getCDSNTChangeStr());
                        fields.add(annot.getProteinChangeStr());
                        writer.write(String.join("\t", fields) + "\n");
                    } catch (Exception e) {
                        System.err.printf("[ERROR] Could not annotate entry %s!\n", entry.toString());
                        e.printStackTrace(System.err);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return 0;
    }


}
