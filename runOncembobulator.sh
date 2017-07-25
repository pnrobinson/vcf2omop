#!/bin/bash

mvn clean package

if [ ! -f "allActionableVariants.txt" ]; then
    java -jar target/oncembobulator-0.0.1-SNAPSHOT.jar download-oncokb
fi

if [ ! -f "clinvarGRCh38.vcf.gz" ]; then
    java -jar target/oncembobulator-0.0.1-SNAPSHOT.jar download-clinvar
fi

java -Xmx4g -Xmx 6g -jar target/oncembobulator-0.0.1-SNAPSHOT.jar jannovar



