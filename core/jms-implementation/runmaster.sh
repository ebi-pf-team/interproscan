echo "Enter home directory"
cd

echo "Kill any currently running jobs"
ssh -x pjones@ebi-001.ebi.ac.uk bkill 0

echo "Delete any old files"
rm -rf /nfs/nobackup/interpro/default-test/*

echo "Empty the database"
cd /homes/pjones/Desktop/
empty.sh

ssh -x pjones@maple.windows.ebi.ac.uk cd /home/pjones/Applications/hornetq-2.0.0.GA/bin/\;./stop_i5_broker.sh

ssh -x pjones@maple.windows.ebi.ac.uk cd /home/pjones/Applications/hornetq-2.0.0.GA/bin/\;./start_i5_broker.sh

echo "Enter working directory for the master"
cd /ebi/sp/pro1/interpro/programmers/pjones/projects/i5/jms-implementation/target/interproscan-5-dist.dir

echo "Run the master."
java -Xmx2048m -Xms32m -Dconfig=../../conf/phil-application.properties -jar interproscan-5.jar
