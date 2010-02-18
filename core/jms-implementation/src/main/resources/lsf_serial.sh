#!/bin/sh
#
# LSF Script for starting a JVM worker.
#BSUB -q production                       # Job queue
#BSUB -R "rusage[mem=2048]"               # Resource requirement in MB
#BSUB -o /net/nas10b/vol1/homes/maslen/work/interproscan/core/jms-implementation/temp-serial-worker.out              # output is sent to file job.output
#
# Command to run on LSF
/net/nas10b/vol1/homes/maslen/work/apache-maven-2.2.1/bin/mvn -e -P runSerialWorker -f /net/nas10b/vol1/homes/maslen/work/interproscan/core/jms-implementation/pom.xml exec:java -DXmx=2048m