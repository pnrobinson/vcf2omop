package org.monarchinitiative.omop.command;

import org.monarchinitiative.omop.except.Vcf2OmopRuntimeException;
import org.monarchinitiative.omop.stage.Assembly;
import org.monarchinitiative.omop.stage.OmopStageFileParser;
import org.monarchinitiative.omop.stage.OmopStagedVariant;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class Command implements Callable<Integer> {
    /** The directory where we download Jannovar files and later load them. */
    @CommandLine.Option(names = {"-d", "--data"},
            scope = CommandLine.ScopeType.INHERIT,
            description = "location of download directory (default: ${DEFAULT-VALUE})")
    protected String downloadDir = "data";



}
