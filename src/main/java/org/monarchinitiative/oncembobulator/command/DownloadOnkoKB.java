package org.monarchinitiative.oncembobulator.command;

import org.apache.log4j.Logger;
import org.monarchinitiative.oncembobulator.io.FileDownloadException;
import org.monarchinitiative.oncembobulator.io.FileDownloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadOnkoKB extends Command {
    static Logger logger = Logger.getLogger(DownloadOnkoKB.class.getName());

    private static final String oncokbURL="http://oncokb.org/api/v1/utils/allActionableVariants.txt";
    public void execute(){
        logger.debug("Executing DownloadOnkoKB");
        FileDownloader downloader=new FileDownloader();
        try {
            URL url = new URL(oncokbURL);
            logger.debug("Created url from "+oncokbURL+": "+url.toString());
            downloader.copyURLToFile(url, new File("./allActionableVariants.txt"));
        } catch (MalformedURLException e) {
            logger.error("Malformed URL for oncoKB");
            logger.error(e,e);
        } catch (FileDownloadException e) {
            logger.error("Error downloading oncoKB file");
            logger.error(e,e);
        }
    }
}
