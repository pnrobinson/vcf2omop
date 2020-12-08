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


    private static final String JannovarZenodoUrl = "https://zenodo.org/record/4311513/files/hg38_refseq_curated.ser?download=1";
    private static final String JannovarFilename = "hg38_refseq_curated.ser";
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
        int downloaded = 0;
        downloaded += downloadFileIfNeeded(JannovarFilename,JannovarZenodoUrl);
        if (downloaded > 0) {
            System.out.printf("[INFO] Downloaded %s  to \"%s\"\n",
                    JannovarFilename,
                    downloadDirectory);
        } else {
            System.out.printf("[INFO] %s previously downloaded to \"%s\"\n",
                    JannovarFilename,
                    downloadDirectory);
        }

    }


    private int downloadFileIfNeeded(String filename, String webAddress) {
        File f = new File(String.format("%s%s%s",downloadDirectory,File.separator,filename));
        if (f.exists() && (! overwrite)) {
            logger.trace(String.format("Cowardly refusing to download %s since we found it at %s",
                    filename,
                    f.getAbsolutePath()));
            return 0;
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
        return 1;
    }





}
