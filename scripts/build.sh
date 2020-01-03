cd core 
mvn -q clean install  
cd jms-implementation 
mvn -q clean package 
cd target/interproscan-5-dist 
cd $TRAVIS_BUILD_DIR/core/jms-implementation/target/interproscan-5-dist
df -h  
wget ftp://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/bin/getorf.ubuntu18.04 
mv getorf.ubuntu18.04 bin/nucleotide/getorf
chmod +x bin/nucleotide/getorf
./interproscan.sh -i test_proteins.fasta -f tsv -dp -appl sfld, hamap,prints,smart,pfam,pirsf,tigrfam,prositeprofiles,prositepatterns,gene3d,superfamily
./interproscan.sh -i test_nt_seqs.fasta -t n -f tsv -dp -appl sfld, hamap,prints,smart,pfam,pirsf,tigrfam,prositeprofiles,prositepatterns,gene3d,superfamily


