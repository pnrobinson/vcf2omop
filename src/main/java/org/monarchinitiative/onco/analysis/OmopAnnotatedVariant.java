package org.monarchinitiative.onco.analysis;

import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.onco.except.OmopulatorRuntimeException;

import java.util.ArrayList;
import java.util.List;

public class OmopAnnotatedVariant {

    private final List<OmopAnnotatedTranscript> transcriptAnnotations;

    public OmopAnnotatedVariant(int omopId, String assembly, VariantAnnotation vann) {
        transcriptAnnotations = new ArrayList<>();
        for (TranscriptAnnotation tannot : vann.getTranscriptAnnotations()) {
            OmopAnnotatedTranscript otranscript = new OmopAnnotatedTranscript(
                    omopId,
                    assembly,
                    tannot,
                    vann.getChromosomeName(),
                    vann.getPosition(),
                    vann.getRef(),
                    vann.getAlt(),
                    vann.getGeneSymbol(),
                    vann.getGeneId());
            transcriptAnnotations.add(otranscript);
        }
    }

    public boolean hasAnnotation() {
        return transcriptAnnotations.size() > 0;
    }

    public OmopAnnotatedTranscript getHighestImpactAnnotation() {
        if (! hasAnnotation()) {
            throw new OmopulatorRuntimeException("Attempt to retrieve non-existent annotation");
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
