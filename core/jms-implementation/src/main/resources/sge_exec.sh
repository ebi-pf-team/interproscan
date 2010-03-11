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
#$ -M ${mvn.notification.emailaddress}
#
# Output file
#$ -o ${mvn.absolute.output.path}/${mvn.sge.output.file}
#
${mvn.maven.executable} -e -P runSerialWorker -f ${mvn.jms-implementation.pom.path} exec:java ${mvn.sge.memory.setting}