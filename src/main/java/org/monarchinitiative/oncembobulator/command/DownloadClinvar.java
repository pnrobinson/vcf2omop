package org.monarchinitiative.oncembobulator.command;

import org.apache.log4j.Logger;
import org.monarchinitiative.oncembobulator.io.FileDownloadException;
import org.monarchinitiative.oncembobulator.io.FileDownloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadClinvar extends Command {
    static Logger logger = Logger.getLogger(DownloadClinvar.class.getName());

    private static final String clinvarURL="ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/vcf_GRCh38/clinvar.vcf.gz";
    public void execute(){
        logger.debug("Executing Clinvar");
        FileDownloader downloader=new FileDownloader();
        try {
            URL url = new URL(clinvarURL);
            logger.debug("Will download from url "+url.toString());
            downloader.copyURLToFile(url, new File("./clinvarGRCh38.vcf.gz"));
        } catch (MalformedURLException e) {
            logger.error("Malformed URL for ClinVar");
            logger.error(e,e);
        } catch (FileDownloadException e) {
            logger.error("Error downloading ClinVar VCF file");
            logger.error(e,e);
        }
    }


}
