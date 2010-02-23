#!/bin/sh
#
# LSF Script for starting a JVM worker.
#BSUB -q ${lsf.queue.submission}                                   # Job queue
#BSUB -u ${notification.emailaddress}                              # Email address for LSF output
#BSUB -R "rusage[mem=${lsf.parallel.worker.memoryrequirement}]"    # Resource requirement in MB
#BSUB -M ${lsf.parallel.worker.memoryrequirement}
#
# Command to run on LSF
${maven.executable} -e -P runParallelWorker -f ${jms-implementation.pom.path} exec:java ${parallel.worker.xmx}