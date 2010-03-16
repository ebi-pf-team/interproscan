#!/bin/bash

cd "$(dirname "$0")"

target="/homes/$USER"

rsync -avz target/interproscan-5-dist.dir "ebi-004.ebi.ac.uk:$target"

ssh ebi-004.ebi.ac.uk /sw/arch/pkg/jdk1.6/bin/java -Dconfig=$target/interproscan-5.properties -jar "$target/interproscan-5-dist.dir/interproscan-5.jar"

#end