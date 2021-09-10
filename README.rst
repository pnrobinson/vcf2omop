########
vcf2omop
########


The Observational Health Data Sciences and Informatics 
(`OHDSI <https://ohdsi.org/>`_) consortium develops the Observational Medical Outcomes
Partnership (`OMOP <https://www.ohdsi.org/data-standardization/the-common-data-model/>`_) common data model, which is widely used in 
Electronic Healthcare Record (EHR) research. 
The `OMOP Genomic module <https://github.com/OHDSI/Genomic-CDM>`_ is being developed
to promote the integration of genomic and EHR data. 

The OHDSI genomics workgroup has developed a staging file of genomic variants with relevance to oncology. This file assigns
OMOP concept ids to each of these variants. In the OMOP CDM, each ``concept_id`` is an 
integer that is unique across all domains. It is intended that observations of 
genetic variants will be represented in the 
OMOP `measurement <https://ohdsi.github.io/CommonDataModel/cdm531.html#MEASUREMENT>`_ table in the ``measurement_concept_id`` field.

**vcf2omop** is a Java command-line application that takes as input a VCF file and the OMOP staging file and annotates any variant that is 
represented in the staging file with the corresponding concept_id. Optionally, the app will also annotate 
variants with respect to their predicted effect on transcripts using the 
`Jannovar <https://github.com/charite/jannovar>`_ library.


:Authors:
    Peter N Robinson,
    Daniel Danis

:Version: 0.6.1 of 2021/09/10


Requirements
############

- Java runtime environment with version 8 or higher
- OMOP Genomic Staging File 
- If you want to build vcf2omop from source, you will need `maven <https://maven.apache.org/>`_.

.. role:: raw-html(raw)
   :format: html

:raw-html:`<font color="blue"><b>TODO -- Provide information about where to get the OMOP staging file</b></font>`!


Setup
#####

To build the tool, you can clone the repository and use maven.

.. code-block:: bash

  $ https://github.com/pnrobinson/vcf2omop.git
  $ cd vcf2omop
  $ mvn package


This will create a Java app in the ``target`` directory. To check whether the build was
successful, enter the following.

.. code-block:: bash

  $ java -jar target/vcf2omop.jar 
  Usage: vcf2omop [-hV] [COMMAND]
  Extract OMOP-encoded variants
    -h, --help      Show this help message and exit.
    -V, --version   Print version information and exit.
  Commands:
    download  Download files
    synonyms  Generate table with all transcript "synonyms"
    omop      extract OMOP-annotated vars from VCF



The tool uses a transcript data file from 
the `Jannovar project <https://github.com/charite/jannovar>`_. 
Jannovar offers transcript definition files for `NCBI RefSeq <https://www.ncbi.nlm.nih.gov/refseq/>`_ (hg19, hg38)
and and `Ensembl <https://ensembl.org>`_ (hg19, hg38). 
Before using the app for analysis, run the ``download`` command to  download the Jannovar transcript definition
files. 

.. code-block:: bash

  $ java -jar target/vcf2omop.jar download

This will create a new directory called ``data`` and download the following four files to the directory

- ensembl_87_hg19.ser
- ensembl_91_hg38.ser
- refseq_curated_105_hg19.ser
- refseq_curated_109_hg38.ser


Annotate VCF files with OMOP concept ids
########################################


After setting up the app as above, all you need is a VCF file and a 
path to the OMOP staging file (represented as ``stage_genomic.csv`` in 
the following command). Then run the app with the ``omop`` command as follows.

.. code-block:: bash
  
  $ java -jar target/vcf2omop.jar omop --stage <path/to/stage_genomic.csv> --vcf <path/to/vcf>

There are several options that can be seen with the -h flag.


.. code-block:: bash

  $ java -jar target/vcf2omop.jar omop -h
  Usage: vcf2omop vcf2omop [-hV] [--annot] [-a=<assembly>] [-d=<downloadDir>]
                         [--database=<genomeDatabase>] [-p=<prefix>]
                         -s=<omopStageFilePath> --vcf=<vcfPath>
  extract OMOP-annotated vars from VCF
    -a, --assembly=<assembly>  genome assembly: hg19, GRCh19, hg38, GRCh38,
                               default null
        --annot                add transcript annotations via Jannovar
    -d, --data=<downloadDir>   location of download directory (default: data)
        --database=<genomeDatabase>
                             database: refseq, ensembl
    -h, --help                 Show this help message and exit.
    -p, --prefix=<prefix>      Outfile prefix
    -s, --stage=<omopStageFilePath>
                             path to OMOP stage file
    -V, --version              Print version information and exit.
        --vcf=<vcfPath>        path to VCF file

It is possible to combine the hg19 and hg38 (genome assembly) options with the
refseq or ensembl options. For instance, to annotate a VCF file with variants called according to hg19 using ensembl transcripts, enter the following


.. code-block:: bash
  
  $ java -jar target/vcf2omop.jar omop \
      --stage stage_genomic.csv \
      --vcf sample.vcf \
      -a hg19 \
      -d ensembl


Note that hg19 is the default and thus ``-a hg19`` can be omitted.


