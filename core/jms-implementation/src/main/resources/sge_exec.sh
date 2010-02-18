#!/bin/sh
#
# Sun Grid Engine Script for starting a JVM worker.
#
# Join stdout and stderr
#$ -j y
#
# Email settings
#$ -m aes
#
# Email address
#$ -M maslen@ebi.ac.uk
#
# Output file
#$ -o /net/nas10b/vol1/homes/maslen/work/interproscan/core/jms-implementation/temp-serial-worker.out
#
/net/nas10b/vol1/homes/maslen/work/apache-maven-2.2.1/bin/mvn -e -P runSerialWorker -f /net/nas10b/vol1/homes/maslen/work/interproscan/core/jms-implementation/pom.xml exec:java -DXmx=2048m