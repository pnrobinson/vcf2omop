package org.monarchinitiative.omop.stage;

import org.monarchinitiative.omop.data.VcfVariant;
import org.monarchinitiative.omop.except.Vcf2OmopRuntimeException;

import java.util.Objects;

public class OmopStagedVariant {

    private final static int X_CHROMOSOME = 23;
    private final static int Y_CHROMOSOME = 24;
    private final static int MT_CHROMOSOME = 25;

    private final int omopId;
    private final int chrom;
    private final int pos;
    private final String ref;
    private final String alt;
    private final Assembly assembly;

    public OmopStagedVariant(int omopId, int chrom, int pos, String ref, String alt, Assembly assembly) {
        this.omopId = omopId;
        this.chrom = chrom;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
        this.assembly = assembly;
    }


    public int getOmopId() {
        return omopId;
    }

    public int getChrom() {
        return chrom;
    }

    public String chromToString() {
        switch (chrom) {
            case MT_CHROMOSOME:
                return "MT";
            case Y_CHROMOSOME:
                return "Y";
            case X_CHROMOSOME:
                return "X";
            default:
                return String.valueOf(chrom);
        }
    }

    public String getChromWithChr() {
        return "chr" + chromToString();
    }

    public int getPos() {
        return pos;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }

    public VcfVariant toVcfVariant() {
        return new VcfVariant(chromToString(), pos, ref, alt);
    }

    public Assembly getAssembly() {
        return assembly;
    }

    public static OmopStagedVariant fromLine(String line) {
        String [] fields = line.split(",");
        if  (fields.length != 6) {
            throw new Vcf2OmopRuntimeException("Malformed stage file line (" + line
                    +")  (we were expecting 6 fields but got " + fields.length +").");
        }
        int omopId = Integer.parseInt(fields[0]);
        int chrom = getChrom(fields[1]);
        int pos = Integer.parseInt(fields[2]);
        String ref = fields[3];
        String alt = fields[4];
        Assembly assembly = getAssembly(fields[5]);
        return new OmopStagedVariant(omopId, chrom, pos,  ref,  alt,  assembly);
    }

    private static Assembly getAssembly(String field) {
        switch (field) {
            case "37":
            case "hg19":
            case "hg37":
            case "GRCh37":
                return Assembly.GRCh19;
            case "38":
            case "hg38":
            case "GRCh38":
                return Assembly.GRCh38;
        }
        throw new Vcf2OmopRuntimeException("DId not recognize assembly (" + field + ")");
    }


    private static int getChrom(String field) {
        if (field.equalsIgnoreCase("X")) {
            return X_CHROMOSOME;
        } else if (field.equalsIgnoreCase("Y")) {
            return Y_CHROMOSOME;
        } else if (field.equalsIgnoreCase("M") || field.equalsIgnoreCase("MT")) {
            return MT_CHROMOSOME;
        } else {
            try {
                int c = Integer.parseInt(field);
                if (c > 0 && c < 23) {
                    return c;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        throw new Vcf2OmopRuntimeException("Invalid chromosome: \"" + field + "\"");
    }

    @Override
    public int hashCode() {
        return Objects.hash(omopId, chrom, pos, ref, alt, assembly);
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof  OmopStagedVariant)) return false;
        OmopStagedVariant that = (OmopStagedVariant) obj;
        return this.omopId == that.omopId &&
                this.chrom == that.chrom &&
                this.pos == that.pos &&
                this.ref.equals(that.ref) &&
                this.alt.equals(that.alt) &&
                this.assembly.equals(that.assembly);
    }


    @Override
    public String toString() {
        return String.format("OmopStagedVariant: %d: %s:%d%s>%s (%s)",
                omopId, chromToString(), pos, ref, alt, assembly);
    }
}
