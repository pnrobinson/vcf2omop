# vcf2omop

A tool to identify OMOP-listed variants in VCF files.

## Setup

To build the tool, you can clone the repository and use maven.
```
https://github.com/pnrobinson/vcf2omop.git
cd vcf2omop
mvn package
```
This will create a Java app in the ``target`` directory. To check whether the build was
successful, enter the following.
```
$ java -jar target/vcf2omop.jar 
Usage: vcf2omop [-hV] [COMMAND]
Extract omop-encoded variants
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  download  Download files
  vcf2omop  extract OMOP-annotated vars from VCF
```


The tool uses a transcript data file from the [Jannovar project](https://github.com/charite/jannovar). It is possible
to build data files for RefSeq curated or Ensembl. Before using the app for analysis, run the download command to  download Jannovar transcript definition
files for hg19 and hg38. 

```
$ java -jar target/vcf2omop.jar download

```

To download fresh versions of these files, either delete the ``data`` subdirectory or pass the ``--overwrite`` flag.

## Annotate VCF files with OMOP concept ids: vcf2omop
After setting up the app as above, all you need is a VCF file and a path to the OMOP staging file (represented as ``stage_genomic.csv`` in the following command).

```
$ java -jar target/vcf2omop.jar vcf2omop --stage <path/to/stage_genomic.csv> --vcf <path/to/vcf>
```
There are several options that can be seen with the -h flag.





## To generate synonyms
This new feature generates a table of 'synoynms'.

```
$ java -jar target/vcf2omop.jar synonyms [-a hg19]
```

## To run the demo

We have spiked in 2 OMOP-relevant variants for a demo. THe VCF file is available at
``src/main/resources/sample-hg38.vcf``

The corresponding VCF file is located here:
```
src/main/resources/sample.vcf
```
Therefore, run the app as follows.
```
$ java -jar target/vcf2omop.jar vcf2omop --vcf src/main/resources/sample.vcf
```

This is the output (both to the command line and to a file, which by default is called ``vcf2omop.tsv``).
```
36740245	hg38	chr1	92836283	G	A	RPL5	6125	MISSENSE_VARIANT	g.92836283G>A	c.418G>A	p.(Gly140Ser)
35981554	hg38	chr7	5982885	C	T	PMS2	5395	MISSENSE_VARIANT	g.5982885C>T	c.2113G>A	p.(Glu705Lys)
```

Run the app with the ``--all`` flag to see all transcripts.