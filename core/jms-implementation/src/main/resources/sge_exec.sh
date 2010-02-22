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
#$ -M ${notification.emailaddress}
#
# Output file
#$ -o ${absolute.output.path}/${sge.output.file}
#
${maven.executable} -e -P runSerialWorker -f ${jms-implementation.pom.path} exec:java ${sge.memory.setting}