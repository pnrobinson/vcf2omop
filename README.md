# omopulator
A tool to identify cancer-relevant variants in VCF files.

## Setup
Using the version 36-SNAPSHOT of Jannovar, execute the following command
```
 java -Xmx8g -jar jannovar-cli/target/jannovar-cli-0.36-SNAPSHOT.jar download -d hg38/refseq_curated
```
This creates a file called
```
data/hg38_refseq_curated.ser 
```

## To run the app
```bash
git clone https://github.com/pnrobinson/omopulator.git
cd omopulator
mvn package
java -jar target/omopulator.jar jannovar
java -jar target/omopulator.jar omopulate -v <path-to-vcf-file> -j <path/data/hg38_refseq_curated.ser>
```

## To run the demo

We have created a demo with 72 variants including threee cancer-relevant variants
that were spiked in:

* chr17:31357058C>G
* chr12:76346442CT>C
* chrX:71223934T>G

The corresponding VCF file is located here:
```
src/main/resources/sample.vcf
```
The corresponding preliminary OMOP map is here (I added just a small excerpt):
```
src/main/resources/omopmap.csv
```
To run the app, enter
```
java -jar target/omopulator.jar -v src/main/resources/sample.vcf -j <path/to/hg38_refseq_curated.ser>
```

Replace `<path/to/hg38_refseq_curated.ser>` with the correct path on your system.