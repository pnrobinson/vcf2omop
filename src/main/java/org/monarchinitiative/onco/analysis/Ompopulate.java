package org.monarchinitiative.onco.analysis;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.data.*;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.progress.ProgressReporter;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegionIndex;
import org.monarchinitiative.exomiser.core.model.RegulatoryFeature;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.onco.data.OmopEntry;
import org.monarchinitiative.onco.data.OmopMapParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.JannovarVariantAnnotator;

import java.io.File;
import java.util.List;


public class Ompopulate {
    private final static Logger logger = LoggerFactory.getLogger(Ompopulate.class);
    private final JannovarData jannovarData;
    /**
     * Reference dictionary that is part of {@link #jannovarData}.
     */
    private final ReferenceDictionary referenceDictionary;
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

    final List<OmopEntry> entries;

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

    /** Must be one of GRCh19 or GRCh38. */
    private final String genomeAssembly;

    public Ompopulate(String jannovarPath, String vcfPath, String assembly) {
        this.genomeAssembly = assembly;
        OmopMapParser parser = new OmopMapParser(genomeAssembly);
        this.entries = parser.getEntries();
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
        this.referenceDictionary = jannovarData.getRefDict();
        this.chromosomeMap = jannovarData.getChromosomes();
        this.vcfFilePath = f.getAbsolutePath();
        parseVcf();
    }



    private final String [] header = {"OMOP.id", "assembly", "chromosome", "position", "reference", "alternate", "gene", "gene.id", "variant.effect",
                        "hgvs.genomic", "hgvs.cdna", "hgvs.protein"};


    private void parseVcf() {
        // whether or not to just look at a specific genomic interval
        final boolean useInterval = false;
        final long startTime = System.nanoTime();
        System.out.println("[INFO] VCF: " + this.vcfFilePath);
        System.out.println(String.join("\t", header));
        try (VCFFileReader vcfReader = new VCFFileReader(new File(this.vcfFilePath), useInterval)) {
            //final SAMSequenceDictionary seqDict = VCFFileReader.getSequenceDictionary(new File(getOptionalVcfPath));
            VCFHeader vcfHeader = vcfReader.getFileHeader();
            this.samplenames = vcfHeader.getSampleNamesInOrder();
            this.n_samples = samplenames.size();
            this.samplename = samplenames.get(0);
            logger.trace("Annotating VCF at " + this.vcfFilePath + " for sample " + this.samplename);
            CloseableIterator<VariantContext> iter = vcfReader.iterator();
            VariantContextAnnotator variantEffectAnnotator =
                    new VariantContextAnnotator(this.referenceDictionary, this.chromosomeMap,
                            new VariantContextAnnotator.Options());
            GenomeAssembly genomeAssembly = GenomeAssembly.HG38;
            List<RegulatoryFeature> emtpylist = ImmutableList.of();
            ChromosomalRegionIndex<RegulatoryFeature> emptyRegionIndex = ChromosomalRegionIndex.of(emtpylist);
            JannovarVariantAnnotator jannovarVariantAnnotator = new JannovarVariantAnnotator(genomeAssembly, jannovarData, emptyRegionIndex);
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
                //variantEffectAnnotator.
                List<Allele> altAlleles = vc.getAlternateAlleles();
                String contig = vc.getContig();
                int start = vc.getStart();
                String ref = vc.getReference().getBaseString();
                for (Allele allele : altAlleles) {
                    String alt = allele.getBaseString();
                    VariantAnnotation va = jannovarVariantAnnotator.annotate(contig, start, ref, alt);
                    VariantEffect variantEffect = va.getVariantEffect();
                    int end = start + alt.length() - 1;
                    ChrPosition pos = new ChrPosition(contig, start, end);
                    for (OmopEntry entry : this.entries) {
                        if (entry.isEqual(contig, start, ref, alt)) {
                            List<TranscriptAnnotation> annots = va.getTranscriptAnnotations();
                            for (TranscriptAnnotation ann : annots) {
                                System.out.printf("%d\t%s\t%s\t%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                                        entry.getOmopId(),
                                        this.genomeAssembly,
                                        va.getChromosomeName(),
                                        va.getPosition(),
                                        va.getRef(),
                                        va.getAlt(),
                                        va.getGeneSymbol(),
                                        va.getGeneId(),
                                        va.getVariantEffect(),
                                        ann.getHgvsGenomic(),
                                        ann.getHgvsCdna(),
                                        ann.getHgvsProtein());
                            }
                        }
                    }


                }
            }
        }
        System.out.printf("[INFO] VCF had a total of %d variants and and %d low-quality variants filtered out.\n", n_good_quality_variants, n_filtered_variants);
    }


}
