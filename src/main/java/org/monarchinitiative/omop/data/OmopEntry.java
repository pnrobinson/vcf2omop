package org.monarchinitiative.omop.data;

import java.util.Objects;

/**
 * One entry (line) from the omopmap file
 * 1	100068	G	A	36739402
 */
public class OmopEntry {

    private static final String NOT_AVAILABLE = "";
    private final int omopId;
    private final VcfVariant variant;

    public OmopEntry(String chr, int pos, String ref, String alt, int id) {
        this.variant = new VcfVariant(chr,pos, ref, alt);
        this.omopId = id;
    }



    public String getChromosome() {
        return variant.getChromosome();
    }

    public String getRef() {
        return variant.getRef();
    }

    public String getAlt() {
        return variant.getAlt();
    }

    public int getPosition() {
        return variant.getPosition();
    }

    public int getOmopId() {
        return omopId;
    }

    public VcfVariant getVariant() {
        return variant;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.variant, this.omopId);
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof OmopEntry)) return false;
        OmopEntry that = (OmopEntry) obj;
        return this.variant.equals(that.variant) &&
                this.omopId == that.omopId;
    }

    @Override
    public String toString() {
        return String.format("%s:%d%s>%s[omop:%d", getChromosome(), getPosition(), getRef(), getAlt(), getOmopId());
    }
}
