package org.monarchinitiative.omop.data;

public class MyJannovarAnnotator {

    /*
    public VariantAnnotation annotate(String contig, int pos, String ref, String alt) {
        //so given the above, trim the allele first, then annotate it otherwise untrimmed alleles from multi-allelic sites will give different results
        AllelePosition trimmedAllele = AllelePosition.trim(pos, ref, alt);
        VariantAnnotations variantAnnotations = jannovarAnnotationService.annotateVariant(contig, trimmedAllele.getPos(), trimmedAllele
                .getRef(), trimmedAllele.getAlt());
        return buildVariantAlleleAnnotation(genomeAssembly, contig, trimmedAllele, variantAnnotations);
    }
     */


    public void annotate(String contig, int pos, String ref, String alt) {

    }


}
