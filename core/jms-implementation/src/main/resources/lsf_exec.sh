#!/bin/sh
#
# LSF Script for starting a JVM worker.
#BSUB -q ${mvn.lsf.queue.submission}                                   # Job queue
#BSUB -u ${mvn.notification.emailaddress}                              # Email address for LSF output
#BSUB -R "rusage[mem=${mvn.lsf.parallel.worker.memoryrequirement}]"    # Resource requirement in MB
#BSUB -M ${mvn.lsf.parallel.worker.memoryrequirement}
#
# Command to run on LSF
${mvn.maven.executable} -e -P runParallelWorker -f ${mvn.jms-implementation.pom.path} exec:java ${mvn.parallel.worker.xmx}