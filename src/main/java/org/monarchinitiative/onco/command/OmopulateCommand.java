package org.monarchinitiative.onco.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.monarchinitiative.onco.analysis.Ompopulate;
import org.monarchinitiative.onco.data.Gene2ClinvarMutations;
import org.monarchinitiative.onco.data.OncoKBVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Parameters(commandDescription = "annotate a VCF")
public class OmopulateCommand extends Command {
    static Logger logger = LoggerFactory.getLogger(OmopulateCommand.class);
    @Parameter(names={"-v","--vcf"}, description ="path to VCF file", required = true)
    private String vcfPath;

    private String jannovarPath = "data/hg19_ucsc.ser";

    /** All variants from the OncoKB file.*/
    private List<OncoKBVariant> variants;
    /** All gene symbols we found in the OncoKB file -- we will extract ClinVar variants only for these genes. */
    private Set<String> activegenesymbols;

    private Map<String,Gene2ClinvarMutations> gene2mutMap=null;

    public void execute(){
        logger.debug("Executing Ompopulate");
        Ompopulate ompopulate = new Ompopulate(jannovarPath, vcfPath);
    }




}