The app does not check that your VCF file corresponds
to the option you chose and will produce incorrect results if the options are incorrect.

Expected results
################

If the input VCF file contains variants in the OMOP staging file, the app will
add an annotation to the INFO field of the VCF file. For instance, 
the INFO field of this variant

  5	112177171	rs465899	G	A	4052.01	PASS	(...)

gets a new OMOP annotation 

  (...);MQ=37;MQ0=0;MQRankSum=0.485;OMOP=36746894;(...)

The remaining data in the VCF file is transmitted as is.

If the user chooses the ``--annot`` option, then the VCF file is additionally annotated
with transcript level annotations for each variant. For instance, 


  to do improve Jannovar annotations.


Generate synonyms
^^^^^^^^^^^^^^^^^

This new feature generates a table of 'synoynms'.


  $ java -jar target/vcf2omop.jar synonyms [-a hg19]

Expected results
^^^^^^^^^^^^^^^^

This command will generate a file called ``synonyms-vcf2omop-GRCh19.tsv``. Here is the output for the first two variants -- we see all of the
transcript-level variants that correspond to the indicated genomic variant. One could run analogous commands to
generate synonyms with refseq ids or for hg38.


    omop.id	chrom	pos	ref	alt	gene.symbol	hgvs.genomic	hgvs.transcript	hgvs.protein
    1801782	19	4099314	G	A	MAP2K2	g.4099314G>A	ENST00000262948.5:c.804C>T	p.(=)
    1801782	19	4099314	G	A	MAP2K2	g.4099314G>A	ENST00000394867.4:c.513C>T	p.(=)
    1801782	19	4099314	G	A	MAP2K2	g.4099314G>A	ENST00000599021.1:c.29+1703C>T	p.(=)
    1801782	19	4099314	G	A	MAP2K2	g.4099314G>A	ENST00000593364.1:n.751C>T
    1801782	19	4099314	G	A	MAP2K2	g.4099314G>A	ENST00000595715.1:n.619C>T
    1801782	19	4099314	G	A	MAP2K2	g.4099314G>A	ENST00000600584.1:n.1364C>T
    1801782	19	4099314	G	A	MAP2K2	g.4099314G>A	ENST00000601786.1:n.1105C>T
    1801782	19	4099314	G	A	MAP2K2	g.4099314G>A	ENST00000597263.1:n.169+1703C>T
    1802019	9	21974760	C	T	CDKN2A	g.21974760C>T	ENST00000304494.5:c.67G>A	p.(G23S)
    1802019	9	21974760	C	T	CDKN2A	g.21974760C>T	ENST00000380151.3:c.67G>A	p.(G23S)
    1802019	9	21974760	C	T	CDKN2A	g.21974760C>T	ENST00000446177.1:c.67G>A	p.(G23S)
    1802019	9	21974760	C	T	CDKN2A	g.21974760C>T	ENST00000498124.1:c.67G>A	p.(G23S)
    1802019	9	21974760	C	T	CDKN2A	g.21974760C>T	ENST00000579122.1:c.67G>A	p.(G23S)
    1802019	9	21974760	C	T	CDKN2A	g.21974760C>T	ENST00000361570.3:c.317-3553G>A	p.(=)
    1802019	9	21974760	C	T	CDKN2A	g.21974760C>T	ENST00000530628.2:c.194-3553G>A	p.(=)
    1802019	9	21974760	C	T	CDKN2A	g.21974760C>T	ENST00000579755.1:c.194-3553G>A	p.(=)
    1802019	9	21974760	C	T	RP11-145E5.5	g.21974760C>T	ENST00000404796.2:c.348-54672C>T	p.(=)
    1802019	9	21974760	C	T	CDKN2A	g.21974760C>T	ENST00000494262.1:c.-3-3553G>A	p.(=)
    1802019	9	21974760	C	T	CDKN2A	g.21974760C>T	ENST00000498628.2:c.-3-3553G>A	p.(=)



:raw-html:`<font color="blue"><b>TODO -- Decide what to do with variants that do not intersect with a transcript, e.g., 35981064: 5:1295228G>A (GRCh19)
Currently the app says Could not annotate entry OmopStagedVariant: 35981064: 5:1295228G>A (GRCh19)! but this is not an error.</b></font>`!


To run the demo
###############

We have spiked in 2 OMOP-relevant variants for a demo. The VCF file is available at

  src/main/resources/sample-hg19.vcf

The corresponding VCF file is located here:

  src/main/resources/sample.vcf

Therefore, run the app as follows.


.. code-block:: bash

  $ java -jar target/vcf2omop.jar vcf2omop --vcf src/main/resources/sample.vcf


This is the output (both to the command line and to a file, which by default is called ``vcf2omop.tsv``).


  36740245	hg38	chr1	92836283	G	A	RPL5	6125	MISSENSE_VARIANT	g.92836283G>A	c.418G>A	p.(Gly140Ser)
  35981554	hg38	chr7	5982885	C	T	PMS2	5395	MISSENSE_VARIANT	g.5982885C>T	c.2113G>A	p.(Glu705Lys)


Run the app with the ``--all`` flag to see all transcripts.