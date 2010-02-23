#!/bin/sh
#
# LSF Script for starting a JVM worker.
#BSUB -q ${lsf.queue.submission}                                 # Job queue
#BSUB -R "rusage[mem=${lsf.serial.worker.memoryrequirement}]"    # Resource requirement in MB
#BSUB -M ${lsf.serial.worker.memoryrequirement}
#BSUB -o ${absolute.output.path}/${lsf.output.file}              # output is sent to file job.output
#
# Command to run on LSF
${maven.executable} -e -P runSerialWorker -f ${jms-implementation.pom.path} exec:java ${serial.worker.xmx}