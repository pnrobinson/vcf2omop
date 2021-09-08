package org.monarchinitiative.omop.data;

import java.util.Objects;

/**
 * This class is used as a key to compare {@link org.monarchinitiative.omop.stage.OmopStagedVariant} objects
 * with variants in a VCF file.
 * @author Peter N Robinson
 */
public class VcfVariant {

    private final String chromosome;
    private final String ref;
    private final String alt;
    private final int position;

    public VcfVariant(String chr, int pos, String ref, String alt){
        if (chr.startsWith("chr"))
            this.chromosome = chr;
        else
            this.chromosome = "chr" + chr;
        this.position = pos;
        this.ref = ref;
        this.alt = alt;
    }

    public String getChromosome() {
        return chromosome;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.chromosome, this.position, this.ref, this.alt);
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof VcfVariant)) return false;
        VcfVariant that = (VcfVariant) obj;
        return this.chromosome.equals(that.chromosome) &&
                this.position == that.position &&
                this.ref.equals(that.ref) &&
                this.alt.equals(that.alt);
    }

    @Override
    public String toString() {
        return String.format("%s:%d%s>%s", this.chromosome, this.position, this.ref, this.alt);
    }
}
