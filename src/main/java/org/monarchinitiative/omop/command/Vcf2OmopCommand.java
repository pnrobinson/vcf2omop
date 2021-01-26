package org.monarchinitiative.omop.command;


import org.monarchinitiative.omop.analysis.OmopAnnotatedTranscript;
import org.monarchinitiative.omop.analysis.OmopAnnotatedVariant;
import org.monarchinitiative.omop.analysis.Ompopulate;
import org.monarchinitiative.omop.except.Vcf2OmopRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "vcf2omop",  mixinStandardHelpOptions = true, description = "extract OMOP-annotated vars from VCF")
public class Vcf2OmopCommand implements Callable<Integer>  {
    static final Logger logger = LoggerFactory.getLogger(Vcf2OmopCommand.class);
    @CommandLine.Option(names = {"--vcf"}, description ="path to VCF file", required = true)
    private String vcfPath;
    @CommandLine.Option(names = {"-j", "--jannovar"}, description = "path to Jannovar transcript file")
    private String jannovarPath=null;
    @CommandLine.Option(names = {"-a", "--assembly"}, description = "genome assembly (hg19,hg38")
    private String assembly="GRCh38";
    @CommandLine.Option(names = {"--all"}, description = "Show all affected transcripts (default: ${DEFAULT-VALUE})")
    boolean showAll = false;
    @CommandLine.Option(names = {"-d", "--data"}, description = "location of download directory (default: ${DEFAULT-VALUE})")
    private String downloadDir = "data";
    @CommandLine.Option(names = {"-p", "--prefix"}, description = "Outfile prefix")
    String prefix = "vcf2omop";


    private String getJannovarPath() {
        File f;
        if (jannovarPath != null) {
            f = new File(jannovarPath); // user specified the path to the Jannovar ser file
        } else {
            if (assembly.equalsIgnoreCase("GRCh38") || assembly.equalsIgnoreCase("hg38")) {
                f = new File(downloadDir + File.separator + "hg38_refseq_curated.ser");
            } else if (assembly.equalsIgnoreCase("GRCh37") || assembly.equalsIgnoreCase("hg19")) {
                f = new File(downloadDir + File.separator + "hg19_refseq.ser");
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

    @Override
    public Integer call() {
        logger.debug("Executing vcf2omop");
        Ompopulate ompopulate = new Ompopulate(getJannovarPath(), vcfPath, assembly, showAll);
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
        if (annotations.isEmpty()) {
            System.out.println("[INFO] No annotations found");
        }
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
