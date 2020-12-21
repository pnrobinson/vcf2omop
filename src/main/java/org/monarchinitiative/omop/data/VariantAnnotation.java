package org.monarchinitiative.omop.data;



import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.util.List;
import java.util.Objects;

/**
 * Simple immutable data class to represent annotations for a variant.
 * Adapted from code by Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantAnnotation {

    private static final VariantAnnotation EMPTY = new Builder().build();

    private final String genomeAssembly;
    private final int chromosome;
    private final String chromosomeName;
    private final int position;
    private final String ref;
    private final String alt;

    private final String geneSymbol;
    private final String geneId;
    private final VariantEffect variantEffect;
    private final List<TranscriptAnnotation> annotations;

    private VariantAnnotation(Builder builder) {
        this.genomeAssembly = builder.genomeAssembly;
        this.chromosome = builder.chromosome;
        this.chromosomeName = builder.chromosomeName;
        this.position = builder.position;
        this.ref = builder.ref;
        this.alt = builder.alt;
        this.geneSymbol = builder.geneSymbol;
        this.geneId = builder.geneId;
        this.variantEffect = builder.variantEffect;
        this.annotations = ImmutableList.copyOf(builder.annotations);
    }

    public String getGenomeAssembly() {
        return genomeAssembly;
    }

    public int getChromosome() {
        return chromosome;
    }

    public String getChromosomeName() {
        return chromosomeName;
    }

    public int getPosition() {
        return position;
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

    public VariantEffect getVariantEffect() {
        return variantEffect;
    }

    public List<TranscriptAnnotation> getTranscriptAnnotations() {
        return annotations;
    }


    public boolean hasTranscriptAnnotations() {
        return !annotations.isEmpty();
    }

    public static VariantAnnotation empty() {
        return EMPTY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariantAnnotation that = (VariantAnnotation) o;
        return chromosome == that.chromosome &&
                position == that.position &&
                genomeAssembly.equals(that.genomeAssembly) &&
                Objects.equals(chromosomeName, that.chromosomeName) &&
                Objects.equals(ref, that.ref) &&
                Objects.equals(alt, that.alt) &&
                Objects.equals(geneSymbol, that.geneSymbol) &&
                Objects.equals(geneId, that.geneId) &&
                variantEffect == that.variantEffect &&
                Objects.equals(annotations, that.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomeAssembly, chromosome, chromosomeName, position, ref, alt, geneSymbol, geneId, variantEffect, annotations);
    }

    @Override
    public String toString() {
        return "VariantAnnotation{" +
                "genomeAssembly=" + genomeAssembly +
                ", chromosome=" + chromosome +
                ", chromosomeName='" + chromosomeName + '\'' +
                ", position=" + position +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                ", geneSymbol='" + geneSymbol + '\'' +
                ", geneId='" + geneId + '\'' +
                ", variantEffect=" + variantEffect +
                ", annotations=" + annotations +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        //Should be GenomeAssembly.UNSPECIFIED ?
        private String genomeAssembly = "HG19";
        private int chromosome = 0;
        private String chromosomeName = "";
        private int position = 0;
        private String ref = "";
        private String alt = "";
        private String geneSymbol = "";
        private String geneId = "";
        private VariantEffect variantEffect = VariantEffect.SEQUENCE_VARIANT;
        private List<TranscriptAnnotation> annotations = ImmutableList.of();

        public Builder genomeAssembly(String genomeAssembly) {
            this.genomeAssembly = genomeAssembly;
            return this;
        }

        public Builder chromosome(int chr) {
            this.chromosome = chr;
            return this;
        }

        public Builder chromosomeName(String chromosomeName) {
            this.chromosomeName = chromosomeName;
            return this;
        }

        public Builder position(int position) {
            this.position = position;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder alt(String alt) {
            this.alt = alt;
            return this;
        }

        public Builder geneSymbol(String geneSymbol) {
            this.geneSymbol = geneSymbol;
            return this;
        }

        public Builder geneId(String geneId) {
            this.geneId = geneId;
            return this;
        }

        public Builder variantEffect(VariantEffect variantEffect) {
            this.variantEffect = variantEffect;
            return this;
        }

        public Builder annotations(List<TranscriptAnnotation> annotations) {
            this.annotations = annotations;
            return this;
        }

        public VariantAnnotation build() {
            return new VariantAnnotation(this);
        }
    }
}
