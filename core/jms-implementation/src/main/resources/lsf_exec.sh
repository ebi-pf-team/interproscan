#!/bin/sh
#
# LSF Script for starting a JVM worker.
#BSUB -q ${mvn.lsf.queue.submission}                                   # Job queue
#BSUB -u ${mvn.notification.emailaddress}                              # Email address for LSF output
#BSUB -R "rusage[mem=${mvn.lsf.parallel.worker.memoryrequirement}]"    # Resource requirement in MB
#BSUB -M ${mvn.lsf.parallel.worker.memoryrequirement}
#
# Command to run on LSF
# ${mvn.maven.executable} -e -P runParallelWorker -f ${mvn.jms-implementation.pom.path} exec:java ${mvn.parallel.worker.xmx}

/sw/arch/pkg/jdk1.6/bin/java -Xms32m -Xmx2048m -Dconfig=/ebi/sp/pro1/interpro/programmers/pjones/projects/i5/jms-implementation/conf/phil-application.properties -jar /ebi/sp/pro1/interpro/programmers/pjones/projects/i5/jms-implementation/target/interproscan-5-dist.dir/interproscan-5.jar worker