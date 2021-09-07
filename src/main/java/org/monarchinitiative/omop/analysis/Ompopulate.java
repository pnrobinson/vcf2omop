package org.monarchinitiative.omop.analysis;

import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.*;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.progress.ProgressReporter;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;

import org.monarchinitiative.omop.data.VcfVariant;
import org.monarchinitiative.omop.stage.StagedVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Ompopulate {
    private final static Logger logger = LoggerFactory.getLogger(Ompopulate.class);
    private final JannovarData jannovarData;
    /**
     * Reference dictionary that is part of {@link #jannovarData}.
     */
    private final ReferenceDictionary refDict;
    /**
     * Map of Chromosomes, used in the annotation.
     */
    private final ImmutableMap<Integer, Chromosome> chromosomeMap;
    /**
     * A Jannovar object to report progress of VCF parsing.
     */
    private ProgressReporter progressReporter = null;
    private final String vcfFilePath;

    //GenomeAssembly genomeAssembly = GenomeA
    /**
     * Number of variants that were not filtered.
     */
    private int n_good_quality_variants = 0;
    /**
     * Number of variants that were removed because of the quality filter.
     */
    private int n_filtered_variants = 0;

    /**
     * Number of samples in the VCF file.
     */
    private int n_samples;
    /**
     * Name of the proband in the VCF file.
     */
    private String samplename;
    /**
     * List of all names in the VCF file
     */
    private List<String> samplenames;

    private final List<OmopAnnotatedVariant> variantAnnotations;

    private final Map<VcfVariant, Integer> variant2omopIdMap;

    private final boolean showAllAffectedTranscripts;




    /** Must be one of GRCh19 or GRCh38. */
    private final String genomeAssembly;

    public Ompopulate(String jannovarPath, String vcfPath, String assembly, List<StagedVariant> stagedVariantList, boolean showAll) {
        this.genomeAssembly = assembly;
        this.showAllAffectedTranscripts = showAll;
        variant2omopIdMap = new HashMap<>();
        for (StagedVariant e : stagedVariantList) {
            variant2omopIdMap.put(e.toVcfVariant(), e.getOmopId());
        }
        try {
            this.jannovarData = new JannovarDataSerializer(jannovarPath).load();
        } catch (SerializationException se) {
            throw new RuntimeException(se.getMessage());
        }
        System.out.printf("[INFO] We ingested %d transcripts from %s.\n",
                this.jannovarData.getTmByAccession().size(), jannovarPath);
        File f = new File(vcfPath);
        if (!f.exists()) {
            throw new RuntimeException("Could not find VCF file at " + vcfPath);
        }
        this.refDict = jannovarData.getRefDict();
        this.chromosomeMap = jannovarData.getChromosomes();
        this.vcfFilePath = f.getAbsolutePath();
        this.variantAnnotations = new ArrayList<>();
        parseVcf();
    }



    private void parseVcf() {
        final long startTime = System.nanoTime();
        System.out.println("[INFO] VCF: " + this.vcfFilePath);
        try (VCFFileReader vcfReader = new VCFFileReader(new File(this.vcfFilePath), false)) {
            //final SAMSequenceDictionary seqDict = VCFFileReader.getSequenceDictionary(new File(getOptionalVcfPath));
            VCFHeader vcfHeader = vcfReader.getFileHeader();
            this.samplenames = vcfHeader.getSampleNamesInOrder();
            this.n_samples = samplenames.size();
            this.samplename = samplenames.get(0);
            logger.trace("Annotating VCF at " + this.vcfFilePath + " for sample " + this.samplename);
            CloseableIterator<VariantContext> iter = vcfReader.iterator();
            VariantContextAnnotator variantEffectAnnotator =
                    new VariantContextAnnotator(this.refDict, this.chromosomeMap,
                            new VariantContextAnnotator.Options());
            final VariantAnnotator annotator = new VariantAnnotator(this.refDict, chromosomeMap, new AnnotationBuilderOptions());
            while (iter.hasNext()) {
                VariantContext vc = iter.next();
                if (vc.isFiltered()) {
                    // this is a failing VariantContext
                    n_filtered_variants++;
                    continue;
                } else {
                    n_good_quality_variants++;
                }
                vc = variantEffectAnnotator.annotateVariantContext(vc);
                List<Allele> altAlleles = vc.getAlternateAlleles();
                String contig = vc.getContig();
                int start = vc.getStart();
                String ref = vc.getReference().getBaseString();
                for (Allele allele : altAlleles) {
                    String alt = allele.getBaseString();
                    int chr = jannovarData.getRefDict().getContigNameToID().get(contig);
                    GenomeVariant genomeChange = new GenomeVariant(new GenomePosition(this.refDict, Strand.FWD, chr, start, PositionType.ONE_BASED), ref, alt);
                    try {
                        VariantAnnotations annoList = annotator.buildAnnotations(genomeChange);
                        VcfVariant candidate = new VcfVariant(contig, start, ref, alt);
                        if (this.variant2omopIdMap.containsKey(candidate)) {
                            int omopId = variant2omopIdMap.get(candidate);
                            this.variantAnnotations.add(new OmopAnnotatedVariant(omopId, genomeAssembly, annoList));
                        }
                    } catch (Exception e) {
                        System.err.printf("[ERROR] Could not annotate variant %s!\n", vc);
                        e.printStackTrace(System.err);
                    }
                }
            }
        }
        System.out.printf("[INFO] VCF had a total of %d variants. %d low-quality variants were filtered out.\n", n_good_quality_variants, n_filtered_variants);
        final long endTime = System.nanoTime();
        System.out.printf("[INFO] Processing completed in %.2f seconds.\n", (1e-9*(endTime-startTime)));
    }

    public List<OmopAnnotatedVariant> getVariantAnnotations() {
        return this.variantAnnotations;
    }

}
