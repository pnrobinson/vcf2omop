package org.monarchinitiative.oncembobulator.command;

import de.charite.compbio.jannovar.Jannovar;
import org.apache.log4j.Logger;

public class CreateJannovarTranscriptFile extends Command {
    static Logger logger = Logger.getLogger(CreateJannovarTranscriptFile.class.getName());

    public void execute() {
        logger.debug("Executing CreateJannovarTranscriptFile");
        String args[]={"download","-d","hg38/refseq"};
        Jannovar.main(args);

    }
}
