package org.monarchinitiative.omop.data;

import java.util.Objects;

/**
 * One entry (line) from the omopmap file
 * 1	100068	G	A	36739402
 */
public class OmopEntry {

    private static final String NOT_AVAILABLE = "";

    private final String chromosome;
    private final String ref;
    private final String alt;
    private final int position;
    private final int omopId;

    public OmopEntry(String chr, int pos, String ref, String alt, int id) {
        if (chr.startsWith("chr"))
            this.chromosome = chr;
        else
            this.chromosome = "chr" + chr;
        this.position = pos;
        this.ref = ref;
        this.alt = alt;
        this.omopId = id;
    }

    public boolean isEqual(String chromosome, int pos, String ref, String alt) {
        return this.chromosome.equals(chromosome) && this.position == pos && this.ref.equals(ref) && this.alt.equals(alt);
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

    public int getOmopId() {
        return omopId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.chromosome, this.position, this.ref, this.alt, this.omopId);
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof OmopEntry)) return false;
        OmopEntry that = (OmopEntry) obj;
        return this.chromosome.equals(that.chromosome) &&
                this.position == that.position &&
                this.ref.equals(that.ref) &&
                this.alt.equals(that.alt) &&
                this.omopId == that.omopId;
    }

    @Override
    public String toString() {
        return String.format("%s:%d%s>%s", this.chromosome, this.position, this.ref, this.alt);
    }
}
