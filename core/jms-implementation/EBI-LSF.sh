#!/bin/bash

cd "$(dirname "$0")"
distribution_folder=interproscan-5-dist

# DO NOT SET TO ebi-002 - currently being used for phobius production.
master_host=ebi-002.ebi.ac.uk
target="/homes/$USER"
broker_data_folder=$target/$distribution_folder/broker_data
temporary_file_folder=$target/$distribution_folder/temp
temporary_file_folder=/nfs/nobackup/interpro/pjones

# Empty the JMS broker before starting up.
ssh ${master_host} ps -o pid,cmd -u $USER \| grep interproscan \| cut -c 1-5 \| xargs kill
ssh ${master_host} bkill -g /interproscan-test 0
ssh ${master_host} rm -r $broker_data_folder
ssh ${master_host} rm -r $temporary_file_folder
ssh ${master_host} sqlplus OPS\$PJONES/ispro_pjones@ispro @/nfs/seqdb/production/interpro/production/interproscan-5/empty.sql

rsync -avz target/${distribution_folder} "${master_host}:$target"

#ssh ${master_host} cd $target/$distribution_folder\;/sw/arch/pkg/jdk1.6/bin/java -Djava.util.logging.config.file=$target/${distribution_folder}/scripts/hornetq.logging.properties -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -Xms512M -Xmx2048M -Dconfig=$target/interproscan-5.properties -jar "interproscan-5.jar" "$@"
#ssh ${master_host} cd $target/$distribution_folder\;/sw/arch/pkg/jdk1.6/bin/java -Djava.util.logging.config.file=$target/${distribution_folder}/scripts/hornetq.logging.properties -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -Xms512M -Xmx2048M -jar "interproscan-5.jar" "$@"
ssh ${master_host} cd $target/$distribution_folder\;/sw/arch/pkg/jdk1.6/bin/java -Djava.util.logging.config.file=$target/${distribution_folder}/scripts/hornetq.logging.properties -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -Xms512M -Xmx2048M -Dconfig=/homes/pjones/projects/i5/core/jms-implementation/distributed-i5.properties -jar "interproscan-5.jar" "$@"

#end
