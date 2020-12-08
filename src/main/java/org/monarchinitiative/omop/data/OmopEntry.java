package org.monarchinitiative.omop.data;

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
    public String toString() {
        return String.format("%s:%d%s>%s", this.chromosome, this.position, this.ref, this.alt);
    }
}
