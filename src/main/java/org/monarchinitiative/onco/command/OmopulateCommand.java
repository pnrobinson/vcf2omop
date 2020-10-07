package org.monarchinitiative.onco.command;


import org.monarchinitiative.onco.analysis.Ompopulate;
import org.monarchinitiative.onco.data.Gene2ClinvarMutations;
import org.monarchinitiative.onco.data.OncoKBVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "omopulate",  mixinStandardHelpOptions = true, description = "omopulate")
public class OmopulateCommand implements Callable<Integer>  {
    static Logger logger = LoggerFactory.getLogger(OmopulateCommand.class);
    @CommandLine.Option(names = {"--vcf"}, description ="path to VCF file", required = true)
    private String vcfPath;
    @CommandLine.Option(names = {"-j", "--jannovar"}, description = "path to Jannovar transcript file")
    private String jannovarPath;

    /** All variants from the OncoKB file.*/
    private List<OncoKBVariant> variants;
    /** All gene symbols we found in the OncoKB file -- we will extract ClinVar variants only for these genes. */
    private Set<String> activegenesymbols;

    private Map<String,Gene2ClinvarMutations> gene2mutMap=null;

    @Override
    public Integer call() throws Exception {
        logger.debug("Executing Ompopulate");
        Ompopulate ompopulate = new Ompopulate(jannovarPath, vcfPath);
        return 0;
    }

}
