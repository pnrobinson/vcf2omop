package org.monarchinitiative.omop.command;


import org.monarchinitiative.omop.analysis.OmopAnnotatedTranscript;
import org.monarchinitiative.omop.analysis.OmopAnnotatedVariant;
import org.monarchinitiative.omop.analysis.Ompopulate;
import org.monarchinitiative.omop.except.Vcf2OmopRuntimeException;
import org.monarchinitiative.omop.stage.Assembly;
import org.monarchinitiative.omop.stage.OmopStageFileParser;
import org.monarchinitiative.omop.stage.OmopStagedVariant;
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

    enum GenomeDatabase {refseq, ensembl}

    @CommandLine.Option(names = {"--vcf"}, description ="path to VCF file", required = true)
    private String vcfPath;
    @CommandLine.Option(names = {"-s", "--stage"}, description = "path to OMOP stage file", required = true)
    private String omopStageFilePath;
    @CommandLine.Option(names = "--database", description = "database: ${COMPLETION-CANDIDATES}")
    private GenomeDatabase genomeDatabase = GenomeDatabase.ensembl;
    @CommandLine.Option(names = {"-a", "--assembly"}, description = "genome assembly: ${COMPLETION-CANDIDATES}, default ${DEFAULT_VALUE}")
    private Assembly assembly=Assembly.GRCh19;
    @CommandLine.Option(names = {"--all"}, description = "Show all affected transcripts (default: ${DEFAULT-VALUE})")
    boolean showAll = false;
    @CommandLine.Option(names = {"-d", "--data"}, description = "location of download directory (default: ${DEFAULT-VALUE})")
    private String downloadDir = "data";
    @CommandLine.Option(names = {"-p", "--prefix"}, description = "Outfile prefix")
    String prefix = "vcf2omop";


    private String getJannovarPath() {
        File f;
        switch (assembly) {
            case hg19:
            case GRCh19:
                if (genomeDatabase.equals(GenomeDatabase.refseq)) {
                    f =  new File(downloadDir + File.separator + "refseq_curated_105_hg19.ser");
                } else if (genomeDatabase.equals(GenomeDatabase.ensembl)) {
                    f =  new File(downloadDir + File.separator + "ensembl_87_hg19.ser");
                } else {
                    throw new Vcf2OmopRuntimeException("Could not identify databasae " + genomeDatabase);
                }
                break;
            case hg38:
            case GRCh38:
                if (genomeDatabase.equals(GenomeDatabase.refseq)) {
                    f =  new File(downloadDir + File.separator + "refseq_curated_109_hg38.ser");
                } else if (genomeDatabase.equals(GenomeDatabase.ensembl)) {
                    f =  new File(downloadDir + File.separator + "ensembl_91_hg38.ser");
                } else {
                    throw new Vcf2OmopRuntimeException("Could not identify databasae " + genomeDatabase);
                }
            default:
                throw new Vcf2OmopRuntimeException("Could not identify assembly " + assembly.toString());
        }
        if (! f.exists()) {
            throw new Vcf2OmopRuntimeException("Could not find Jannovar file at " + f.getAbsolutePath());
        }
        return f.getAbsolutePath();
    }

    @Override
    public Integer call() throws IOException {
        logger.debug("Executing vcf2omop");
        File f = new File(omopStageFilePath);
        if (! f.isFile()) {
            throw new Vcf2OmopRuntimeException("Could not find OMOP stage file at " + f.getAbsolutePath());
        }
        OmopStageFileParser omopStageFileParser = new OmopStageFileParser(f);
        List<OmopStagedVariant> stagedVariantList = omopStageFileParser.getStagedVariantList();
        Ompopulate ompopulate = new Ompopulate(getJannovarPath(), vcfPath, assembly.name(), stagedVariantList, showAll);
        File vcfFile = new File(vcfPath);
        if (!vcfFile.isFile()) {
            throw new Vcf2OmopRuntimeException("Could not find VCF file at " + vcfFile.getAbsolutePath());
        }
        String basename = vcfFile.getName();
        String outname = prefix + "-" + basename;
        File outfile = new File(outname);
        ompopulate.annotateVcf(outfile);
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
