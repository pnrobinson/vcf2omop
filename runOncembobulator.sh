#!/bin/bash


echo "** Running Onembobulator **"
echo "** Will first download necessary files and then analyze oncoKB variants"

mvn clean package

if [ ! -f "allActionableVariants.txt" ]; then
    java -jar target/oncembobulator-0.0.1-SNAPSHOT.jar download-oncokb
fi

if [ ! -f "clinvarGRCh38.vcf.gz" ]; then
    java -jar target/oncembobulator-0.0.1-SNAPSHOT.jar download-clinvar
fi

if [ ! -f "data/hg38_refseq.ser" ]; then
    java -Xmx8g -Xmx8g -jar target/oncembobulator-0.0.1-SNAPSHOT.jar jannovar
fi


java  -Xmx4g -Xmx2g  -jar target/oncembobulator-0.0.1-SNAPSHOT.jar undiscombobulate




