package org.monarchinitiative.onco;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import org.monarchinitiative.onco.command.Command;
import org.monarchinitiative.onco.command.CreateJannovarTranscriptFile;
import org.monarchinitiative.onco.command.DownloadCommand;
import org.monarchinitiative.onco.command.OmopulateCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Omopulator {
    static Logger logger = LoggerFactory.getLogger(Omopulator.class);
    @Parameter(names = {"-h", "--help"}, help = true, description = "display this help message")
    private boolean usageHelpRequested;


    public static void main(String args[]) {

        Omopulator oncembobulator = new Omopulator();
        Command jannovar = new CreateJannovarTranscriptFile();
        Command ompopulate = new OmopulateCommand();
        Command download = new DownloadCommand();
        JCommander jc = JCommander.newBuilder()
                .addObject(oncembobulator)
                .addCommand("jannovar", jannovar)
                .addCommand("download", download)
                .addCommand("omopulate", ompopulate)
                .build();
        try {
            jc.parse(args);
        } catch (ParameterException pe) {
            System.err.printf("[ERROR] Could not start chc2go: %s\n", pe.getMessage());
            System.exit(1);
        }
        if (oncembobulator.usageHelpRequested) {
            jc.usage();
            System.exit(0);
        }
        String command = jc.getParsedCommand();
        if (command == null) {
            System.err.println("\n[ERROR] no command passed");
            System.err.println("[ERROR] run java -jar omopulator.jar -h for help.\n");
            return;
        }
        Command myCommand = null;
        switch (command) {
            case "download":
                myCommand= download;
                break;
            case "jannovar":
                myCommand= jannovar;
                break;
            case "omopulate":
                myCommand= ompopulate;
                break;
            default:
                System.err.println("[ERROR] Did not recognize command: "+ command);
                jc.usage();
                System.exit(0);
        }
        myCommand.execute();
    }

    public Omopulator() {
    }


}
