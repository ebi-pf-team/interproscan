#!/bin/sh
#
# LSF Script for starting a JVM worker.
#BSUB-q ${lsf.queue.submission}                       # Job queue
#BSUB-u ${notification.emailaddress}                 # Email address for LSF output
#BSUB-R "rusage[mem=2048]"               # Resource requirement in MB
#
# Command to run on LSF
${maven.executable} -e -P runParallelWorker -f ${jms-implementation.pom.path} exec:java