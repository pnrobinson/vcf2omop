package org.monarchinitiative.onco.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.JannovarException;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.cmd.HelpRequestedException;
import de.charite.compbio.jannovar.cmd.annotate_vcf.JannovarAnnotateVCFOptions;
import de.charite.compbio.jannovar.data.Chromosome;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.progress.GenomeRegionListFactoryFromSAMSequenceDictionary;
import de.charite.compbio.jannovar.progress.ProgressReporter;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.*;

public class ClinvarParser {
    static final Logger logger = LoggerFactory.getLogger(ClinvarParser.class);

    private static final String jannovarfile="data/hg38_refseq.ser";

    /** {@link ReferenceDictionary} with genome information. */
    protected ReferenceDictionary refDict = null;

    /** Map of Chromosomes, used in the annotation. */
    protected ImmutableMap<Integer, Chromosome> chromosomeMap = null;
    /** {@link JannovarData} with the information */
    protected JannovarData jannovarData = null;
    /** Configuration */
    private JannovarAnnotateVCFOptions options;

    private VariantContextAnnotator annotator;

    private final List<VariantAnnotations> annotlist;

    private Map<String,Gene2ClinvarMutations> gene2mutMap=null;

    private Set<String> activegenesymbols=null;

    private String pathToClinvarVCF=null;


    public ClinvarParser(String path) {
        annotlist=new ArrayList<>();
        gene2mutMap=new HashMap<>();
        this.pathToClinvarVCF=path;
    }



    /**
     * Deserialize the transcript definition file.
     *
     * @param pathToDataFile
     *            String with the path to the data file to deserialize
     * @throws JannovarException
     *             when there is a problem with the deserialization
     * @throws HelpRequestedException
     *             when the user requested the help page
     */
    private void deserializeTranscriptDefinitionFile(String pathToDataFile)
            throws JannovarException, HelpRequestedException {
        this.jannovarData = new JannovarDataSerializer(pathToDataFile).load();
        this.refDict = this.jannovarData.getRefDict();
        this.chromosomeMap = this.jannovarData.getChromosomes();
        final boolean isUtrOffTarget = false;
        final boolean isIntronicSpliceOffTarget = false;
        VariantContextAnnotator.Options opts = new VariantContextAnnotator.Options();
                //(false, false, false, false, isUtrOffTarget,
              //  isIntronicSpliceOffTarget);
        this.annotator = new VariantContextAnnotator(refDict,chromosomeMap,opts);
    }

    private void initJannovarOptions() {
        this.options=new JannovarAnnotateVCFOptions();
        options.setEscapeAnnField(true);
        options.setNt3PrimeShifting(true);
        options.setOffTargetFilterEnabled(true);
        options.setOffTargetFilterUtrIsOffTarget(true);
        options.setOffTargetFilterIntronicSpliceIsOffTarget(true);
    }

    public  Map<String,Gene2ClinvarMutations> getGene2mutMap() { return gene2mutMap;}


    public void parse() {
        boolean useInterval=false;
        initJannovarOptions();
        ProgressReporter progressReporter=null;
        int n=0;
        try {
            deserializeTranscriptDefinitionFile(jannovarfile);
        } catch (JannovarException e) {
            logger.error("Could not deserialize Jannovar data");
            logger.error(e.getMessage());
            logger.error("Cannot recover, exiting.");
            System.exit(1);
        }


        try  {
            VCFFileReader vcfReader = new VCFFileReader(new File(this.pathToClinvarVCF), useInterval);
            final SAMSequenceDictionary seqDict = VCFFileReader.getSequenceDictionary(new File(this.pathToClinvarVCF));
                if (seqDict != null) {
                    final GenomeRegionListFactoryFromSAMSequenceDictionary factory = new GenomeRegionListFactoryFromSAMSequenceDictionary();
                    progressReporter = new ProgressReporter(factory.construct(seqDict), 60);
                    progressReporter.printHeader();
                    progressReporter.start();
                } else {
                    System.err.println("Progress reporting does not work because VCF file is missing the contig "
                            + "lines in the header.");
                }
            VCFHeader vcfHeader = vcfReader.getFileHeader();

            logger.trace("Annotating VCF...");
            final long startTime = System.nanoTime();


            for (VariantContext vc : vcfReader) {
                ImmutableList<VariantAnnotations> vclst = this.annotator.buildAnnotations(vc);
                for (VariantAnnotations va : vclst) {
                    ImmutableList<Annotation> annots = va.getAnnotations(); /* get all possible annotations for this variant. */
                    for (Annotation ann : annots) {
                        String sym = ann.getGeneSymbol();
                        //if (sym.startsWith("ID"))
                        //logger.trace("Annotation had gene symbol \""+sym+"\"");
                        if (this.activegenesymbols.contains(sym)) {
                            addAnnotation(sym, ann);
                            n++;
                        }
                    }
                    //System.out.println(va.toString());
                }
            }

            final long endTime = System.nanoTime();
            logger.trace(String.format("Reading clinvar VCF took %.2f sec.",
                    (endTime - startTime) / 1000.0 / 1000.0 / 1000.0));
            logger.trace("Adding a total of "+n+" annotations for "+gene2mutMap.size()+" genes.");

        } catch (Exception e) {
            System.err.println("There was a problem annotating the VCF file");
            System.err.println("The error message was as follows.  The stack trace below the error "
                    + "message can help the developers debug the problem.\n");
            System.err.println(e.getMessage());
            System.err.println("\n");
            e.printStackTrace(System.err);
            return;
        }

        if (progressReporter != null)
            progressReporter.done();
    }

    /** For easier processing, we will store all annotations (mutations from ClinVar) that correpsond to
     * a given gene symbol in their own class instance.
     * @param sym
     * @param ann
     */
    private void addAnnotation(String sym, Annotation ann) {
        Gene2ClinvarMutations g2m=null;
        if (this.gene2mutMap.containsKey(sym)) {
            g2m=gene2mutMap.get(sym);
        } else {
            g2m=new Gene2ClinvarMutations(sym);
            gene2mutMap.put(sym,g2m);
        }
        g2m.addAnnotation(ann);
    }

    /** Set the gene that we will look for (discard other annotations). */
    public void setActiveGeneSymbols(Set<String> symbols) {
        this.activegenesymbols=symbols;
    }
}
