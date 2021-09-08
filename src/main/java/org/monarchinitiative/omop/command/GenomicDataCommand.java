package org.monarchinitiative.omop.command;

import org.monarchinitiative.omop.except.Vcf2OmopRuntimeException;
import org.monarchinitiative.omop.stage.Assembly;
import org.monarchinitiative.omop.stage.OmopStageFileParser;
import org.monarchinitiative.omop.stage.OmopStagedVariant;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

/**
 * Common superclass for the synonyms and vcf2omop commands
 * @author Peter N Robinson
 */
public abstract class GenomicDataCommand extends Command {
    @CommandLine.Option(names = {"-a", "--assembly"},
            scope = CommandLine.ScopeType.INHERIT,
            description = "genome assembly: ${COMPLETION-CANDIDATES}, default ${DEFAULT_VALUE}")
    protected Assembly assembly = Assembly.GRCh19;

    @CommandLine.Option(names = "--database",
            scope = CommandLine.ScopeType.INHERIT,
            description = "database: ${COMPLETION-CANDIDATES}")
    protected Vcf2OmopCommand.GenomeDatabase genomeDatabase = Vcf2OmopCommand.GenomeDatabase.ensembl;

    @CommandLine.Option(names = {"-j", "--jannovar"},
            scope = CommandLine.ScopeType.INHERIT,
            description = "path to Jannovar transcript file")
    protected String jannovarPath = null;

    @CommandLine.Option(names = {"-s", "--stage"},
            scope = CommandLine.ScopeType.INHERIT,
            description = "path to OMOP stage file", required = true)
    protected String omopStageFilePath;



    protected String getJannovarPath() {
        File f;
        switch (assembly) {
            case hg19:
            case GRCh19:
                if (genomeDatabase.equals(Vcf2OmopCommand.GenomeDatabase.refseq)) {
                    f =  new File(downloadDir + File.separator + "refseq_curated_105_hg19.ser");
                } else if (genomeDatabase.equals(Vcf2OmopCommand.GenomeDatabase.ensembl)) {
                    f =  new File(downloadDir + File.separator + "ensembl_87_hg19.ser");
                } else {
                    throw new Vcf2OmopRuntimeException("Could not identify databasae " + genomeDatabase);
                }
                break;
            case hg38:
            case GRCh38:
                if (genomeDatabase.equals(Vcf2OmopCommand.GenomeDatabase.refseq)) {
                    f =  new File(downloadDir + File.separator + "refseq_curated_109_hg38.ser");
                } else if (genomeDatabase.equals(Vcf2OmopCommand.GenomeDatabase.ensembl)) {
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

    protected List<OmopStagedVariant> stagedVariantList(String omopStageFilePath) {
        File f = new File(omopStageFilePath);
        if (!f.isFile()) {
            throw new Vcf2OmopRuntimeException("Could not find OMOP stage file at " + f.getAbsolutePath());
        }
        OmopStageFileParser omopStageFileParser = new OmopStageFileParser(f);
        return omopStageFileParser.getStagedVariantList();
    }
}
