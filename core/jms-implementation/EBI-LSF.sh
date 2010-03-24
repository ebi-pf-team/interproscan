#!/bin/bash

cd "$(dirname "$0")"

target="/homes/$USER"

rsync -avz target/interproscan-5-dist "ebi-002.ebi.ac.uk:$target"

# Empty the JMS broker before starting up.
ssh ebi-002.ebi.ac.uk rm -rf $target/i5_broker_data

ssh ebi-002.ebi.ac.uk /sw/arch/pkg/jdk1.6/bin/java -Djava.util.logging.config.file=$target/interproscan-5-dist/scripts/hornetq.logging.properties -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -Xms512M -Xmx2048M -Dconfig=$target/interproscan-5.properties -jar "$target/interproscan-5-dist/interproscan-5.jar" master

#end
