package org.monarchinitiative.oncembobulator.command;

import org.apache.log4j.Logger;
import org.monarchinitiative.oncembobulator.data.OncoKBParser;

public class Undiscombobulate extends Command {
    static Logger logger = Logger.getLogger(Undiscombobulate.class.getName());
    /** Hard coded path for now.*/
    private static final String oncoKBpath="allActionableVariants.txt";

    public void execute(){
        logger.debug("Executing Undiscombobulate");
        OncoKBParser parser = new OncoKBParser(oncoKBpath);
    }
}
