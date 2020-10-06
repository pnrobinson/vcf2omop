package org.monarchinitiative.onco.data;

public class OmopEntry {

    private final String chromosome;
    private final String ref;
    private final String alt;
    private final int position;
    private final String annots;

    public OmopEntry(String chr, int pos, String ref, String alt, String annots) {
        this.chromosome = chr;
        this.position = pos;
        this.ref = ref;
        this.alt = alt;
        this.annots = annots;
    }


    public boolean isEqual(String chromosome, int pos, String ref, String alt) {
        return this.chromosome.equals(chromosome) && this.position == pos && this.ref.equals(ref) && this.alt.equals(alt);
    }

    @Override
    public String toString() {
        return String.format("%s:%d%s>%s [%s]", this.chromosome, this.position, this.ref, this.alt, this.annots);
    }
}
