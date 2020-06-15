# omopulator
A tool to identify variants list in CIVIC and later metaKB in VCF files.

## To run the app
```bash
git clone https://github.com/pnrobinson/omopulator.git
cd omopulator
mvn package
java -jar target/omopulator.jar download
java -jar target/omopulator.jar jannovar
java -jar target/omopulator.jar omopulate -v <path-to-vcf-file>
```


Please note there is a workaround for the jannovar step because of an upstream "bug" with hg19.
