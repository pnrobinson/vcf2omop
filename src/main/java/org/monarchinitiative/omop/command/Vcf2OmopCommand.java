package org.monarchinitiative.omop.command;


import org.monarchinitiative.omop.analysis.OmopAnnotatedTranscript;
import org.monarchinitiative.omop.analysis.OmopAnnotatedVariant;
import org.monarchinitiative.omop.analysis.Omopulator;
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
public class Vcf2OmopCommand extends GenomicDataCommand implements Callable<Integer>  {
    static final Logger logger = LoggerFactory.getLogger(Vcf2OmopCommand.class);

    enum GenomeDatabase {refseq, ensembl}

    @CommandLine.Option(names = {"--vcf"}, description ="path to VCF file", required = true)
    private String vcfPath;
    @CommandLine.Option(names = {"-p", "--prefix"}, description = "Outfile prefix")
    private String prefix = "vcf2omop";
    @CommandLine.Option(names={"--annot"}, description = "add transcript annotations via Jannovar")
    private boolean transcriptAnnotations = false;

    @Override
    public Integer call() throws IOException {
        logger.debug("Executing vcf2omop");
        List<OmopStagedVariant> stagedVariantList = stagedVariantList(omopStageFilePath);
        Omopulator ompopulate = new Omopulator(getJannovarPath(), vcfPath, assembly.name(), stagedVariantList, transcriptAnnotations);
        File vcfFile = new File(vcfPath);
        if (!vcfFile.isFile()) {
            throw new Vcf2OmopRuntimeException("Could not find VCF file at " + vcfFile.getAbsolutePath());
        }
        String basename = vcfFile.getName();
        String outname = prefix + "-" + basename;
        File outfile = new File(outname);
        ompopulate.annotateVcf(outfile);
        return 0;
    }

}
