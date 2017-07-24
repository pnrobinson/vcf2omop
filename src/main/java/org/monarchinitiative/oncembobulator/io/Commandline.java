package org.monarchinitiative.oncembobulator.io;

import org.apache.commons.cli.*;
import org.monarchinitiative.oncembobulator.command.Command;
import org.monarchinitiative.oncembobulator.command.DownloadOnkoKB;


import java.io.OutputStream;
import java.io.PrintWriter;

public class Commandline {

    private Command command=null;

    public Commandline(String args[]) {
        final CommandLineParser cmdLineGnuParser = new GnuParser();

        final Options gnuOptions = constructGnuOptions();
        org.apache.commons.cli.CommandLine commandLine;

        String mycommand = null;
        try
        {
            commandLine = cmdLineGnuParser.parse(gnuOptions, args);
            String category[] = commandLine.getArgs();
            if (category.length != 1) {
                System.out.println("command missing:");
                printUsage();
            } else {
                mycommand=category[0];

            }
            if (commandLine.getArgs().length<1) {
                printUsage();
                return;
            }
        }
        catch (ParseException parseException)  // checked exception
        {
            System.err.println(
                    "Encountered exception while parsing using GnuParser:\n"
                            + parseException.getMessage() );
        }
        if (mycommand.equals("download-oncokb")) {
            this.command = new DownloadOnkoKB();
        } else {
            System.out.println("Did not recognize command: "+ mycommand);
            printUsage();
        }
    }


    public Command getCommand() {
        return command;
    }

    /**
         * Construct and provide GNU-compatible Options.
         *
         * @return Options expected from command-line of GNU form.
         */
        public static Options constructGnuOptions()
        {
            final Options gnuOptions = new Options();
            gnuOptions.addOption("f", "fabian-directory", true, "FABIAN directory path")
                    .addOption("g", "gtexdir", true, "GTEx directory (default: \"data\" in PWD)")
                    .addOption("n", true, "Number of copies")
                    .addOption("c","cpe",true,"Symbol of core promoter element to be analyzed")
                    .addOption( OptionBuilder.withLongOpt( "cpe1" )
                            .withDescription( "core promoter element 1" )
                            .hasArg()
                            .create())
                    .addOption( OptionBuilder.withLongOpt( "cpe2" )
                            .withDescription( "core promoter element 2" )
                            .hasArg()
                            .create());
            return gnuOptions;
        }



    /**
     * Print usage information to provided OutputStream.
     */
    public static void printUsage()
    {
        final PrintWriter writer = new PrintWriter(System.out);
        final HelpFormatter usageFormatter = new HelpFormatter();
        final String applicationName="Oncembobulator";
        final Options options=constructGnuOptions();
        usageFormatter.printUsage(writer, 80, applicationName, options);
        writer.print("\t where command is one of download-oncokb, download-clinvar, parse.\n");
        writer.print("\t download-oncokb: Download the OnkoKB data to the data directory.\n");
        writer.print("\t download-clinvar: Download the ClinVar data to the data directory.\n");
        writer.print("\t parse: parse and discombobulate the mutations.\n");
        writer.close();
        System.exit(0);
    }

}
