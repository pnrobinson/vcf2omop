package org.monarchinitiative.omop.analysis;

import java.util.Objects;

public class ChrPosition {

    private final String chromosome;
    private final int start;
    private final int end;
    public ChrPosition(String chr, int start, int end) {
        this.chromosome = chr;
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChrPosition that = (ChrPosition) o;
        return start == that.start &&
                end == that.end &&
                Objects.equals(chromosome, that.chromosome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chromosome, start, end);
    }
}
