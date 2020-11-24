package org.monarchinitiative.onco;



import de.charite.compbio.jannovar.JannovarException;
import org.monarchinitiative.onco.command.DownloadCommand;
import org.monarchinitiative.onco.command.JannovarDownloadCommand;
import org.monarchinitiative.onco.command.OmopulateCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "omopulator", mixinStandardHelpOptions = true, version = "0.2.5",
        description = "Extract omop-encoded variants")
public class Omopulator implements Callable<Integer>  {
    static Logger logger = LoggerFactory.getLogger(Omopulator.class);



    public static void main(String[] args) {

        CommandLine cline = new CommandLine(new Omopulator()).
                addSubcommand("download", new DownloadCommand()).
                addSubcommand("omopulate", new OmopulateCommand()).
                addSubcommand("jannovar", new JannovarDownloadCommand());
        cline.setToggleBooleanFlags(false);
        int exitCode = cline.execute(args);
        System.exit(exitCode);




    }

    public Omopulator() {
    }
    @Override
    public Integer call() throws Exception {
        // work done in subcommands
        return 0;
    }

}
