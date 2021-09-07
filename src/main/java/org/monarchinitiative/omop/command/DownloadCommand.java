package org.monarchinitiative.omop.command;


import org.monarchinitiative.omop.download.Vcf2OmopDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;



/**
 * Download a number of files needed for vcf2omop analysis. We download the Jannovar transcript file.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
@CommandLine.Command(name = "download",  mixinStandardHelpOptions = true, description = "Download files")
public class DownloadCommand implements Callable<Integer> {
    static final Logger logger = LoggerFactory.getLogger(DownloadCommand.class);

    @CommandLine.Option(names = {"-d", "--data"}, description = "location of download directory (default: ${DEFAULT-VALUE})")
    private String downloadDir = "data";

    @Override
    public Integer call()  {
        logger.debug("Downloader");
        Vcf2OmopDownloader downloader = new Vcf2OmopDownloader(downloadDir);
        downloader.download();
        return 0;
    }

}
