package org.monarchinitiative.onco.analysis;

import java.util.Comparator;
import java.util.Objects;

public class CivicVariant implements Comparable<CivicVariant> {
    private final String id;
    private final String symbol;
    private final String entrezId;
    private final String variantName;
    private final String summary;
    private final  String chromosome;
    private final int start;
    private final int end;
    private final String references_bases;
    private final String variant_bases;


    private CivicVariant(String id,
            String symbol,
            String entrezId,
            String variantName,
            String summary,
            String chromosome,
            int start,
            int end,
            String references_bases,
            String variant_bases) {
        this.id = id;
        this.symbol = symbol;
        this.entrezId = entrezId;
        this.variantName = variantName;
        this.summary = summary;
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.references_bases = references_bases;
        this.variant_bases = variant_bases;
    }



    public static CivicVariant fromArray(String [] fields) {
        String id = fields[0];
        String symbol = fields[2];
        String entrezId = fields[3];
        String variantName = fields[4];
        String summary = fields[5];
        String chromosome = fields[7];
        int start = Integer.parseInt(fields[8]);
        int end = Integer.parseInt(fields[9]);
        String references_bases = fields[10];
        String variant_bases = fields[11];
        return new CivicVariant(id, symbol, entrezId, variantName, summary, chromosome, start, end, references_bases, variant_bases);
    }

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getEntrezId() {
        return entrezId;
    }

    public String getVariantName() {
        return variantName;
    }

    public String getSummary() {
        return summary;
    }

    public String getChromosome() {
        return chromosome;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getReferences_bases() {
        return references_bases;
    }

    public String getVariant_bases() {
        return variant_bases;
    }

    private static final Comparator<CivicVariant> COMPARATOR =
            Comparator.comparing(CivicVariant::getChromosome)
                .thenComparingInt(CivicVariant::getStart)
                .thenComparingInt(CivicVariant::getEnd)
                .thenComparing(CivicVariant::getReferences_bases)
                .thenComparing(CivicVariant::getVariant_bases);

    @Override
    public int compareTo(CivicVariant other) {
        return COMPARATOR.compare(this,other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CivicVariant that = (CivicVariant) o;
        return start == that.start &&
                end == that.end &&
                Objects.equals(id, that.id) &&
                Objects.equals(symbol, that.symbol) &&
                Objects.equals(entrezId, that.entrezId) &&
                Objects.equals(variantName, that.variantName) &&
                Objects.equals(summary, that.summary) &&
                Objects.equals(chromosome, that.chromosome) &&
                Objects.equals(references_bases, that.references_bases) &&
                Objects.equals(variant_bases, that.variant_bases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, symbol, entrezId, variantName, summary, chromosome, start, end, references_bases, variant_bases);
    }

    @Override
    public String toString() {
        return "CivicVariant{" +
                "id='" + id + '\'' +
                ", symbol='" + symbol + '\'' +
                ", entrezId='" + entrezId + '\'' +
                ", variantName='" + variantName + '\'' +
                ", summary='" + summary + '\'' +
                ", chromosome='" + chromosome + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", references_bases='" + references_bases + '\'' +
                ", variant_bases='" + variant_bases + '\'' +
                '}';
    }
}
