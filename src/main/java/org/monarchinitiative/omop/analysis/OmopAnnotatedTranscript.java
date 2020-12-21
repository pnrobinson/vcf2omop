package org.monarchinitiative.omop.analysis;

import de.charite.compbio.jannovar.annotation.Annotation;

public class OmopAnnotatedTranscript {

    private final int omopId;

    private final String assembly;

    private final int pos;
    private final String chrom;
    private final String ref;
    private final String alt;
    private final String geneSymbol;
    private final String geneId;
    private final String hgvsGenomic;
    private final String hgvsTranscript;
    private final String hgvsProtein;
    private final String variantEffect;


    public OmopAnnotatedTranscript(int omopId,
                                   String assembly,
                                   Annotation tannot,
                                   String chromName,
                                   int pos,
                                   String ref,
                                   String alt,
                                   String geneSymbol,
                                   String geneId) {
        this.omopId = omopId;
        this.assembly = assembly;
        this.chrom = chromName.startsWith("chr") ? chromName : "chr" + chromName;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
        this.geneSymbol = geneSymbol;
        this.geneId = geneId;

        this.hgvsGenomic = tannot.getGenomicNTChangeStr();
        this.hgvsTranscript = tannot.getCDSNTChangeStr();
        this.hgvsProtein = tannot.getProteinChangeStr();
        this.variantEffect = tannot.getMostPathogenicVarType().toString();
    }

    public String getTsvLine() {
        return String.format("%d\t%s\t%s\t%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                omopId,
               assembly,
                chrom,
                pos,
                ref,
                alt,
                geneSymbol,
                geneId,
                variantEffect,
                hgvsGenomic,
                hgvsTranscript,
                hgvsProtein);
    }

    public int getOmopId() {
        return omopId;
    }

    public String getAssembly() {
        return assembly;
    }

    public int getPos() {
        return pos;
    }

    public String getChrom() {
        return chrom;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public String getGeneId() {
        return geneId;
    }

    public String getHgvsGenomic() {
        return hgvsGenomic;
    }

    public String getHgvsTranscript() {
        return hgvsTranscript;
    }

    public String getHgvsProtein() {
        return hgvsProtein;
    }

    public String getVariantEffect() {
        return variantEffect;
    }
}
