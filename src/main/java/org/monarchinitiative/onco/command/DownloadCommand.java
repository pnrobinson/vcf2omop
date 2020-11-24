package org.monarchinitiative.onco.command;


import org.monarchinitiative.onco.io.FileDownloadException;
import org.monarchinitiative.onco.io.FileDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;



/**
 * Download a number of files needed for the org.jax.hc2go.analysis. We download by default to a subdirectory called
 * {@code data}, which is created if necessary. We download the files {@code hp.obo}, {@code phenotype.hpoa},
 * {@code Homo_sapiencs_gene_info.gz}, and {@code mim2gene_medgen}.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
@CommandLine.Command(name = "download",  mixinStandardHelpOptions = true, description = "Download files")
public class DownloadCommand implements Callable<Integer> {
    static final Logger logger = LoggerFactory.getLogger(DownloadCommand.class);




    private static final String clinvarLocalPath="data/clinvarGRCh38.vcf.gz";


    private static final String clinvarURL="ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/vcf_GRCh38/clinvar.vcf.gz";


    @Override
    public Integer call() throws Exception {
        logger.debug("Executing DownloadOnkoKB");
        download(clinvarURL, clinvarLocalPath);
        return 0;
    }



    private void download(String URL, String localpath){
        File f = new File(localpath);
        if (f.exists()) {
            System.out.println("[INFO] Cowardly refusing to download " + URL + " because we already have the file");
        }

        FileDownloader downloader=new FileDownloader();
        try {
            URL url = new URL(URL);
            logger.debug("Created url from "+URL+": "+url.toString());
            downloader.copyURLToFile(url, new File(localpath));
        } catch (MalformedURLException e) {
            logger.error("Malformed URL for " + localpath);
            logger.error(e.getMessage());
        } catch (FileDownloadException e) {
            logger.error("Error downloading " + URL);
            logger.error(e.getMessage());
        }
    }
}
