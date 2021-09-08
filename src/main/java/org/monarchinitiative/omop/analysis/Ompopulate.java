package org.monarchinitiative.omop.analysis;

import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.annotation.Annotation;
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
import htsjdk.tribble.TribbleException;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFFilterHeaderLine;
import htsjdk.variant.vcf.VCFHeader;

import htsjdk.variant.vcf.VCFHeaderVersion;
import org.monarchinitiative.omop.data.VcfVariant;
import org.monarchinitiative.omop.except.Vcf2OmopRuntimeException;
import org.monarchinitiative.omop.stage.OmopStagedVariant;
import org.monarchinitiative.omop.vcf.VcfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;


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

    private  final VariantAnnotator annotator;

    private final VariantContextAnnotator variantEffectAnnotator;

    private final boolean showAllAffectedTranscripts;

    private static final String OMOP_FLAG_FIELD_NAME = "OMOP-Genomics";
    private static final VCFFilterHeaderLine OMOP_FLAG_LINE = new VCFFilterHeaderLine(OMOP_FLAG_FIELD_NAME,
            "OMOP genomics concept ID");


    /** Must be one of GRCh19 or GRCh38. */
    private final String genomeAssembly;

    public Ompopulate(String jannovarPath, String vcfPath, String assembly, List<OmopStagedVariant> stagedVariantList, boolean showAll) {
        this.genomeAssembly = assembly;
        this.showAllAffectedTranscripts = showAll;
        variant2omopIdMap = new HashMap<>();
        for (OmopStagedVariant e : stagedVariantList) {
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
        this.variantEffectAnnotator =
                new VariantContextAnnotator(this.refDict, this.chromosomeMap,
                        new VariantContextAnnotator.Options());
        this.annotator = new VariantAnnotator(this.refDict, chromosomeMap, new AnnotationBuilderOptions());

    }

    /**
     * It is not reliable to get the VCF version from HTSJDK --
     * vcfHeader.getVCFHeaderVersion().getVersionString() can give a NP and there is no other accessor.
     * It is easier to read the first line of the VCF file separately.
     * @param f
     * @return
     */
    public String getVcfVersionString(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String versionLine = br.readLine();
        if (versionLine != null && versionLine.startsWith("##")) {
            return versionLine;
        } else {
            throw new Vcf2OmopRuntimeException("Could not extract first line from " + f.getAbsolutePath());
        }
    }


    /**
     * Input -- a VariantContext object.
     * We check if the corresponding variant is in our OMOP list
     * If so, we add the concept id abd return the same VariantContext
     *
     * @return variant context (OMOP info added to INFO if applicable)
     */
    private Function<VariantContext, VariantContext> addInfoFields() {
        return vc -> {
            // first do Jannovar annotations
            vc = variantEffectAnnotator.annotateVariantContext(vc);
            VariantContextBuilder builder = new VariantContextBuilder(vc);
            // now look for OMOP matches
            String contig = vc.getContig();
            int start = vc.getStart();
            String ref = vc.getReference().getBaseString();
            for (Allele allele : vc.getAlternateAlleles()) {
                String alt = allele.getBaseString();
                VcfVariant variant = new VcfVariant(contig, start, ref, alt);
                if (this.variant2omopIdMap.containsKey(variant)) {
                    int omopId = variant2omopIdMap.get(variant);
                    builder.attribute(OMOP_FLAG_FIELD_NAME, String.valueOf(omopId));
                }
            }
            return builder.make();
        };
    }


    /**
     * Add OMOP annotations to matching variants in a VCF file by adding corresponding annotations to the
     * INFO field for corresponding variants and outputing the rest of the original VCF file unchanged.
     */
    public void annotateVcf(File outFileName) throws IOException {
        final long startTime = System.nanoTime();
        logger.info("Parsing VCF: " + this.vcfFilePath);
        File vcfFile = new File(this.vcfFilePath); // input file
        try (VCFFileReader vcfReader = new VCFFileReader(vcfFile, false)) {
            VCFHeader vcfHeader = prepareVcfHeader(vcfFile.toPath());
            VariantContextWriter vcfWriter = new VariantContextWriterBuilder().setOutputFile(outFileName)
                    .setReferenceDictionary(vcfReader.getFileHeader().getSequenceDictionary())
                    .unsetOption(Options.INDEX_ON_THE_FLY)
                    .build();
            vcfWriter.writeHeader(vcfHeader);
            Stream<VariantContext> variants = vcfReader.iterator().stream()
                    .map(addInfoFields());
            variants.forEach(vcfWriter::add);
        }
        final long endTime = System.nanoTime();
        logger.info("[INFO] Processing completed in %.2f seconds.\n", (1e-9 * (endTime - startTime)));
    }


    /**
     * Extend the <code>header</code> with INFO fields that are being added in this command.
     *
     * @return the extended header
     */
    private VCFHeader prepareVcfHeader(Path inputVcfPath) {
        VCFHeader header;
        try (VCFFileReader reader = new VCFFileReader(inputVcfPath, false)) {
            header = reader.getFileHeader();
        } catch (TribbleException.MalformedFeatureFile e) {
            // happens when the input variants were not read from a VCF file but from e.g. a CSV file
            logger.info("Creating a stub VCF header");
            header = new VCFHeader();
            header.setVCFHeaderVersion(VCFHeaderVersion.VCF4_2);
        }
        // OMOP-Genomics - flag
        header.addMetaDataLine(OMOP_FLAG_LINE);
        return header;
    }

    public List<OmopAnnotatedVariant> getVariantAnnotations() {
        return this.variantAnnotations;
    }

}
