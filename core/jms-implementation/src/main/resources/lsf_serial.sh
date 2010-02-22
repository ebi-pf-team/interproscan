#!/bin/sh
#
# LSF Script for starting a JVM worker.
#BSUB -q ${lsf.queue.submission}                       # Job queue
#BSUB -R "rusage[mem=2048]"               # Resource requirement in MB
#BSUB -o ${absolute.output.path}/${lsf.output.file}              # output is sent to file job.output
#
# Command to run on LSF
${maven.executable} -e -P runSerialWorker -f ${jms-implementation.pom.path} exec:java ${lsf.memory.setting}