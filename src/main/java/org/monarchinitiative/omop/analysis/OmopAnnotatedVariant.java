package org.monarchinitiative.omop.analysis;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import org.monarchinitiative.omop.except.Vcf2OmopRuntimeException;

import java.util.ArrayList;
import java.util.List;

public class OmopAnnotatedVariant {

    private final List<OmopAnnotatedTranscript> transcriptAnnotations;

    public OmopAnnotatedVariant(int omopId, String assembly, VariantAnnotations vann) {
        transcriptAnnotations = new ArrayList<>();
        for (Annotation tannot : vann.getAnnotations()) {
            OmopAnnotatedTranscript otranscript = new OmopAnnotatedTranscript(
                    omopId,
                    assembly,
                    tannot,
                    vann.getChrName(),
                    vann.getPos(),
                    vann.getRef(),
                    vann.getAlt(),
                    tannot.getGeneSymbol(),
                    "n/a");
            transcriptAnnotations.add(otranscript);
        }
    }

    public boolean hasAnnotation() {
        return transcriptAnnotations.size() > 0;
    }

    public OmopAnnotatedTranscript getHighestImpactAnnotation() {
        if (! hasAnnotation()) {
            throw new Vcf2OmopRuntimeException("Attempt to retrieve non-existent annotation");
        }
        return this.transcriptAnnotations.get(0); // annotations are pre-sorted!
    }

    public List<OmopAnnotatedTranscript> getTranscriptAnnotations() {
        return this.transcriptAnnotations;
    }


    private final static String [] header = {"OMOP.id", "assembly", "chromosome", "position", "reference", "alternate", "gene", "gene.id", "variant.effect",
            "hgvs.genomic", "hgvs.cdna", "hgvs.protein"};

    public static String getHeaderLine() {
        return String.join("\t", header);
    }

}
