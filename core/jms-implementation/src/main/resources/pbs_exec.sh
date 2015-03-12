#!/bin/sh
#
# Portable Batch System (PBS) Script for starting a JVM worker.
#
# Join stdout and stderr
#PBS -j oe
#
# Email settings
#PBS -m ${mvn.pbs.mail.settings}
#
# Email address
#PBS -M ${mvn.notification.emailaddress}
#
# Queue job submitted to
#PBS -q ${mvn.pbs.queue.submission}
#
# Output file
#PBS -o ${mvn.absolute.output.path}/${mvn.pbs.output.file}
#
${mvn.maven.executable} -e -P runSerialWorker -f ${mvn.jms-implementation.pom.path} exec:java ${mvn.pbs.memory.setting}