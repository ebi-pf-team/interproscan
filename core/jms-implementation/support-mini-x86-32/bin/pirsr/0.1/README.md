# pirsr

PIR Site Rules


## Introduction

PIR Site Rules is a series of HMMs and rules to match sites, manually created based on template sequence.

For a sequence to hit a PIRSR it needs to hit a HMM and conform to the crafted residue site rules for that HMM.

The runner script is pirsr.pl and it uses the PIRSR.pm package.

To get help you can run
```
perl pirsr.pl -man
```


## Data Source

Protein Information Resource provides regular data updates for PIRSR. Those can be found at https://proteininformationresource.org/ura/pirsr/files_for_ebi/srhmm_for_interpro/

Data comes in a tarball with the name SR-InterPro-YYYY-MM.tar.gz and updates are released roughly monthly.

Inside the tarball there is a data/ folder, this is the folder that will be required for processing and building of the PIRSR system.


## Data Processing


Assuming you downloaded SR-InterPro-2020_05.tar.gz, you can untar the files and preprocess the data use the following commands:
```
tar xvzf SR-InterPro-2020_05.tar.gz
perl pirsr.pl -data SR-InterPro-2020_05/data/ -preprocess
```

Inside the  SR-InterPro-2020_05/data/ folder there are 3 important data types that are processed (almost) independently:

### sr_tp/ folder

This folder contains the file sr_tp.seq, which contains the sequence of all template sequences in fasta format.

This file is processed and split into individual sequence files named <template_seq_name>.fasta

### sr_hmm/ folder

This folder contains the collection of hmm files, all with the field "NAME  clustalw.hmm3".

Those are processed and added into the sr_hmm.hmm library file, with the NAME field corrected into the name of the individual hmm file.

The library pressed at the end of the processing.

### PIRSR.uru file

This file contains the rule descriptors in a specific format. This is an example of a rule:
PIRSF.uru rule  Expand source

The rule file is processed and split into individual rule files in json format. The rule above would become the file:
PIRSR002560-1.json  Expand source

As can be seen, the information contained in the uru file is now contained in the json file in a more readily accessible way computationally.

Additionally the "hmmStart" and "hmmEnd" fields in the rules have been added by translating template sequence residue positions to hmm positions.


## Querying

With all the data processed and stored away, we are ready to process a query fasta file with query sequences.

Assuming we have the query sequences in a query.fasta file and we want the results to be written to output.json, we can use the command:
```
perl pirsr.pl -data SR-InterPro-2020_05/data/ -query query.fasta -out output.json
```

### HMM scanning

Each query sequence is scanned against the hmm library and hits stored together with the hmm alignments.

Hitting a hmm is the first step to triggering a PIRSR, the second is to conform to the site rules for that hmm.

### Site rule scanning

A rule is in fact most often a collection of rules. As said before the first is for the sequence to match the corresponding rule hmm.

Then a PIRSR may have several groups of individual site rules.

For a query sequence to trigger a PIRSR it needs to conform to all individual site rules for at least one group.

Thus, the condition for each individual site rule on the groups is checked, and a PIRSR pass flag is activated when one group of individual rules is fully satisfied.

### Output

Output from the querying is produced in JSON format, with the query sequence id at the top level, with a matching rule (queries that do not match a rule are left out) then the id of the rules that match and then all the information about the rule hmm match and the actual Rules sites. An example of output is presented.
