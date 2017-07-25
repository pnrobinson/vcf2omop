package org.monarchinitiative.oncembobulator.command;

import org.apache.log4j.Logger;
import org.monarchinitiative.oncembobulator.data.ClinvarParser;
import org.monarchinitiative.oncembobulator.data.Gene2ClinvarMutations;
import org.monarchinitiative.oncembobulator.data.OncoKBParser;
import org.monarchinitiative.oncembobulator.data.OncoKBVariant;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Undiscombobulate extends Command {
    static Logger logger = Logger.getLogger(Undiscombobulate.class.getName());
    /* hardcoded path to downloaded file for now. */
    private static final String oncoKBpath="allActionableVariants.txt";
    /* hardcoded path to downloaded file for now. */
    private static final String clinvarpath="clinvarGRCh38.vcf.gz";
    /** Simple regex for test. Should get missense and some other valid mutations..*/
    private static final String missensePattern="\\w+\\d+\\w+";
    private Pattern r;

    /** All variants from the OncoKB file.*/
    private List<OncoKBVariant> variants;
    /** All gene symbols we found in the OncoKB file -- we will extract ClinVar variants only for these genes. */
    private Set<String> activegenesymbols;

    private Map<String,Gene2ClinvarMutations> gene2mutMap=null;

    public void execute(){
        logger.debug("Executing Undiscombobulate");
        OncoKBParser parser = new OncoKBParser(oncoKBpath);
        variants=parser.getOncoKBVariants();
        r=Pattern.compile(missensePattern);
        activegenesymbols=getOncoKBParsableProteinVariants();
        ClinvarParser cvparser = new ClinvarParser(clinvarpath);
        cvparser.setActiveGeneSymbols(activegenesymbols);
        cvparser.parse();
        this.gene2mutMap=cvparser.getGene2mutMap();
        undiscombobulate();
    }




    private void undiscombobulate() {
        int n=0;
        int found=0;
        for (OncoKBVariant var : variants) {
            String sym=var.getGenesymbol();
            String mutation=var.getMutation();
            Gene2ClinvarMutations g2c=this.gene2mutMap.get(sym);
            if (g2c==null) {
                logger.trace("Could not identify ClinVar data for "+sym);
                continue;
            }
            if (g2c.interpretMutation(mutation) ) {
                found++;
            }
            n++;
        }
        logger.info("We investigated "+n+"oncoKB mutations and found reasonable candidates for "+found);

    }



    private Set<String> getOncoKBParsableProteinVariants() {
        Set<String> syms=new HashSet<>();
        for (OncoKBVariant var:variants) {
            String symbol=var.getGenesymbol();
            String mut=var.getMutation();
            if (isValidMutation(mut)) {
                syms.add(symbol);
            }
        }
        logger.debug("We found "+syms.size()+ " genes with parsable mutation data in OncoKB");
        return syms;
    }
    /** Decide whether an OncoKB mutation string is a valid mutation, e.g., V560D but not Fusions */
    private boolean isValidMutation(String mut) {
        Matcher m = this.r.matcher(mut);
        if (m.find())
            return true;
        else
            return false;
    }
}
