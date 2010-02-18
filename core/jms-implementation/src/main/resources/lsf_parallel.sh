#!/bin/sh
#
# LSF Script for starting a JVM worker.
#BSUB-q production                       # Job queue
#BSUB-u maslen@ebi.ac.uk                 # Email address for LSF output
#BSUB-R "rusage[mem=2048]"               # Resource requirement in MB
#
# Command to run on LSF
/net/nas10b/vol1/homes/maslen/work/apache-maven-2.2.1/bin/mvn -e -P runParallelWorker -f /net/nas10b/vol1/homes/maslen/work/interproscan/core/jms-implementation/pom.xml exec:java $xmxMemory