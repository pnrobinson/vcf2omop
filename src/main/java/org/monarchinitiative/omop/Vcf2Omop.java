package org.monarchinitiative.omop;

import org.monarchinitiative.omop.command.DownloadCommand;
import org.monarchinitiative.omop.command.SynonymsCommand;
import org.monarchinitiative.omop.command.Vcf2OmopCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * @author Peter N Robinson
 */
@CommandLine.Command(name = "vcf2omop",
        mixinStandardHelpOptions = true,
        version = "0.6.1",
        description = "Extract OMOP-encoded variants")
public class Vcf2Omop implements Callable<Integer>  {

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{"-h"}; // if the user does not pass any arguments, show help
        }
        CommandLine cline = new CommandLine(new Vcf2Omop()).
                addSubcommand("download", new DownloadCommand()).
                addSubcommand("synonyms", new SynonymsCommand()).
                addSubcommand("omop", new Vcf2OmopCommand());
        cline.setToggleBooleanFlags(false);
        int exitCode = cline.execute(args);
        System.exit(exitCode);
    }

    public Vcf2Omop() {
    }
    @Override
    public Integer call() {
        // work done in subcommands
        return 0;
    }

}
