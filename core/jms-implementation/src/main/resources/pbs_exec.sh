#!/bin/sh
#
# Portable Batch System (PBS) Script for starting a JVM worker.
#
# Join stdout and stderr
#PBS -j oe
#
# Email settings
#PBS -m ${pbs.mail.settings}
#
# Email address
#PBS -M ${notification.emailaddress}
#
# Queue job submitted to
#PBS -q ${pbs.queue.submission}
#
# Output file
#PBS -o ${absolute.output.path}/${pbs.output.file}
#
echo ${maven.executable}
echo ${jms-implementation.pom.path}
echo ${pbs.memory.setting}
${maven.executable} -e -P runSerialWorker -f ${jms-implementation.pom.path} exec:java ${pbs.memory.setting}