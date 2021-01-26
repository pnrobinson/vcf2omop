package org.monarchinitiative.omop.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Command to download the {@code hp.obo} and {@code phenotype.hpoa} files that
 * we will need to run the LIRICAL approach.
 * @author Peter N Robinson
 */
public class Vcf2OmopDownloader {
    private static final Logger logger = LoggerFactory.getLogger(Vcf2OmopDownloader.class);
    /** Directory to which we will download the files. */
    private final String downloadDirectory;
    /** If true, download new version whether or not the file is already present. */
    private final boolean overwrite;

    private static final String JannovarHg19ZenodoUrl = "https://zenodo.org/record/4468026/files/hg19_refseq.ser?download=1";
    private static final String JannovarHg38ZenodoUrl = "https://zenodo.org/record/4468026/files/hg38_refseq_curated.ser?download=1";
    private static final String JannovarHg38Filename = "hg38_refseq_curated.ser";
    private static final String JannovarHg19Filename = "hg19_refseq.ser";
    public Vcf2OmopDownloader(String path){
        this(path,false);
    }

    public Vcf2OmopDownloader(String path, boolean overwrite){
        this.downloadDirectory=path;
        this.overwrite=overwrite;
        logger.info("overwrite="+overwrite);
    }

    /**
     * Download the files unless they are already present.
     */
    public void download() {
        downloadFileIfNeeded(JannovarHg38Filename, JannovarHg38ZenodoUrl);
        downloadFileIfNeeded(JannovarHg19Filename, JannovarHg19ZenodoUrl);
    }


    private void downloadFileIfNeeded(String filename, String webAddress) {
        File f = new File(String.format("%s%s%s",downloadDirectory,File.separator,filename));
        if (f.exists() && (! overwrite)) {
            logger.trace(String.format("Cowardly refusing to download %s since we found it at %s",
                    filename,
                    f.getAbsolutePath()));
            return;
        }
        FileDownloader downloader=new FileDownloader();
        try {
            URL url = new URL(webAddress);
            logger.debug("Created url from "+webAddress+": "+url.toString());
            downloader.copyURLToFile(url, new File(f.getAbsolutePath()));
        } catch (MalformedURLException e) {
            logger.error(String.format("Malformed URL for %s [%s]",filename, webAddress));
            logger.error(e.getMessage());
        } catch (FileDownloadException e) {
            logger.error(String.format("Error downloading %s from %s" ,filename, webAddress));
            logger.error(e.getMessage());
        }
        System.out.println("[INFO] Downloaded " + filename);
    }





}
