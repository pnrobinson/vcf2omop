package org.monarchinitiative.onco.command;

import de.charite.compbio.jannovar.Jannovar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Note -- there is a bug that requires us to first run this code and then
 * delete the line chrM    16571   /gbdb/hg19/hg19.2bit from the chromInfo.txt.gz file
 * (and then recompressing the file).
 */
public class CreateJannovarTranscriptFile extends Command {
    static Logger logger = LoggerFactory.getLogger(CreateJannovarTranscriptFile.class);
    private static final Path iniPath = Paths.get("src","main","resources","jannovar_sources.ini");
    private static final String iniAbsPath = iniPath.toAbsolutePath().toString();
    private static final String downloadDir = "data";

    public void execute() {
        logger.debug("Executing CreateJannovarTranscriptFile");
        String args[]={"download","-d", "hg38/ensembl", "-s", iniAbsPath, "--download-dir", downloadDir};
        Jannovar.main(args);

    }
}
