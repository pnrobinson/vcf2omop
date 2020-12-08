package org.monarchinitiative.onco.command;


import org.monarchinitiative.onco.analysis.OmopAnnotatedTranscript;
import org.monarchinitiative.onco.analysis.OmopAnnotatedVariant;
import org.monarchinitiative.onco.analysis.Ompopulate;
import org.monarchinitiative.onco.data.Gene2ClinvarMutations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "omopulate",  mixinStandardHelpOptions = true, description = "omopulate")
public class OmopulateCommand implements Callable<Integer>  {
    static final Logger logger = LoggerFactory.getLogger(OmopulateCommand.class);
    @CommandLine.Option(names = {"--vcf"}, description ="path to VCF file", required = true)
    private String vcfPath;
    @CommandLine.Option(names = {"-j", "--jannovar"}, description = "path to Jannovar transcript file")
    private String jannovarPath;
    @CommandLine.Option(names = {"-a", "--assembly"}, description = "genome assembly (hg19,hg38")
    private String assembly="GRCh38";
    @CommandLine.Option(names = {"--all"}, description = "Show all affected transcripts (default: ${DEFAULT-VALUE})")
    boolean showAll = false;
    @CommandLine.Option(names = {"-p", "--prefix"}, description = "Outfile prefix")
    String prefix = "vcf2omop";


    private Map<String,Gene2ClinvarMutations> gene2mutMap=null;

    @Override
    public Integer call() throws Exception {
        logger.debug("Executing vcf2omop");
        Ompopulate ompopulate = new Ompopulate(jannovarPath, vcfPath, assembly, showAll);
        List<OmopAnnotatedVariant> annotations = ompopulate.getVariantAnnotations();
        dumpToShell(annotations);
        writeToFile(annotations);
        return 0;
    }

    /**
     * Show relevant variants and annotations on the shell
     * @param annotations
     */
    public void dumpToShell(List<OmopAnnotatedVariant> annotations) {
        for (OmopAnnotatedVariant ovar : annotations) {
            if (showAll) {
                for (OmopAnnotatedTranscript otran : ovar.getTranscriptAnnotations()) {
                    System.out.println(otran.getTsvLine());
                }
            } else {
                System.out.println(ovar.getHighestImpactAnnotation().getTsvLine());
            }
        }
    }

    /**
     * Write relevant variants and annotations to file
     * @param annotations
     */
    public void writeToFile(List<OmopAnnotatedVariant> annotations) {
        String fname = String.format("%s.tsv", this.prefix);
        if (showAll) {
            fname = String.format("%s-all.tsv", this.prefix);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fname))) {
            for (OmopAnnotatedVariant ovar : annotations) {
                if (showAll) {
                    for (OmopAnnotatedTranscript otran : ovar.getTranscriptAnnotations()) {
                        writer.write(otran.getTsvLine() + "\n");
                    }
                } else {
                    writer.write(ovar.getHighestImpactAnnotation().getTsvLine() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
