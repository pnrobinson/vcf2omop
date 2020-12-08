package org.monarchinitiative.onco.data;

import de.charite.compbio.jannovar.hgvs.AminoAcidCode;
import de.charite.compbio.jannovar.hgvs.protein.change.ProteinChange;

import de.charite.compbio.jannovar.annotation.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.List;

@Deprecated
public class Gene2ClinvarMutations {
    static final Logger logger = LoggerFactory.getLogger(Gene2ClinvarMutations.class);
    private String genesymbol=null;

    private final List<Annotation> annots;

    public Gene2ClinvarMutations(String sym) {
        genesymbol=sym;
        annots=new ArrayList<>();
    }

    public void addAnnotation(Annotation ann) {
        this.annots.add(ann);
    }
    /** We receive a mutation such as T790M and try to see if there is a corresponding ClinVar entry. */
    public boolean interpretMutation(String mutation) {
        boolean foundSomething=false;
        System.out.println("Attempting to find "+genesymbol+":"+mutation);
        for (Annotation ann :annots) {
            ProteinChange ps = ann.getProteinChange();
            if (ps==null) {
                continue;/* not all variants have a protein change.*/
            }
            String hgvs=ps.toHGVSString(AminoAcidCode.ONE_LETTER);
            if (hgvs==null) {
                logger.error("Could not get hgvs for "+ps);
                continue;
            }
           if (hgvs.contains(mutation)) {
               System.out.println("\t"+ann.toString());
               foundSomething=true;
           }
        }
        return foundSomething;
    }
}
