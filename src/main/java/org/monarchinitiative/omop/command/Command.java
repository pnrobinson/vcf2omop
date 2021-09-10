package org.monarchinitiative.omop.command;

import picocli.CommandLine;

import java.util.concurrent.Callable;

public abstract class Command implements Callable<Integer> {
    /** The directory where we download Jannovar files and later load them. */
    @CommandLine.Option(names = {"-d", "--data"},
            scope = CommandLine.ScopeType.INHERIT,
            description = "location of download directory (default: ${DEFAULT-VALUE})")
    protected String downloadDir = "data";



}
