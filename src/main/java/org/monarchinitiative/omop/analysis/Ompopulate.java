package org.monarchinitiative.omop.analysis;

import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.*;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.progress.ProgressReporter;
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

    private final String vcfFilePath;

    private final List<OmopAnnotatedVariant> variantAnnotations;

    private final Map<VcfVariant, Integer> variant2omopIdMap;

    private final VariantContextAnnotator variantEffectAnnotator;

    private static final String OMOP_FLAG_FIELD_NAME = "OMOP";
    private static final VCFFilterHeaderLine OMOP_FLAG_LINE = new VCFFilterHeaderLine(OMOP_FLAG_FIELD_NAME,
            "OMOP genomics concept ID");
    private static final String JANNOVAR_FLAG_FIELD_NAME = "ANN";
    private static final VCFFilterHeaderLine JANNOVAR_FLAG_LINE = new VCFFilterHeaderLine(JANNOVAR_FLAG_FIELD_NAME,
            "Jannovar annotation");


    /**
     *
     * @param jannovarPath Path to Jannovar transcript file (use download command to get them in the data subdirectory)
     * @param vcfPath path to input VCF file
     * @param assembly Must be one of GRCh19 or GRCh38
     * @param stagedVariantList variants contained in the OMOP list
     */
    public Ompopulate(String jannovarPath, String vcfPath, String assembly, List<OmopStagedVariant> stagedVariantList) {
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
        /**
         * Reference dictionary that is part of {@link #jannovarData}.
         */
        ReferenceDictionary refDict = jannovarData.getRefDict();
        /**
         * Map of Chromosomes, used in the annotation.
         */
        ImmutableMap<Integer, Chromosome> chromosomeMap = jannovarData.getChromosomes();
        this.vcfFilePath = f.getAbsolutePath();
        this.variantAnnotations = new ArrayList<>();
        this.variantEffectAnnotator =
                new VariantContextAnnotator(refDict, chromosomeMap,
                        new VariantContextAnnotator.Options());
        VariantAnnotator annotator = new VariantAnnotator(refDict, chromosomeMap, new AnnotationBuilderOptions());

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
    public void annotateVcf(File outFileName) {
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
        logger.info("[INFO] Processing completed in {} seconds.\n", (1e-9 * (endTime - startTime)));
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
        header.addMetaDataLine(JANNOVAR_FLAG_LINE);
        return header;
    }

    public List<OmopAnnotatedVariant> getVariantAnnotations() {
        return this.variantAnnotations;
    }

}
