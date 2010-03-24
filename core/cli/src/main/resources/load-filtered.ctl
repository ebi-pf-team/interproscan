LOAD DATA
INFILE '/ebi/production/interpro/onion/test/g3d/aquinn-results/UPI000002973F_UPI0000124FE1.fil'
APPEND INTO TABLE ONION.IPRSCAN
FIELDS TERMINATED BY X'9' (
   analysis_type_id  INTEGER EXTERNAL(2),
   upi               CHAR(13),
   method_ac         CHAR(25),
   relno_major       INTEGER EXTERNAL(6),
   relno_minor       INTEGER EXTERNAL(6),
   seq_start         INTEGER EXTERNAL(6),
   seq_end           INTEGER EXTERNAL(6),
   hmm_start         INTEGER EXTERNAL(6),
   hmm_end           INTEGER EXTERNAL(6),
   hmm_bounds        CHAR(20),
   score             DECIMAL EXTERNAL(126),
   seqscore          DECIMAL EXTERNAL(126),
   evalue	         DECIMAL EXTERNAL(126),
   status            CHAR(1),
   timestamp	     SYSDATE
)

